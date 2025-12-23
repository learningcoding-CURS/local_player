@echo off
echo ========================================
echo 本地播放器 - 日志获取工具
echo ========================================
echo.

echo 正在清空旧日志...
adb logcat -c

echo 启动日志监控（按 Ctrl+C 停止）...
echo 请在另一个终端运行应用，或手动启动应用
echo.
echo ========================================
echo 日志输出开始：
echo ========================================
echo.

adb logcat -s MainActivity:D AppNavigation:D AppDatabase:D ExoPlayerManager:D LibraryViewModel:D AndroidRuntime:E

pause

