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
  SPI connection from RFID module to Arduino
  -------------------------------------------
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
// wifi status
int status = WL_IDLE_STATUS;
char ssid[] = SECRET_SSID;       // your network SSID (name)
char pass[] = SECRET_PASSWORD; // your network password (use for WPA, or use as key for WEP)
char clientuser[] = SECRET_CLIENTUSER; // basic auth credentials for REST endpoints
char clientpassword[] = SECRET_CLIENTPASSWORD; // basic auth credentials for REST endpoints

bool saved = false;

// timer for request
// initial values at startup

// 30 secs in millis = 30000
int intervalStockMillis = 30000;
// 20 secs min = 20000 ms
int intervalClimateMillis = 20000;

unsigned long lastStockCheckMillis = 0;
unsigned long lastClimateMillis = 0;
unsigned long currentTimeMillis = 0;

WiFiSSLClient wifi;
HttpClient client = HttpClient(wifi, server, port);

// RFID classes
MFRC522 rfid(SS_PIN, RST_PIN); // Instance of the class
MFRC522::MIFARE_Key key;

DHT dht(DHTPIN, DHTTYPE);

Servo myServo;

// Init array that will store new NUID
byte nuidPICC[4];

// Software reset method
void (*Reset_)(void) = 0;

void setup() {

  // initialize multicolor LED
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);

  digitalWrite(LED_RED, HIGH);

  //initialize serial output
  Serial.begin(9600);

  //initialize servo and his port
  myServo.attach(3);
  delay(100);
  myServo.write(90);

  // initialize RFID module
  SPI.begin(); // Init SPI bus
  rfid.PCD_Init(); // Init MFRC522
  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }
  Serial.println(F("Scanning the MIFARE Classsic NUID."));
  Serial.print(F("Using the following key:"));
  printHex(key.keyByte, MFRC522::MF_KEY_SIZE);

  // check wifi module
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    // don't continue
    while (true)
      ;
  }
  // connect to wifi
  checkWifiConnection();

  //initialize temperature and humidity sensor
  dht.begin();

  //initialize buzzer
  pinMode(BUZZER_PIN, OUTPUT);

  // everything ok, setup finished
  digitalWrite(LED_RED, LOW);
  digitalWrite(LED_GREEN, HIGH);
  delay(2000);
  beep();
  digitalWrite(LED_GREEN, LOW);
}

