@echo off
REM ChessMaster Pro - Windows Installer Script
REM This script helps install ChessMaster on your system

echo ===============================================
echo     ChessMaster Pro v1.0.0 - Installer
echo ===============================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 21 or higher from:
    echo https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Get Java version
for /f tokens^=3 %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VERSION=%%i
echo Detected Java version: %JAVA_VERSION%
echo.

REM Set installation directory
set "INSTALL_DIR=%LOCALAPPDATA%\ChessMaster"
echo Installation directory: %INSTALL_DIR%
echo.

REM Create installation directory
if not exist "%INSTALL_DIR%" (
    mkdir "%INSTALL_DIR%"
    echo Created installation directory.
) else (
    echo Installation directory already exists.
)

REM Copy files
echo.
echo Copying files...
xcopy /E /I /Y "bin" "%INSTALL_DIR%\bin" >nul
xcopy /E /I /Y "lib" "%INSTALL_DIR%\lib" >nul
echo Files copied successfully.

REM Create desktop shortcut
echo.
echo Creating desktop shortcut...
set "SHORTCUT=%USERPROFILE%\Desktop\ChessMaster.lnk"
set "TARGET=%INSTALL_DIR%\bin\ChessMaster.bat"

powershell -Command "$WshShell = New-Object -ComObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%SHORTCUT%'); $Shortcut.TargetPath = '%TARGET%'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%\bin'; $Shortcut.Description = 'ChessMaster Pro - Chess Game'; $Shortcut.Save()"

if exist "%SHORTCUT%" (
    echo Desktop shortcut created successfully.
) else (
    echo Warning: Could not create desktop shortcut.
)

REM Create Start Menu entry
echo.
echo Creating Start Menu entry...
set "START_MENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs\ChessMaster.lnk"

powershell -Command "$WshShell = New-Object -ComObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%START_MENU%'); $Shortcut.TargetPath = '%TARGET%'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%\bin'; $Shortcut.Description = 'ChessMaster Pro - Chess Game'; $Shortcut.Save()"

if exist "%START_MENU%" (
    echo Start Menu entry created successfully.
) else (
    echo Warning: Could not create Start Menu entry.
)

echo.
echo ===============================================
echo     Installation Complete!
echo ===============================================
echo.
echo ChessMaster has been installed to: %INSTALL_DIR%
echo.
echo You can now:
echo   1. Use the desktop shortcut
echo   2. Find it in the Start Menu
echo   3. Run from: %INSTALL_DIR%\bin\ChessMaster.bat
echo.
echo Enjoy playing chess!
echo.
pause
