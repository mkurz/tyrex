@echo off
REM $Id: build.bat,v 1.6 2004/04/30 06:37:05 metaboss Exp $
call setenvironment.bat
set JAVA=%JAVA_HOME%\bin\java
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i
REM for %%i in (..\OpenORB-1.1.1\dist\*.jar) do call cp.bat %%i
REM for %%i in (..\RMIoverIIOP-1.1.1\dist\*.jar) do call cp.bat %%i
REM for %%i in (..\castor-0.9.3\dist\*.jar) do call cp.bat %%i
set CP=build\classes;%JAVA_HOME%\lib\tools.jar;%CP%
%JAVA% -classpath %CP% -Dant.home=lib org.apache.tools.ant.Main %* -buildfile src/build.xml %1 %2 %3 %4 %5

