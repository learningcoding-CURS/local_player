@echo off
chcp 65001 >nul
echo ========================================
echo 本地播放器 - 清理并编译
echo ========================================
echo.

echo [1/3] 清理项目...
call gradle clean
if %ERRORLEVEL% NEQ 0 (
    echo 清理失败！
    pause
    exit /b 1
)

echo.
echo [2/3] 编译 Debug 版本...
call gradle assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo 编译失败！请查看错误信息
    pause
    exit /b 1
)

echo.
echo [3/3] 编译完成！
echo APK 位置: app\build\outputs\apk\debug\app-debug.apk
echo.
echo 下一步：
echo 1. 手动安装 APK 到手机
echo 2. 运行 get_logs.bat 查看日志
echo 3. 启动应用
echo.
pause

