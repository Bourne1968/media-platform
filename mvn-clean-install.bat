@echo off
echo Cleaning and rebuilding project...
call mvn clean install -DskipTests
echo.
echo Done! Please restart your application.
