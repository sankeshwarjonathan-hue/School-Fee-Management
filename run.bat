@echo off
setlocal EnableExtensions
title SchoolFeeWeb - build and start

set "PROJECT_DIR=%~dp0"
set "TOMCAT_HOME=C:\apache-tomcat-10.1.61"
set "CATALINA_HOME=%TOMCAT_HOME%"
set "CATALINA_BASE=%TOMCAT_HOME%"
set "JAVA_HOME=C:\Program Files\Java\jdk-23"
set "SCHOOLFEE_DATA_DIR=D:\Downloads\schoolfee-data"
set "APP_URL=http://localhost:8080/SchoolFeeWeb/admin/login"

echo [1/5] Checking requirements...
if not exist "%TOMCAT_HOME%\bin\catalina.bat" goto missing_tomcat
if not exist "%JAVA_HOME%\bin\java.exe" goto missing_java
if not exist "%SCHOOLFEE_DATA_DIR%\users.txt" goto missing_data
where mvn >nul 2>nul
if errorlevel 1 goto missing_maven
where curl.exe >nul 2>nul
if errorlevel 1 goto missing_curl

set "PATH=%JAVA_HOME%\bin;%PATH%"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dschoolfee.data.dir=%SCHOOLFEE_DATA_DIR%"
cd /d "%PROJECT_DIR%"
if errorlevel 1 goto project_directory_error

echo [2/5] Building SchoolFeeWeb...
call mvn -B clean package
if errorlevel 1 goto build_failed
if not exist "target\SchoolFeeWeb.war" goto war_missing

echo [3/5] Stopping Tomcat before deployment...
curl.exe --silent --output NUL --max-time 2 "http://127.0.0.1:8080/"
if errorlevel 1 goto tomcat_stopped
call "%TOMCAT_HOME%\bin\shutdown.bat"
set "WAIT_COUNT=0"

:wait_for_tomcat
call :check_tomcat_stopped
if errorlevel 1 goto tomcat_stopped
set /a WAIT_COUNT+=1
if %WAIT_COUNT% geq 30 goto tomcat_stop_timeout
ping 127.0.0.1 -n 3 >nul
goto wait_for_tomcat

:tomcat_stopped
echo [4/5] Deploying the new WAR...
copy /Y "target\SchoolFeeWeb.war" "%TOMCAT_HOME%\webapps\SchoolFeeWeb.war"
if errorlevel 1 goto deploy_failed

echo [5/5] Starting Tomcat...
call "%TOMCAT_HOME%\bin\catalina.bat" start
if errorlevel 1 goto tomcat_start_failed

echo Waiting for %APP_URL% to respond...
set "WAIT_COUNT=0"

:wait_for_app
call :check_app_ready
if errorlevel 1 goto app_ready
set /a WAIT_COUNT+=1
if %WAIT_COUNT% geq 60 goto app_timeout
ping 127.0.0.1 -n 3 >nul
goto wait_for_app

:app_ready
echo Application is ready. Opening browser...
start "" "%APP_URL%"
echo.
echo SchoolFeeWeb is running at %APP_URL%
echo Keep this window open for status. Stop Tomcat with:
echo "%TOMCAT_HOME%\bin\shutdown.bat"
goto finish

:check_tomcat_stopped
curl.exe --silent --output NUL --max-time 2 "http://127.0.0.1:8080/"
if errorlevel 1 exit /b 1
exit /b 0

:check_app_ready
curl.exe --silent --output NUL --location --fail --max-time 2 "%APP_URL%"
if errorlevel 1 exit /b 0
exit /b 1

:missing_tomcat
echo ERROR: Tomcat was not found at "%TOMCAT_HOME%".
goto failed

:missing_java
echo ERROR: Java was not found at "%JAVA_HOME%".
goto failed

:missing_data
echo ERROR: Existing user data was not found at "%SCHOOLFEE_DATA_DIR%\users.txt".
goto failed

:missing_maven
echo ERROR: Maven was not found on PATH.
goto failed

:missing_curl
echo ERROR: curl.exe was not found on PATH.
goto failed

:project_directory_error
echo ERROR: Could not enter the project folder "%PROJECT_DIR%".
goto failed

:build_failed
echo ERROR: Maven build failed. Review the Maven output above.
goto failed

:war_missing
echo ERROR: target\SchoolFeeWeb.war was not created.
goto failed

:tomcat_stop_timeout
echo ERROR: Tomcat is still responding after the shutdown timeout.
echo Close the Tomcat service/process and run this file again.
goto failed

:deploy_failed
echo ERROR: Could not deploy the WAR. Check Tomcat folder permissions.
goto failed

:tomcat_start_failed
echo ERROR: Tomcat failed to start. Check "%TOMCAT_HOME%\logs".
goto failed

:app_timeout
echo ERROR: The application did not respond within 2 minutes.
echo Check "%TOMCAT_HOME%\logs" for deployment errors.
goto failed

:failed
echo.
echo Startup failed. This window will remain open so you can read the error.

:finish
echo.
pause
endlocal
