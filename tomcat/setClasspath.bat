@echo off

set DIR=%~dp0

REM [Arkin] Added the following packages to the classpath
REM         Required to support Tyrex
set CLASSPATH=%CLASSPATH%;C:\java\tyrex\build\classes
REM set CLASSPATH=%CLASSPATH%;%DIR%lib\tyrex-0.9.6.jar
SET CLASSPATH=%CLASSPATH%;%DIR%lib\castor-0.8.8-xml.jar
set CLASSPATH=%CLASSPATH%;%DIR%lib\xerces.jar;%DIR%lib\postgresql-6.3.jar
REM [Arkin] Added the following packages to the classpath
REM         Required for J2EE features (JTA, JDBC, JNDI)
set CLASSPATH=%CLASSPATH%;%DIR%lib\j2ee\jndi.jar;%DIR%lib\j2ee\jdbc-se2.0.jar
set CLASSPATH=%CLASSPATH%;%DIR%lib\j2ee\jta1.0.1.jar;%DIR%lib\j2ee\lightots.jar



