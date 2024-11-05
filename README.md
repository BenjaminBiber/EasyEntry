# EasyEntry
Eine DIY-Smart-Home-Lösung für die Steuerung von Industrie- und Garagentoren. Jedes Tor wird durch einen ESP32 und eine .NET MAUI-basierte Anwendung gesteuert. Das Projekt kann auch ohne Anfertigung eines PCBs erfolgreich umgesetzt werden, jedoch ist es für die Wartbarkeit und die Verkabelung einfacher, die Variante mit PCB zu verwenden. Durch das Auslagern der Relais auf separate Boards konnten die Abmessungen des Boards unter der 10 cm x 10 cm-Grenze gehalten werden, sodass man die Boards über PCBWay für 5 € pro Stück bestellen kann. Gerber-Dateien wurden im Repository bereitgestellt.

Das Gehäuse ist eine abgewandelte Version der [Rugged Parametric Box](https://www.printables.com/model/258431-rugged-box-parametric?lang=de) mit einer Breite von 170 mm und einer Höhe von 110 mm.

![first Image](https://github.com/BenjaminBiber/EasyEntry/blob/main/Pictures/img1.jpg?raw=true)

## Benötigtes Material
- [Ein-Kanal-Relais](https://www.amazon.de/dp/B0CSJQZ89V?ref=cm_sw_r_cso_wa_apan_dp_JMC89PAT6X1BANEV0JAZ&ref_=cm_sw_r_cso_wa_apan_dp_JMC89PAT6X1BANEV0JAZ&social_share=cm_sw_r_cso_wa_apan_dp_JMC89PAT6X1BANEV0JAZ&starsLeft=1&skipTwisterOG=1&th=1) (3 Stück pro Board)
- [24V-Spannungswandler](https://www.amazon.de/dp/B0CGVMRQXB?ref=cm_sw_r_cso_wa_apan_dp_TCYJM282NCX5VMQ1HK86&ref_=cm_sw_r_cso_wa_apan_dp_TCYJM282NCX5VMQ1HK86&social_share=cm_sw_r_cso_wa_apan_dp_TCYJM282NCX5VMQ1HK86&starsLeft=1&skipTwisterOG=1) (1 Stück pro Board)
- [ESP32-Board mit USB-C (DOIT ESP32 DEVKIT V1)](https://www.amazon.de/dp/B0D9LDFJ9H?ref=ppx_yo2ov_dt_b_fed_asin_title&th=1) (1 Stück pro Board)
- [Lötpins Male / Female](https://www.amazon.de/dp/B07DBY753C?ref=ppx_yo2ov_dt_b_fed_asin_title) 
- [PCB-Terminals](https://www.amazon.de/dp/B071GMT7ZZ?ref=ppx_yo2ov_dt_b_fed_asin_title) (5 Stück pro Board)
- Lötzinn
- Kabel (0,75 mm² - 1 mm² reichen aus)
- M3-Schrauben

## Verwendung

### Tor
Grundsätzlich kann das Modul für jedes Industrie- und Garagentor mit 1 - 3 Tastern verwendet werden. Bei den Toren, die ich umgerüstet habe, war bereits ein 24V-DC-Anschluss (links unten) vorhanden, drei Relais-Anschlüsse für die Steuerung der einzelnen Torfunktionen (links oben) und ein Torstands-Relais (rechts oben), das so programmiert werden konnte, dass es nur im geschlossenen Zustand ein NO-Signal ausgibt und somit als Torstandssensor verwendet werden konnte.

![second Image](https://github.com/BenjaminBiber/EasyEntry/blob/main/Pictures/img3.jpg?raw=true)

### Schaltplatine
Für die Schaltplatine müssen die einzelnen Komponenten auf das PCB gelötet werden. Anschließend wird mithilfe der Arduino-IDE das ESP-Script auf den ESP32 geladen. Für die Einrichtung der App wird empfohlen, den ESP nach dem Flashen erneut mit dem PC zu verbinden, um die IP-Adresse über den seriellen Monitor auszulesen.

![third Image](https://github.com/BenjaminBiber/EasyEntry/blob/main/Pictures/img5.jpg?raw=true)

### App
Für die App muss das MAUI-Projekt für das jeweilige Betriebssystem veröffentlicht und dann auf dem Mobilgerät installiert werden. Danach kann in der App über das Plus-Symbol in der App-Leiste ein neues Gerät hinzugefügt werden (die vorher ausgelesene IP-Adresse wird hierfür benötigt).

![fourth Image](https://github.com/BenjaminBiber/EasyEntry/blob/main/Pictures/img8.png?raw=true)

Nun kann das Tor über die App auf, zu oder gestoppt werden. Über das Einstellungssymbol kann festgelegt werden, ob bei jedem API-Aufruf eine Snackbar erscheinen soll. Das Reload-Symbol dient zum Neuladen der gesamten Seite.

![fifth Image](https://github.com/BenjaminBiber/EasyEntry/blob/main/Pictures/img7.png?raw=true)

Falls mehrere Geräte gruppiert werden sollen, kann dies über die Gruppenverwaltung erfolgen, wo Geräte auch gelöscht werden können.

![sixth Image](https://github.com/BenjaminBiber/EasyEntry/blob/main/Pictures/img9.png?raw=true)
