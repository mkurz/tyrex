@echo off
REM $Id: test.bat,v 1.3 2000/09/08 20:00:02 mohammed Exp $
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=build\classes;build\tests;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i

%JAVA% -classpath %CP% RunTests %1 %2 %3 %4 %5 %6
