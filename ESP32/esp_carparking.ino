#include <Arduino.h>
#include <ESP32Servo.h>
#include <WiFi.h>
#include <Firebase_ESP_Client.h>

// WiFi Credentials
#define WIFI_SSID "WIFI SSID"
#define WIFI_PASSWORD "WIFI PASSWORD"

// Firebase Credentials
#define FIREBASE_HOST "PROJECT-ID"
#define FIREBASE_AUTH "FIREBASE SECRET"

#define IR_SENSOR_PIN_A1 13  
#define IR_SENSOR_PIN_A2 27  
#define IR_SENSOR_PIN_A3 14  
#define SERVO_PIN 32        

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

//only 3 parking spots A1,A2,A3
const int NUM_SPOTS = 3;


bool vehiclePresent[NUM_SPOTS] = {false};
Servo gateServo;
int gateAngle = 0;

void setup() {
  Serial.begin(115200);
  delay(1500);


  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nConnected to WiFi");

  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);


  pinMode(IR_SENSOR_PIN_A1, INPUT);
  pinMode(IR_SENSOR_PIN_A2, INPUT);
  pinMode(IR_SENSOR_PIN_A3, INPUT);

  gateServo.attach(SERVO_PIN);
  gateServo.write(gateAngle);

  Serial.println("System Initialized. Monitoring Parking Slots...");
}

void loop() {
  handleParkingSpot(0, IR_SENSOR_PIN_A1, "A1"); 
  handleParkingSpot(1, IR_SENSOR_PIN_A2, "A2"); 
  handleParkingSpot(2, IR_SENSOR_PIN_A3, "A3"); 
  
  updateGateStatus();
  displaySlotStatus();
  delay(500);
}

void handleParkingSpot(int spotIndex, int sensorPin, const char* spotName) {
  bool isOccupied = digitalRead(sensorPin) == LOW;

  if (isOccupied != vehiclePresent[spotIndex]) {
    vehiclePresent[spotIndex] = isOccupied;
    Serial.printf("%s: %s\n", spotName, isOccupied ? "Occupied" : "Available");
    sendDataToFirebase(spotName, isOccupied);
  }
}

void updateGateStatus() {
  bool isVacant = false;
  for (int i = 0; i < NUM_SPOTS; i++) {
    if (!vehiclePresent[i]) {
      isVacant = true;
      break;
    }
  }
  
  if (isVacant && gateAngle == 0) {
    gateAngle = 90;
    gateServo.write(gateAngle);
    Serial.println("Gate Opened");
  } else if (!isVacant && gateAngle == 90) {
    gateAngle = 0;
    gateServo.write(gateAngle);
    Serial.println("Gate Closed");
  }
}

void sendDataToFirebase(const char* spot, bool occupied) {
  String path = "/parking_spots/" + String(spot) + "/occupied";
  if (Firebase.RTDB.setBool(&fbdo, path.c_str(), occupied)) {
    Serial.printf("Firebase Updated: %s = %s\n", spot, occupied ? "true" : "false");
  } else {
    Serial.printf("Firebase Error (%s): %s\n", spot, fbdo.errorReason().c_str());
  }
}

void displaySlotStatus() {
  Serial.println("Current Status:");
  Serial.printf("A1: %s\n", vehiclePresent[0] ? "Occupied" : "Available");
  Serial.printf("A2: %s\n", vehiclePresent[1] ? "Occupied" : "Available");
  Serial.printf("A3: %s\n", vehiclePresent[2] ? "Occupied" : "Available");
  Serial.println("------------------");
}