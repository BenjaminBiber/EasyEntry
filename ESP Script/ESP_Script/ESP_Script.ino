#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <ArduinoOTA.h>

// WLAN-Zugangsdaten, Geraetename und OTA-Passwort stehen in config.h (nicht eingecheckt).
#if __has_include("config.h")
#include "config.h"
#else
#error "config.h fehlt: config.example.h nach config.h kopieren und Werte eintragen"
#endif

#ifndef OTA_PASSWORD
#error "OTA_PASSWORD fehlt in config.h (siehe config.example.h)"
#endif

#define StopRELAY_PIN 21 // ESP32 pin GPIO15 connected to the IN pin of relay
#define UpRELAY_PIN 19 // ESP32 pin GPIO16 connected to the IN pin of relay
#define DownRELAY_PIN 3 // ESPß32 pin GPIO17 connected to the IN pin of relay
#define GateRelay_PIN 18

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

    // device.Name statt des Makros: sonst hatte handlePost() (Umbenennen über die App)
    // keinerlei sichtbare Wirkung. Hinweis: der Name liegt nur im RAM und fällt nach einem
    // Neustart auf DeviceName zurück -- für Persistenz wäre NVS/Preferences nötig.
    jsonDoc["name"] = device.Name.length() > 0 ? device.Name : String(DeviceName);
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

  // Antwort ZUERST senden, Relais danach schalten.
  // Begründung: server.handleClient() wird aus loop() aufgerufen und bedient strikt eine
  // Verbindung zur Zeit. Das delay(100) im Relais-Puls blockierte die Loop, während die
  // HTTP-Verbindung noch offen war -- in dieser Zeit wurde kein anderer Client bedient und
  // parallele Erreichbarkeitsprüfungen liefen ins Timeout. Der Antwortinhalt hängt nicht
  // vom Schaltvorgang ab, die Reihenfolge ist also gefahrlos tauschbar.
  StaticJsonDocument<200> responseDoc;
  responseDoc["ID"] = device.ID;
  responseDoc["Name"] = device.Name;
  responseDoc["Status"] = static_cast<int>(device.Status);

  String response;
  serializeJson(responseDoc, response);
  server.send(200, "application/json", response);

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

// Firmware-Updates über WLAN: der ESP erscheint in der Arduino IDE unter Tools -> Port als
// Netzwerk-Port, beim Upload wird OTA_PASSWORD abgefragt. Ohne Passwort könnte jeder im WLAN
// eine Firmware aufspielen, die das Tor öffnet.
void setupOta() {
  // mDNS-Hostnamen dürfen keine Leerzeichen enthalten ("Tor 1" -> "Tor-1").
  String hostname = DeviceName;
  hostname.replace(" ", "-");
  ArduinoOTA.setHostname(hostname.c_str());
  ArduinoOTA.setPassword(OTA_PASSWORD);
  ArduinoOTA.onStart([]() { Serial.println("OTA-Update gestartet"); });
  ArduinoOTA.onEnd([]() { Serial.println("OTA-Update fertig, starte neu"); });
  ArduinoOTA.onError([](ota_error_t error) {
    Serial.print("OTA-Fehler: ");
    Serial.println(error);
  });
  ArduinoOTA.begin();
}

void setup() {
  Serial.begin(115200);
  delay(3000);
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(StopRELAY_PIN, OUTPUT);
  pinMode(UpRELAY_PIN, OUTPUT);
  pinMode(DownRELAY_PIN, OUTPUT);
  // INPUT_PULLDOWN statt INPUT: der Pin wird in handleGet() per digitalRead als "IsOpen"
  // gemeldet. Ohne definierten Ruhepegel floatet er und der gemeldete Zustand kann Rauschen
  // sein -- das erklärt sprunghaft wechselnde "Offen"/"Geschlossen"-Anzeigen.
  pinMode(GateRelay_PIN, INPUT_PULLDOWN);


  digitalWrite(UpRELAY_PIN, HIGH);
  digitalWrite(DownRELAY_PIN, HIGH);
  digitalWrite(StopRELAY_PIN, HIGH);

  Serial.println();
  Serial.println("Powering on!");
  WiFi.setHostname(DeviceName);
  WiFi.mode(WIFI_STA);

  device.Name = DeviceName;

  printDeviceInfo();
  printNetworks();
  connectToNetwork();

  // WICHTIGSTE Änderung für die Erreichbarkeit: ohne dies läuft der ESP32 im
  // Default-Modem-Sleep und lauscht nur im DTIM-Intervall. Der Access Point puffert Unicast
  // dann bis zum nächsten Beacon (typisch ca. 300 ms) und verwirft Pakete auch ganz -- die
  // Ursache dafür, dass Geräte sporadisch als nicht erreichbar galten und erst nach
  // mehrfachem Neuladen erschienen. Kostet Strom (ca. 20 mA -> 100+ mA), bei netzbetriebener
  // Torsteuerung irrelevant.
  WiFi.setSleep(false);

  // Falls `curl -v http://<ip>/` KEINEN "Connection: close"-Header zeigt, hier zusätzlich
  // server.sendHeader("Connection", "close") in den Handlern setzen: dann hält der Server
  // Keep-Alive-Verbindungen, die die App-Seite als veraltet vorfindet.

  server.on("/", HTTP_GET, handleGet);
  server.on("/", HTTP_PUT, handlePut);
  server.on("/", HTTP_POST, handlePost);

  server.begin();
  setupOta();
}

void loop() {
  server.handleClient();
  ArduinoOTA.handle();
}
