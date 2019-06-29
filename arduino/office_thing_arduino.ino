#include <SPI.h>
#include <MFRC522.h>
#include <WiFiNINA.h>
#include <ArduinoHttpClient.h>
#include <ArduinoJson.h>
#include <Servo.h>
#include <DHT.h>
#include "arduino_secrets.h"
#define DHTPIN 2
#define DHTTYPE DHT22 //DHT11, DHT21, DHT22

/*
  RC522  /  MKR1010
  SDA / 7
  RST / 6
  MISO / MISO
  SCK / SCK
  MOSI / MOSI
  3.3V  / VCC
  GND / GND
*/

#define SS_PIN 7
#define RST_PIN 6

#define BUZZER_PIN 5

#define LED_RED A1
#define LED_GREEN A2
#define LED_BLUE A3

char server[] = SECRET_SERVERHOST;
int port = 443;
int status = WL_IDLE_STATUS;
char ssid[] = SECRET_SSID;       // your network SSID (name)
char pass[] = SECRET_PASSWORD;    // your network password (use for WPA, or use as key for WEP)
char clientuser[] = SECRET_CLIENTUSER; // basic auth credentials for REST endpoints
char clientpassword[] = SECRET_CLIENTPASSWORD; // basic auth credentials for REST endpoints


bool saved = false;

// timer for request
// 1h = 3600000 ms
int intervalStockMillis = 30000;
// 1 min = 900000 ms
int intervalClimateMillis = 450000;

unsigned long lastStockCheckMillis = 0;
unsigned long lastClimateMillis = 0;
unsigned long currentTimeMillis = 0;

WiFiSSLClient wifi;
HttpClient client = HttpClient(wifi, server, port);

MFRC522 rfid(SS_PIN, RST_PIN); // Instance of the class

MFRC522::MIFARE_Key key;

DHT dht(DHTPIN, DHTTYPE);
Servo myServo;

// Init array that will store new NUID
byte nuidPICC[4];

void setup() {
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);

  digitalWrite(LED_RED, HIGH);
  Serial.begin(9600);

  //initialize servo and his port
  myServo.attach(3);
  delay(100);
  myServo.write(90);

  SPI.begin(); // Init SPI bus
  rfid.PCD_Init(); // Init MFRC522

  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }

  Serial.println(F("This code scan the MIFARE Classsic NUID."));
  Serial.print(F("Using the following key:"));
  printHex(key.keyByte, MFRC522::MF_KEY_SIZE);

  // check for the WiFi module:
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    // don't continue
    while (true);
  }

  // attempt to connect to Wifi network:
  while (status != WL_CONNECTED) {
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    status = WiFi.begin(ssid, pass);

    // wait 10 seconds for connection:
    delay(10000);
  }
  Serial.println("Connected to wifi");
  printWifiStatus();

  //initialize temperature and humidity sensor
  dht.begin();

  //initialize buzzer
  pinMode(BUZZER_PIN, OUTPUT);
  digitalWrite(LED_RED, LOW);
  digitalWrite(LED_GREEN, HIGH);
  delay(2000);
  beep();
  digitalWrite(LED_GREEN, LOW);
}

