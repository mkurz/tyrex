@echo off
REM $Id: test.bat,v 1.4 2000/11/10 01:34:27 mohammed Exp $
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=build\classes;build\tests;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i

%JAVA% -classpath %CP% -Djava.security.manager -Djava.security.policy=.\test.policy  RunTests %1 %2 %3 %4 %5 %6
