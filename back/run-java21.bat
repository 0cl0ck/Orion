@echo off
set "JAVA_HOME=D:\Dev\Tools\jdk-21.0.6"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo JAVA en cours : 
java -version
echo.
mvn spring-boot:run
