@echo off
REM $Id: test_iiop.bat,v 1.1 2001/04/24 18:59:51 jdaniel Exp $
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=build\classes;build\tests;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i

REM %JAVA% -classpath %CP% tests.RunTests %1 %2 %3 %4 %5 %6
%JAVA% -classpath %CP% -DORBProfile= ots.OTSTest -execute
