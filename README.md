# Location Widget for Android 10+

Prosta aplikacja + widget na ekran główny, pokazująca:
- miejscowość
- kod pocztowy
- czas ostatniej aktualizacji

## Jak działa
- aplikacja uruchamia foreground service
- lokalizacja odświeża się co 10 sekund
- widget aktualizuje zapisane dane
- kliknięcie widgetu wymusza odświeżenie widoku

## Ważne
Na wielu radiach samochodowych / launcherach Android widgety i usługi w tle bywają agresywnie ubijane.
Wtedy trzeba:
1. dodać aplikację do wyjątków oszczędzania baterii
2. uruchomić aplikację i kliknąć **Start widgetu**
3. dodać widget na pulpit launchera

## Budowanie bez Android Studio
### GitHub Actions
1. wrzuć cały projekt na GitHuba
2. wejdź w **Actions**
3. uruchom workflow **Build Android APK**
4. pobierz artefakt `location-widget-debug-apk`
5. zainstaluj pobrany `app-debug.apk`

To działa bez Android Studio.