void loop() {

  // attempt to connect to Wifi network:
  while (status != WL_CONNECTED) {
    digitalWrite(LED_RED, HIGH);
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    status = WiFi.begin(ssid, pass);

    // wait 10 seconds for connection:
    delay(10000);
  }
  digitalWrite(LED_RED, LOW);

  // Reset the loop if no new card present on the sensor/reader.
  // This saves the entire process when idle.
  if ( rfid.PICC_IsNewCardPresent() && rfid.PICC_ReadCardSerial())
  {
    digitalWrite(LED_RED, HIGH);
    beep();

    Serial.print(F("PICC type: "));
    MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak);
    Serial.println(rfid.PICC_GetTypeName(piccType));

    // Check is the PICC of Classic MIFARE type
    if (piccType != MFRC522::PICC_TYPE_MIFARE_MINI &&
        piccType != MFRC522::PICC_TYPE_MIFARE_1K &&
        piccType != MFRC522::PICC_TYPE_MIFARE_4K) {
      Serial.println(F("Your tag is not of type MIFARE Classic."));
      return;
    }

    Serial.println(F("The NUID tag is:"));
    Serial.print(F("In hex: "));
    printHex(rfid.uid.uidByte, rfid.uid.size);
    Serial.println();

      // Make a HTTP request:
      client.print("POST /officething/workinghours?rfid_uid=");
      unsigned long UID_unsigned;
      UID_unsigned =  rfid.uid.uidByte[0] << 24;
      UID_unsigned += rfid.uid.uidByte[1] << 16;
      UID_unsigned += rfid.uid.uidByte[2] <<  8;
      UID_unsigned += rfid.uid.uidByte[3];

      char id[16];
      sprintf(id, "%02x:%02x:%02x:%02x", rfid.uid.uidByte[0], rfid.uid.uidByte[1], rfid.uid.uidByte[2], rfid.uid.uidByte[3]);

      Serial.println("making POST request");
      String postData = "/officething/workinghours?rfid_uid=";
      postData += id;

      client.beginRequest();
      client.post(postData);
      client.sendHeader("Content-Type", "application/x-www-form-urlencoded");
      client.sendBasicAuth(clientuser, clientpassword);
      client.endRequest();

      // read the status code and body of the response
      int statusCode = client.responseStatusCode();
      String response = client.responseBody();

      Serial.print("Status code: ");
      Serial.println(statusCode);
      client.stop();


      if (statusCode == 201)
      {
        digitalWrite(LED_RED, LOW);
        digitalWrite(LED_GREEN, HIGH);
        delay(2000);
        beepWorkBegin();
        digitalWrite(LED_GREEN, LOW);
      }

      if (statusCode == 200)
      {
        digitalWrite(LED_RED, LOW);
        digitalWrite(LED_BLUE, HIGH);
        delay(2000);
        beepWorkFinished();
        digitalWrite(LED_BLUE, LOW);
      }

      // new card
      if (statusCode == 202)
      {
        digitalWrite(LED_RED, LOW);
        digitalWrite(LED_GREEN, HIGH);
        beepWorkBegin();
        digitalWrite(LED_GREEN, LOW);
        beepWorkFinished();
        digitalWrite(LED_GREEN, HIGH);
        beepWorkBegin();
        delay(2000);
        digitalWrite(LED_GREEN, LOW);
      }

      // card not allowed
      if (statusCode == 401)
      {
        digitalWrite(LED_RED, LOW);
        beepLow();
        digitalWrite(LED_RED, HIGH);
        beepLow();
        digitalWrite(LED_RED, LOW);
        beepLow();
        digitalWrite(LED_RED, HIGH);
      }
      digitalWrite(LED_RED, LOW);

    // Halt PICC
    rfid.PICC_HaltA();

    // Stop encryption on PCD
    rfid.PCD_StopCrypto1();
  }

  // RFID finished. Now stock

  currentTimeMillis = millis();

  if (currentTimeMillis - lastStockCheckMillis >= intervalStockMillis)
  {
    digitalWrite(LED_RED, HIGH);
    lastStockCheckMillis = currentTimeMillis;
    // check stock
    Serial.println("making GET request for stock");
    String getData = "/officething/stock";
    client.beginRequest();
    client.get(getData);
    client.sendHeader("Content-Type", "application/x-www-form-urlencoded");
    client.sendBasicAuth(clientuser, clientpassword);
    client.endRequest();

    // read the status code and body of the response
    int statusCodeGet = client.responseStatusCode();
    String responseGet = client.responseBody();

    Serial.print("Status code stock: ");
    Serial.println(statusCodeGet);
    Serial.print("Response stock: ");
    Serial.println(responseGet);
    client.stop();

    const size_t capacity  = JSON_OBJECT_SIZE(3); //root object has three elements
    DynamicJsonDocument doc(capacity );
    deserializeJson(doc, responseGet);

    float value = doc["stockValue"];
    int val = value * 1000;
    int pos = 90;

    //evalutate and map response vaulue to printed range
    if ((val > 0) & (val <= 10000))
    {
      if ((val > 0) && (val <= 2000))
      {
        pos = map(val, 0, 2000, 0, 18);
      } else if ((val > 2000) && (val <= 4000))
      {
        pos = map(val, 2000, 4000, 18, 45);
      } else if ((val > 4000) && (val <= 6000))
      {
        pos = map(val, 4000, 6000, 45, 81);
      } else if ((val > 6000) && (val <= 8000))
      {
        pos = map(val, 6000, 8000, 81, 126);
      }  else if ((val > 8000) && (val <= 10000))
      {
        pos = map(val, 8000, 10000, 126, 180);
      }

      Serial.print("servo pos would be: ");
      Serial.println(pos);

      // set interval to 1 hour
      intervalStockMillis = 3600000;

    } else
    {
      Serial.println("price value mapping error");

      // set check interval to 60 sec
      intervalStockMillis = 60000;
      //something went wrong requesting the value, try again in 1 minute
    }

    myServo.write(180 - pos);
    digitalWrite(LED_RED, LOW);
    return;
  }

  // Stock fiished. now update humidity and temperature
  if (currentTimeMillis - lastClimateMillis >= intervalClimateMillis)
  {
    digitalWrite(LED_RED, HIGH);
    lastClimateMillis = currentTimeMillis;

    float hum = dht.readHumidity();
    float temp = dht.readTemperature();

    Serial.println("making POST request for climate");
    String postDataClimate = "/officething/climate?temp=";
    postDataClimate += temp;
    postDataClimate += "&hum=";
    postDataClimate += hum;
    Serial.print("hum:");
    Serial.println(hum);
    Serial.print("temp:");
    Serial.println(temp);

    client.beginRequest();
    client.post(postDataClimate);
    client.sendHeader("Content-Type", "application/x-www-form-urlencoded");
    client.sendBasicAuth(clientuser, clientpassword);
    client.endRequest();

    // read the status code and body of the response
    int statusCodeClimate = client.responseStatusCode();

    Serial.print("Status code climate: ");
    Serial.println(statusCodeClimate);
    client.stop();

    // set interval to 15 minutes
  intervalClimateMillis = 900000 ;
  digitalWrite(LED_RED, LOW);
  }

}


