#! /bin/sh

# $Id: build.sh,v 1.10 2001/09/22 00:04:55 jdaniel Exp $

if [ -z "$JAVA_HOME" ] ; then
  JAVA=`which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
fi

JAVA=$JAVA_HOME/bin/java

CLASSPATH=$CLASSPATH:`echo lib/*.jar | tr ' ' ':'`
CLASSPATH=$CLASSPATH:`echo ../OpenORB-1.1.1/dist/*.jar | tr ' ' ':'`
CLASSPATH=$CLASSPATH:`echo ../RMIoverIIOP-1.1.1/dist/*.jar | tr ' ' ':'`
CLASSPATH=$CLASSPATH:`echo ../castor-0.9.3/dist/*.jar | tr ' ' ':'`
CLASSPATH=build/classes:$CLASSPATH:$JAVA_HOME/lib/tools.jar

if [ A$1 = "A" ]
then
$JAVA -classpath $CLASSPATH -Xmx128m -Dant.home=lib org.apache.tools.ant.Main all-iiop  -buildfile src/build.xml
else
$JAVA -classpath $CLASSPATH -Xmx128m -Dant.home=lib org.apache.tools.ant.Main "$@" -buildfile src/build.xml
fi
