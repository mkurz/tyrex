@echo off

if not exist %~dp0classes mkdir %~dp0classes

set tcp=%~dp0src;%~dp0lib\tyrex-0.9.6.1.jar;%~dp0lib\j2ee\jdbc-se2.0.jar;%~dp0lib\j2ee\jta1.0.1.jar;%~dp0tomcat-lib\servlet.jar;%~dp0tomcat-lib\webserver.jar

javac -classpath %tcp% -d %~dp0classes %~dp0src\*.java


if not exist %~dp0src\Tomcat.class goto end

if exist %~dp0lib\tyrex-tomcat.jar del %~dp0lib\tyrex-tomcat.jar /F /Q >nul

jar -cf %~dp0lib\tyrex-tomcat.jar -C %~dp0classes .


:end

rmdir %~dp0classes /S /Q >nul

set tcp=