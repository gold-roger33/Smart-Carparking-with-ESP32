# ESP32 Firmware for Smart Car Parking System

code for the ESP32 module used in the Smart Car Parking system. The ESP32 communicates with the Android app via Firebase Realtime Database to manage and monitor parking spots.


## 🔧 ESP32 Configuration

Before uploading the `esp_carparking.ino` file to your ESP32, make sure to configure your Wi-Fi and Firebase credentials:

Open `ESP32/esp_carparking.ino` and update the following lines:

```cpp
#define WIFI_SSID "your_wifi_ssid"
#define WIFI_PASSWORD "your_wifi_password"

#define FIREBASE_HOST "your-project-id.firebaseio.com"
#define FIREBASE_AUTH "your_firebase_database_secret"