void loop() {

  checkWifiConnection();

  // Step 1 of 3.
  // Check if new RFID card is present

  if (rfid.PICC_IsNewCardPresent() && rfid.PICC_ReadCardSerial()) {
    digitalWrite(LED_RED, HIGH);
    beep();

    Serial.print(F("PICC type: "));
    MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak);
    Serial.println(rfid.PICC_GetTypeName(piccType));

    // Check is the PICC of Classic MIFARE type
    if (piccType != MFRC522::PICC_TYPE_MIFARE_MINI
        && piccType != MFRC522::PICC_TYPE_MIFARE_1K
        && piccType != MFRC522::PICC_TYPE_MIFARE_4K) {
      Serial.println(F("Your tag is not of type MIFARE Classic."));
      return;
    }

    Serial.print(F("The NUID tag is in hex: "));
    printHex(rfid.uid.uidByte, rfid.uid.size);
    Serial.println();

    unsigned long UID_unsigned;
    UID_unsigned = rfid.uid.uidByte[0] << 24;
    UID_unsigned += rfid.uid.uidByte[1] << 16;
    UID_unsigned += rfid.uid.uidByte[2] << 8;
    UID_unsigned += rfid.uid.uidByte[3];

    char id[16];
    sprintf(id, "%02x:%02x:%02x:%02x", rfid.uid.uidByte[0],
            rfid.uid.uidByte[1], rfid.uid.uidByte[2], rfid.uid.uidByte[3]);

    String postData = "/officething/workevent?rfid_uid=";
    postData += id;
    
    Serial.println("Making POST request for RFID event:");
    Serial.println(postData);
    
    client.beginRequest();
    client.post(postData);
    client.sendHeader("Content-Type", "application/x-www-form-urlencoded");
    client.sendBasicAuth(clientuser, clientpassword);
    client.endRequest();

    int statusCode = client.responseStatusCode();
    String response = client.responseBody();

    Serial.print("Status code: ");
    Serial.println(statusCode);
    client.stop();

    // rfid card accepted by server, workperiod begin
    if (statusCode == 201) {
      digitalWrite(LED_RED, LOW);
      digitalWrite(LED_GREEN, HIGH);
      delay(2000);
      beepWorkBegin();
      digitalWrite(LED_GREEN, LOW);
    }

    // rfid card accepted by server, workperiod finish
    if (statusCode == 200) {
      digitalWrite(LED_RED, LOW);
      digitalWrite(LED_BLUE, HIGH);
      delay(2000);
      beepWorkFinished();
      digitalWrite(LED_BLUE, LOW);
    }

    // new card recognized by server and new user has been created automatically, workperiod begin
    if (statusCode == 202) {
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

    // card not allowed by server
    if (statusCode == 401) {
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

  // Step 2 of 3.
  // Request current stock value

  currentTimeMillis = millis();

  if (currentTimeMillis - lastStockCheckMillis >= intervalStockMillis) {

    digitalWrite(LED_RED, HIGH);

    lastStockCheckMillis = currentTimeMillis;

    String getData = "/officething/stock";

    Serial.println("Making GET request for stock:");
    Serial.println(getData);    

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

    const size_t capacity = JSON_OBJECT_SIZE(3); //root object has three elements
    DynamicJsonDocument doc(capacity);
    deserializeJson(doc, responseGet);

    float value = doc["stockValue"];
    int val = value * 1000;

    // default position, 12 o'clock position
    int pos = 90;

    //evalutate and map response stock value to printed range
    if ((val > 0) & (val <= 10000)) {
      if ((val > 0) && (val <= 2000)) {
        pos = map(val, 0, 2000, 0, 18);
      } else if ((val > 2000) && (val <= 4000)) {
        pos = map(val, 2000, 4000, 18, 45);
      } else if ((val > 4000) && (val <= 6000)) {
        pos = map(val, 4000, 6000, 45, 81);
      } else if ((val > 6000) && (val <= 8000)) {
        pos = map(val, 6000, 8000, 81, 126);
      } else if ((val > 8000) && (val <= 10000)) {
        pos = map(val, 8000, 10000, 126, 180);
      }

      Serial.print("servo position: ");
      Serial.println(pos);

      //everything ok, set interval (back) to 1 hour
      intervalStockMillis = 3600000;

    } else {
      Serial.println("Stock value mapping error. Try again in 1 minute.");

      // set check interval to 60 sec
      intervalStockMillis = 60000;
      //something went wrong requesting the value, try again in 1 minute
    }

    myServo.write(180 - pos);
    digitalWrite(LED_RED, LOW);
    return;
  }

  // Step 3 of 3
  // Send current climate informations.

  if (currentTimeMillis - lastClimateMillis >= intervalClimateMillis) {
    digitalWrite(LED_RED, HIGH);
    lastClimateMillis = currentTimeMillis;

    float hum = dht.readHumidity();
    float temp = dht.readTemperature();

    String postDataClimate = "/officething/climate?temp=";
    postDataClimate += temp;
    postDataClimate += "&hum=";
    postDataClimate += hum;

    Serial.println("Making POST request for climate:");
    Serial.println(postDataClimate);

    client.beginRequest();
    client.post(postDataClimate);
    client.sendHeader("Content-Type", "application/x-www-form-urlencoded");
    client.sendBasicAuth(clientuser, clientpassword);
    client.endRequest();

    // read servers response status code
    int statusCodeClimate = client.responseStatusCode();

    Serial.print("Servers response status code for climate POST: ");
    Serial.println(statusCodeClimate);
    client.stop();

    // rfid card accepted, workperiod finish
    if (statusCodeClimate == 201) {
      // set interval to 15 minutes
      intervalClimateMillis = 900000;
    } else {
      Serial.println("Servers response status code for climate POST not 201. Try again in 1 minute.");
      // set interval to 60 seconds
      intervalClimateMillis = 60000;
    }

    digitalWrite(LED_RED, LOW);
  }

}

// LOOP finished.
// Helper methods.

void checkWifiConnection() {

  int wifiTrials = 0;
  
  while (status != WL_CONNECTED) {
    digitalWrite(LED_RED, HIGH);
    wifiTrials++;
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    // 60 trials x 10 seconds = 10 minutes
    if (wifiTrials > 60) {
      Serial.print("Tried 10 minutes to connect to WIFI. Restart in 10 seconds!");
      delay(10000);
      Reset_();
    }
    status = WiFi.begin(ssid, pass);

    // wait 10 seconds for connection:
    delay(10000);
  }

  if (wifiTrials > 0) {
    digitalWrite(LED_RED, LOW);
    Serial.println("Connected to wifi");
    printWifiStatus();
  }
}

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

  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

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
  for (int i = 0; i < 80; i++) { // make a sound
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
  for (int i = 0; i < 80; i++) { // make a sound
    digitalWrite(BUZZER_PIN, HIGH); // send high signal to buzzer
    delay(1); // delay 1ms
    digitalWrite(BUZZER_PIN, LOW); // send low signal to buzzer
    delay(1);
  }
}

void beepWorkFinished() {

  //HIGH sound
  for (int i = 0; i < 80; i++) { // make a sound
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
