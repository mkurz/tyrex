@echo off
REM $Id: test.bat,v 1.2 2000/04/10 20:47:34 arkin Exp $
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=build\classes;build\tests;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i

%JAVA% -classpath %CP% TestHarness %1 %2 %3 %4 %5 %6