/**
   Helper routine to dump a byte array as hex values to Serial.
*/
void printHex(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}

void printWifiStatus() {
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your board's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}

void beepWorkBegin() {

  //LOW sound
  for (int j = 0; j < 100; j++) { //make another sound
    digitalWrite(BUZZER_PIN, HIGH);
    delay(2); // delay 2ms
    digitalWrite(BUZZER_PIN, LOW);
    delay(2);
  }
  delay(50);

  //HIGH sound
  for (int i = 0; i < 80; i++)
  { // make a sound
    digitalWrite(BUZZER_PIN, HIGH); // send high signal to buzzer
    delay(1); // delay 1ms
    digitalWrite(BUZZER_PIN, LOW); // send low signal to buzzer
    delay(1);
  }

}

void beepLow() {

  //LOW sound
  for (int j = 0; j < 100; j++) { //make another sound
    digitalWrite(BUZZER_PIN, HIGH);
    delay(2); // delay 2ms
    digitalWrite(BUZZER_PIN, LOW);
    delay(2);
  }
  delay(50);
}

void beep() {
  //HIGH sound
  for (int i = 0; i < 80; i++)
  { // make a sound
    digitalWrite(BUZZER_PIN, HIGH); // send high signal to buzzer
    delay(1); // delay 1ms
    digitalWrite(BUZZER_PIN, LOW); // send low signal to buzzer
    delay(1);
  }

}

void beepWorkFinished() {

  //HIGH sound
  for (int i = 0; i < 80; i++)
  { // make a sound
    digitalWrite(BUZZER_PIN, HIGH); // send high signal to buzzer
    delay(1); // delay 1ms
    digitalWrite(BUZZER_PIN, LOW); // send low signal to buzzer
    delay(1);
  }
  delay(50);
  //LOW sound
  for (int j = 0; j < 100; j++) { //make another sound
    digitalWrite(BUZZER_PIN, HIGH);
    delay(2); // delay 2ms
    digitalWrite(BUZZER_PIN, LOW);
    delay(2);
  }

}
