@echo off
chcp 65001
cd /d "%~dp0"
echo Cleaning project...
call gradlew.bat clean
echo.
echo Assembling debug APK...
call gradlew.bat assembleDebug
echo.
echo Build complete.
pause

