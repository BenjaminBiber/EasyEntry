// Vorlage fuer config.h: kopieren, Werte eintragen. config.h ist in .gitignore und wird
// nie eingecheckt -- dort stehen die echten WLAN-Zugangsdaten.
#pragma once

#define WIFI_SSID "SSID"
#define WIFI_PASSWORD "Password"
// Hostname im WLAN und Name, den die App beim Hinzufuegen uebernimmt. Pro Geraet eindeutig.
#define DeviceName "DeviceName"
// Passwort fuer Firmware-Updates ueber WLAN (OTA); die Arduino IDE fragt beim Upload danach.
#define OTA_PASSWORD "OtaPassword"
