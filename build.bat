@echo off
REM $Id: build.bat,v 1.4 2001/09/22 00:04:55 jdaniel Exp $
set JAVA=%JAVA_HOME%\bin\java
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i
for %%i in (..\OpenORB-1.1.1\dist\*.jar) do call cp.bat %%i
for %%i in (..\RMIoverIIOP-1.1.1\dist\*.jar) do call cp.bat %%i
for %%i in (..\castor-0.9.3\dist\*.jar) do call cp.bat %%i
set CP=build\classes;%JAVA_HOME%\lib\tools.jar;%CP%
%JAVA% -classpath %CP% -Dant.home=lib org.apache.tools.ant.Main all-iiop -buildfile src/build.xml

