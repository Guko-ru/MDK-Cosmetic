@echo off
setlocal
cd /d "%~dp0"
call gradlew.bat --no-daemon clean prepareRelease > BUILD_LOG.txt 2>&1
type BUILD_LOG.txt
echo.
echo Release JAR: build\release\
pause
