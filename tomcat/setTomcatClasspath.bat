@echo off

REM This btach file sets the classpath for Tyrex to be intergrated with Tomcat

set cp=
for %%i in (%~dp0lib\*.jar) do call %~dp0cp.bat %%i
for %%i in (%~dp0lib\j2ee\*.jar) do call %~dp0cp.bat %%i

set classpath=%CLASSPATH%;%CP%

set cp=