@echo off
REM $Id: test.bat,v 1.8 2002/05/08 10:09:16 mohammed Exp $
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=build\classes;build\tests;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i

REM %JAVA% -classpath %CP% tests.RunTests %1 %2 %3 %4 %5 %6
%JAVA% -classpath %CP% -Dtransaction.configuration=tyrex_configuration.xml TestHarness %1 %2 %3 %4 %5 %6

