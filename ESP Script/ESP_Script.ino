#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>

// Netzwerkanmeldeinformationen
#define WIFI_SSID "SSID"
#define WIFI_PASSWORD "Password"
#define StopRELAY_PIN 21 // ESP32 pin GPIO15 connected to the IN pin of relay
#define UpRELAY_PIN 19 // ESP32 pin GPIO16 connected to the IN pin of relay
#define DownRELAY_PIN 3 // ESPß32 pin GPIO17 connected to the IN pin of relay
#define GateRelay_PIN 18
#define DeviceName "DeviceName"

enum DeviceStatus {
  opened = 1,
  closed = 2,
  neutral = 3
};

// Device-Struktur
class Device {
public:
  int ID;
  String Name;
  DeviceStatus Status;
};

// Globales Gerät
Device device = {1, "", neutral};

WebServer server(80);

// Funktion zur Statusausgabe des WiFi-Verbindungsstatus
String get_wifi_status(int status) {
  switch (status) {
    case WL_IDLE_STATUS: return "WL_IDLE_STATUS";
    case WL_SCAN_COMPLETED: return "WL_SCAN_COMPLETED";
    case WL_NO_SSID_AVAIL: return "WL_NO_SSID_AVAIL";
    case WL_CONNECT_FAILED: return "WL_CONNECT_FAILED";
    case WL_CONNECTION_LOST: return "WL_CONNECTION_LOST";
    case WL_CONNECTED: return "WL_CONNECTED";
    case WL_DISCONNECTED: return "WL_DISCONNECTED";
    default: return "Unknown";
  }
}

// Funktion zur Ausgabe des Verschlüsselungstyps
String get_encryption_type(int type) {
  switch (type) {
    case 5: return "WEP";
    case 2: return "WPA / PSK";
    case 4: return "WPA2 / PSK";
    case 7: return "None (i.e. open network)";
    case 8: return "WPA / WPA2 / PSK (aka Auto)";
    default: return "???";
  }
}

// Funktion zur Ausgabe der verfügbaren Netzwerke
void printNetworks() {
  Serial.println();
  Serial.println("**********");
  Serial.println("Scanning for networks...");
  int numSsid = WiFi.scanNetworks();
  if (numSsid == -1) {
    Serial.println("Couldn't get a wifi connection");
  } else if (numSsid == 0) {
    Serial.println("No networks found.");
  } else {
    for (int i = 0; i < numSsid; i++) {
      String thisSSID = WiFi.SSID(i);
      if (thisSSID == WIFI_SSID) {
        Serial.print(">>> ");
      } else {
        Serial.print("    ");
      }
      Serial.print(i);
      Serial.print(") ");
      Serial.print(thisSSID);
      Serial.print("\t\t\tSignal: ");
      Serial.print(WiFi.RSSI(i));
      Serial.print(" dBm");
      Serial.print("\t\t\tEncryption: ");
      Serial.print(get_encryption_type(WiFi.encryptionType(i)));
      Serial.print(" - #");
      Serial.print(WiFi.encryptionType(i));
      if (thisSSID == WIFI_SSID) {
        Serial.println(" <<<");
      } else {
        Serial.println("    ");
      }
    }
  }
  Serial.println("**********");
}

// Funktion zur Ausgabe der Geräteinformationen
void printDeviceInfo() {
  Serial.println();
  Serial.print("HOST NAME: ");
  Serial.println(DeviceName);
  Serial.print("MAC Address:  ");
  Serial.println(WiFi.macAddress());
}

// Funktion zur Herstellung der WiFi-Verbindung
void connectToNetwork() {
  #define TIMEOUT 100
  #define TRY_TO_RESTART false
  int status = WL_IDLE_STATUS;
  Serial.println();
  Serial.print("Connecting to ");
  Serial.print(WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  int i = TIMEOUT;
  while (status != WL_CONNECTED) {
    i--;
    delay(200);
    Serial.print(".");
    if (status != WiFi.status()) {
      status = WiFi.status();
      Serial.println();
      Serial.print(get_wifi_status(status));
    }
    if (i == 0) {
      if (TRY_TO_RESTART) {
        Serial.println("Connection failed. Restarting...");
        ESP.restart();
      }
      Serial.println();
      i = TIMEOUT;
    }
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
    digitalWrite(LED_BUILTIN, HIGH);
  Serial.println();
}

// GET-Handler: Gibt das Gerät zurück
void handleGet() {
    StaticJsonDocument<200> jsonDoc;

    jsonDoc["name"] = DeviceName;
    jsonDoc["IsOpen"] = digitalRead(GateRelay_PIN) == HIGH;

    // Serialisiere das Dokument in eine Zeichenkette
    String response;
    serializeJson(jsonDoc, response);

    // Sende die Antwort an den Client
    server.send(200, "application/json", response);
}


// PUT-Handler: Aktualisiert den Status des Geräts
void handlePut() {
  if (!server.hasArg("plain")) {
    server.send(400, "text/plain", "No message received");
    return;
  }

  String body = server.arg("plain");
  StaticJsonDocument<200> jsonDoc;
  DeserializationError error = deserializeJson(jsonDoc, body);

  if (error) {
    server.send(400, "text/plain", "Invalid JSON");
    return;
  }

  int newStatus = jsonDoc["Status"].as<int>();
  device.Status = static_cast<DeviceStatus>(newStatus);

  if (device.Status == opened) {
    digitalWrite(UpRELAY_PIN, LOW);
    delay(100);
    digitalWrite(UpRELAY_PIN, HIGH);
  } else if (device.Status == closed) {
    digitalWrite(DownRELAY_PIN, LOW);
    delay(100);
    digitalWrite(DownRELAY_PIN, HIGH);
  } else {
    digitalWrite(StopRELAY_PIN, HIGH);
    delay(100);
    digitalWrite(StopRELAY_PIN, LOW);
  }

  StaticJsonDocument<200> responseDoc;
  responseDoc["ID"] = device.ID;
  responseDoc["Name"] = device.Name;
  responseDoc["Status"] = static_cast<int>(device.Status);

  String response;
  serializeJson(responseDoc, response);
  server.send(200, "application/json", response);
}

// POST-Handler: Ändert den Namen des Geräts
void handlePost() {
  if (!server.hasArg("plain")) {
    server.send(400, "text/plain", "No message received");
    return;
  }

  String body = server.arg("plain");
  StaticJsonDocument<200> jsonDoc;
  DeserializationError error = deserializeJson(jsonDoc, body);

  if (error) {
    server.send(400, "text/plain", "Invalid JSON");
    return;
  }

  String newName = jsonDoc["Name"].as<String>();
  device.Name = newName;

  server.send(200, "text/plain", "Device name updated successfully");
}

void setup() {
  Serial.begin(115200);
  delay(3000);
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(StopRELAY_PIN, OUTPUT);
  pinMode(UpRELAY_PIN, OUTPUT);
  pinMode(DownRELAY_PIN, OUTPUT);
  pinMode(GateRelay_PIN, INPUT);


  digitalWrite(UpRELAY_PIN, HIGH);
  digitalWrite(DownRELAY_PIN, HIGH);
  digitalWrite(StopRELAY_PIN, HIGH);

  Serial.println();
  Serial.println("Powering on!");
  WiFi.setHostname(DeviceName);
  WiFi.mode(WIFI_STA);

  printDeviceInfo();
  printNetworks();
  connectToNetwork();

  server.on("/", HTTP_GET, handleGet);
  server.on("/", HTTP_PUT, handlePut);
  server.on("/", HTTP_POST, handlePost);

  server.begin();
}

void loop() {
  server.handleClient();
}
