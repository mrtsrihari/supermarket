@echo off
REM Compile and run the Smart Supermarket Billing System with MySQL
cd /d "%~dp0"

if not exist "out" mkdir out

echo Compiling Java source files...
javac -cp "lib/*" -d out src/supermarket/model/*.java src/supermarket/exception/*.java src/supermarket/db/*.java src/supermarket/dao/*.java src/supermarket/service/*.java src/supermarket/app/*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Starting Supermarket Billing System...
java -cp "out;lib/*" supermarket.app.SupermarketBillingSystem
pause

