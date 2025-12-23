@echo off
chcp 65001
cd /d "E:\local_lib\coding\播放器"
echo Cleaning project...
gradle clean
echo.
echo Assembling debug APK...
gradle assembleDebug
echo.
echo Build complete.
pause

