@echo off
title Blacksite Breach 3D FPS (Java 21 & jMonkeyEngine)
echo ========================================================
echo  Starting Blacksite Breach 3D FPS Prototype
echo  Engine: jMonkeyEngine 3.7.0-stable | Runtime: Java 21
echo ========================================================

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set MVN_CMD=mvn
) else (
    set MVN_CMD="C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"
)

echo Using Maven: %MVN_CMD%
%MVN_CMD% compile exec:java
pause
