@echo off
setlocal
if exist "%~dp0..\..\.tools\jdk-25.0.5+8\bin\java.exe" set "JAVA_HOME=%~dp0..\..\.tools\jdk-25.0.5+8"
cd /d "%~dp0"
call gradlew.bat --no-daemon clean prepareRelease > BUILD_LOG.txt 2>&1
type BUILD_LOG.txt
echo.
echo Release JAR: build\release\
pause
