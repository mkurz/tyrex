@echo off
REM $Id: test.bat,v 1.5 2001/02/23 19:34:49 omodica Exp $
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=build\classes;build\tests;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i

REM %JAVA% -classpath %CP% tests.RunTests %1 %2 %3 %4 %5 %6
%JAVA% -classpath %CP% -Djava.security.manager -Djava.security.policy=.\test.policy tests.RunTests %1 %2 %3 %4 %5 %6
