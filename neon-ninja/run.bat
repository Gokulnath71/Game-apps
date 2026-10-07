@echo off
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
    echo.
    echo   Java JDK not found. Install Java 17 or newer to play Neon Ninja.
    echo.
    pause
    exit /b 1
)
echo   Compiling Neon Ninja...
javac -encoding UTF-8 NeonNinja.java
if errorlevel 1 (
    echo   Compile failed.
    pause
    exit /b 1
)
java NeonNinja
