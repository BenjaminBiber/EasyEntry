# Plan: EasyEntry Corporate Design — Konsistentes UI-Theme

## Kontext
Die App verwendet Material 3 + Compose, hat aber ein unvollständiges Theme:
- **Dynamic Color ist aktiv** → Auf Android 12+ überschreibt das System-Hintergrundbild alle Brandfarben
- **Color Scheme zu spärlich** → Nur 4–5 von 25+ M3-Farbrollen definiert; fehlende Rollen (z.B. `tertiaryContainer`, `surfaceContainer`) fallen auf M3-Lila-Defaults zurück
- **Launcher-Icon inkompatibel** → Hintergrund verwendet Blau (#1976D2), App nutzt Teal (#60ADC9)
- **Typografie unvollständig** → Nur 3 von 15 M3-Type-Scale-Styles definiert; Screens verwenden hardcodierte `fontSize = 13.sp` / `11.sp`
- **Kein Shape-Token** → Shapes fallen auf M3-Defaults zurück (small=4dp), statt einheitlicher abgerundeter Werte

## Änderungen (6 Dateien)

### 1. `app/src/main/java/com/easyentry/app/ui/theme/Color.kt`
- Alle fehlenden M3-Rollen ergänzen: `primaryContainer`, `onPrimaryContainer`, `secondaryContainer`, `onSecondaryContainer`, `tertiaryContainer`, `onTertiaryContainer`, `surfaceContainer`, `surfaceContainerHigh`, `outlineVariant`
- Eigene Dark-Mode-Farben: helleres Primary für dunklen Hintergrund (`#8DCDE6`), dunkle Surfaces (`#0E1416`)

### 2. `app/src/main/java/com/easyentry/app/ui/theme/Theme.kt`
- **`dynamicColor` auf `false` setzen** → Brandfarben immer aktiv, unabhängig vom System-Theme
- `lightColorScheme(...)` und `darkColorScheme(...)` mit allen neuen Farbwerten aus Color.kt befüllen
- Import von `LocalContext` und `Build` entfernen (nicht mehr nötig)
- `Shapes` an `MaterialTheme` weitergeben

### 3. `app/src/main/java/com/easyentry/app/ui/theme/Type.kt`
- Alle 15 M3-Type-Scale-Styles definieren: `displayLarge/Medium/Small`, `headlineLarge/Medium/Small`, `titleLarge/Medium/Small`, `bodyLarge/Medium/Small`, `labelLarge/Medium/Small`
- Konsistente `letterSpacing`- und `lineHeight`-Werte nach M3-Spec

### 4. `app/src/main/java/com/easyentry/app/ui/theme/Shape.kt` *(neu)*
- `Shapes`-Objekt mit leicht abgerundeteren Werten als M3-Default:
  - `small = RoundedCornerShape(8.dp)` (statt 4dp)
  - `medium = RoundedCornerShape(12.dp)` (gleich)
  - `large = RoundedCornerShape(16.dp)` (gleich)
  - `extraLarge = RoundedCornerShape(24.dp)` (für Bottom Sheets)

### 5. `app/src/main/res/drawable/ic_launcher_background.xml`
- Gradient-Farben von `#1976D2`/`#0D47A1` (Blau) auf `#3A9BB5`/`#1F7A95` (Brand-Teal) ändern

### 6. `app/src/main/java/com/easyentry/app/ui/home/DeviceCard.kt`
- `fontSize = 13.sp` → `style = MaterialTheme.typography.bodyMedium`
- `fontSize = 11.sp` → `style = MaterialTheme.typography.labelSmall`

## Verifikation
1. App bauen: `./gradlew assembleDebug`
2. Auf Android 12+-Emulator starten → TopAppBar muss Teal zeigen, kein Lila
3. Dark Mode umschalten → Screens sollen konsistent dunkel erscheinen
4. Launcher-Icon prüfen → Teal-Hintergrund statt Blau
