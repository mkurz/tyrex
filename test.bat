@echo off
REM $Id: test.bat,v 1.1 2000/02/23 21:19:05 arkin Exp $
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=build\classes;build\tests;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i
set CP=%CP%;%JAVA_HOME%\lib\tools.jar

%JAVA% -classpath %CP% tests.%1 %2 %3 %4 %5 %6

