@echo off
REM $Id: test.bat,v 1.7 2001/04/24 20:13:05 psq Exp $
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=build\classes;build\tests;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i

REM %JAVA% -classpath %CP% tests.RunTests %1 %2 %3 %4 %5 %6
%JAVA% -classpath %CP% -Dtransaction.configuration=tyrex_configuration.xml tests.TestHarness %1 %2 %3 %4 %5 %6

