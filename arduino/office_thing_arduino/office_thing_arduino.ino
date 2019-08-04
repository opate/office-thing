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

#include <Wire.h>
#include <Adafruit_Sensor.h>
#include "Adafruit_BME680.h"

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

/*
  I2C connection from Bosch BME680 to Arduino
  --------------------------------------------
  BME680 / MKR1010
  Vin / VCC (3.3V)
  GND / GND
  SCK / SCL
  SDI / SDA
*/

#define SS_PIN 7
#define RST_PIN 6

#define BUZZER_PIN 5

#define LED_RED A1
#define LED_GREEN A2
#define LED_BLUE A3

#define SEALEVELPRESSURE_HPA (1013.25)

Adafruit_BME680 bme; // I2C

char server[] = SECRET_SERVERHOST;
int port = 443;
// wifi status
int status = WL_IDLE_STATUS;
char ssid[] = SECRET_SSID;       // your network SSID (name)
char pass[] = SECRET_PASSWORD; // your network password (use for WPA, or use as key for WEP)
char clientuser[] = SECRET_CLIENTUSER; // basic auth credentials for REST endpoints
char clientpassword[] = SECRET_CLIENTPASSWORD; // basic auth credentials for REST endpoints

bool saved = false;

// bme680
float hum_weighting = 0.25; // so hum effect is 25% of the total air quality score
float gas_weighting = 0.75; // so gas effect is 75% of the total air quality score

float hum_score, gas_score;
float gas_reference = 250000;
float hum_reference = 40;
int   getgasreference_count = 0;

// timer for request
// initial values at startup

