#! /bin/sh

# $Id: build.sh,v 1.9 2001/07/18 23:38:35 jdaniel Exp $

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
CLASSPATH=build/classes:$CLASSPATH:$JAVA_HOME/lib/tools.jar

echo $CLASSPATH
$JAVA -classpath $CLASSPATH -Xmx128m -Dant.home=lib org.apache.tools.ant.Main "$@" -buildfile src/build.xml
