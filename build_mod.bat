@echo off
setlocal EnableExtensions
cd /d "%~dp0"
set "LOG=%~dp0BUILD_LOG.txt"
set "GRADLE_VER=9.8.2"
set "GRADLE_EXE=%~dp0.tools\gradle-%GRADLE_VER%\bin\gradle.bat"
set "BOOTSTRAP=%~dp0.tools\gradle-%GRADLE_VER%-bin.zip"

> "%LOG%" echo MDK Cosmetic - build log
>>"%LOG%" echo Current dir: %CD%

echo ========================================================
echo   MDK Cosmetic - Forge 26.1.2 - Build

echo ========================================================
echo.
echo Log: "%LOG%"
echo.

where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: Java was not found. Install JDK 25 and set JAVA_HOME/PATH.
  >>"%LOG%" echo ERROR: Java was not found.
  goto :failure
)
java -version 2>"%TEMP%\mdk-java-check-%RANDOM%.txt"
java -version 2>>"%LOG%"

if exist "%GRADLE_EXE%" goto :build
where gradle >nul 2>nul
if not errorlevel 1 (
  set "GRADLE_EXE=gradle"
  goto :build
)

if not exist "%~dp0.tools" mkdir "%~dp0.tools"
echo Gradle was not found. Downloading Gradle %GRADLE_VER% from gradle.org ...
echo The first build requires an Internet connection.
>>"%LOG%" echo Downloading Gradle %GRADLE_VER% ...

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $version='9.8.2'; $base='%~dp0.tools'; $archive=Join-Path $base ('gradle-'+$version+'-bin.zip'); if (-not (Test-Path $archive)) { Invoke-WebRequest -Uri ('https://services.gradle.org/distributions/gradle-'+$version+'-bin.zip') -OutFile $archive }; Expand-Archive -LiteralPath $archive -DestinationPath $base -Force; if (-not (Test-Path (Join-Path $base ('gradle-'+$version+'\bin\gradle.bat')))) { throw 'Gradle executable missing after extraction' }" >>"%LOG%" 2>&1
if errorlevel 1 (
  echo ERROR: Could not download or unpack Gradle. See BUILD_LOG.txt.
  goto :failure
)

:build
echo.
echo Starting Gradle build...
echo IMPORTANT: Compile errors, if any, are in BUILD_LOG.txt.
echo.
if /I "%GRADLE_EXE%"=="gradle" (
  call gradle --no-daemon clean prepareRelease >"%LOG%.gradle" 2>&1
) else (
  call "%GRADLE_EXE%" --no-daemon clean prepareRelease >"%LOG%.gradle" 2>&1
)
set "RESULT=%ERRORLEVEL%"
type "%LOG%.gradle"
type "%LOG%.gradle" >>"%LOG%"
del "%LOG%.gradle" >nul 2>nul
if not "%RESULT%"=="0" goto :failure

echo.
echo SUCCESS: Release JAR should be in build\release\
echo Log: BUILD_LOG.txt
pause
exit /b 0

:failure
echo.
echo BUILD FAILED. Read BUILD_LOG.txt or copy its contents to ChatGPT.
echo.
pause
exit /b 1