// 30 secs  = 30000
int intervalStockMillis = 30000;
// 20 secs = 20000 ms
int intervalClimateMillis = 20000;
// 30 minutes = 1.800.000 ms
// 3 hours = 10.800.000 ms
int gasCalibrationDelayMills = 10800000;

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

  // initialize BME680
  Serial.println(F("BME680 test"));

  if (!bme.begin()) {
    Serial.println("Could not find a valid BME680 sensor, check wiring!");
    while (1);
  }

  // Set up oversampling and filter initialization
  bme.setTemperatureOversampling(BME680_OS_8X);
  bme.setHumidityOversampling(BME680_OS_2X);
  bme.setPressureOversampling(BME680_OS_4X);
  bme.setIIRFilterSize(BME680_FILTER_SIZE_3);
  bme.setGasHeater(320, 150); // 320*C for 150 ms  

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

  if (currentTimeMillis - lastClimateMillis >= intervalClimateMillis) 
  {
    digitalWrite(LED_RED, HIGH);
    lastClimateMillis = currentTimeMillis;

    // legacy DHT22 climate chip
    float hum_dht22 = dht.readHumidity();
    float temp_dht22 = dht.readTemperature();
    Serial.println("DHT 22");
    Serial.print("temp: ");
    Serial.print(temp_dht22);
    Serial.print(" hum: ");
    Serial.println(hum_dht22);

    if (! bme.performReading()) {
      Serial.println("Failed to perform reading :(");
      return;
    }
    float temp = bme.temperature;
    float press = bme.pressure / 100.0;
    // empric measurements stated that we have to correct pressure by adding 16 hPa
    press = press + 16.0;
    float hum = bme.humidity;
    float gas = bme.gas_resistance / 1000.0;
    float iaq = calculateIaq();
  
//    float alt = bme.readAltitude(SEALEVELPRESSURE_HPA);

    Serial.println("BOSCH BME680");    
    Serial.print("temp: ");
    Serial.print(temp);
    Serial.print(" hum: ");
    Serial.println(hum);

    // building post request format: /climate?temp=12.1&hum=22.2&press=1000.32&gas=124.3&iaq=31
    String postDataClimate = "/officething/climate?temp=";
    postDataClimate += temp_dht22;
    postDataClimate += "&hum=";
    postDataClimate += hum_dht22;
    postDataClimate += "&press=";
    postDataClimate += press;
    
    if (currentTimeMillis > gasCalibrationDelayMills)
    {
      postDataClimate += "&gas=";
      postDataClimate += gas;
      postDataClimate += "&iaq=";
      postDataClimate += iaq;      
    }

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

float calculateIaq()
{

/*
 This software, the ideas and concepts is Copyright (c) David Bird 2018. All rights to this software are reserved.
 
 Any redistribution or reproduction of any part or all of the contents in any form is prohibited other than the following:
 1. You may print or download to a local hard disk extracts for your personal and non-commercial use only.
 2. You may copy the content to individual third parties for their personal use, but only if you acknowledge the author David Bird as the source of the material.
 3. You may not, except with my express written permission, distribute or commercially exploit the content.
 4. You may not transmit it or store it in any other website or other form of electronic retrieval system for commercial purposes.
 The above copyright ('as annotated') notice and this permission notice shall be included in all copies or substantial portions of the Software and where the
 software use is visible to an end-user.
 
 THE SOFTWARE IS PROVIDED "AS IS" FOR PRIVATE USE ONLY, IT IS NOT FOR COMMERCIAL USE IN WHOLE OR PART OR CONCEPT. FOR PERSONAL USE IT IS SUPPLIED WITHOUT WARRANTY 
 OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 IN NO EVENT SHALL THE AUTHOR OR COPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 See more at http://www.dsbird.org.uk
*/  
  
  //Calculate humidity contribution to IAQ index
  float current_humidity = bme.readHumidity();
  if (current_humidity >= 38 && current_humidity <= 42)
    hum_score = 0.25*100; // Humidity +/-5% around optimum 
  else
  { //sub-optimal
    if (current_humidity < 38) 
      hum_score = 0.25/hum_reference*current_humidity*100;
    else
    {
      hum_score = ((-0.25/(100-hum_reference)*current_humidity)+0.416666)*100;
    }
  }
  
  //Calculate gas contribution to IAQ index
  float gas_lower_limit = 5000;   // Bad air quality limit
  float gas_upper_limit = 50000;  // Good air quality limit 
  if (gas_reference > gas_upper_limit) gas_reference = gas_upper_limit; 
  if (gas_reference < gas_lower_limit) gas_reference = gas_lower_limit;
  gas_score = (0.75/(gas_upper_limit-gas_lower_limit)*gas_reference -(gas_lower_limit*(0.75/(gas_upper_limit-gas_lower_limit))))*100;
  
  //Combine results for the final IAQ index value (0-100% where 100% is good quality air)
  float air_quality_score = hum_score + gas_score;

  Serial.println("Air Quality = "+String(air_quality_score,1)+"% derived from 25% of Humidity reading and 75% of Gas reading - 100% is good quality air");
  Serial.println("Humidity element was : "+String(hum_score/100)+" of 0.25");
  Serial.println("     Gas element was : "+String(gas_score/100)+" of 0.75");
  if (bme.readGas() < 120000) Serial.println("***** Poor air quality *****");
  Serial.println();
  if ((getgasreference_count++)%10==0) GetGasReference(); 
  Serial.println(CalculateIAQ(air_quality_score));
  Serial.println("------------------------------------------------");
  float iaq = (100-air_quality_score)*5;
  return iaq;
}

void GetGasReference(){
  // Now run the sensor for a burn-in period, then use combination of relative humidity and gas resistance to estimate indoor air quality as a percentage.
  Serial.println("Getting a new gas reference value");
  int readings = 10;
  for (int i = 1; i <= readings; i++){ // read gas for 10 x 0.150mS = 1.5secs
    gas_reference += bme.readGas();
  }
  gas_reference = gas_reference / readings;
}

String CalculateIAQ(float score){
  String IAQ_text = "Air quality is ";
  score = (100-score)*5;
  if      (score >= 301)                  IAQ_text += "Hazardous";
  else if (score >= 201 && score <= 300 ) IAQ_text += "Very Unhealthy";
  else if (score >= 176 && score <= 200 ) IAQ_text += "Unhealthy";
  else if (score >= 151 && score <= 175 ) IAQ_text += "Unhealthy for Sensitive Groups";
  else if (score >=  51 && score <= 150 ) IAQ_text += "Moderate";
  else if (score >=  00 && score <=  50 ) IAQ_text += "Good";
  return IAQ_text;
}  
