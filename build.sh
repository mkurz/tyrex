#! /bin/sh

# $Id: build.sh,v 1.7 2001/03/13 03:14:55 arkin Exp $

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
CLASSPATH=build/classes/:$CLASSPATH:$JAVA_HOME/lib/tools.jar

$JAVA -classpath $CLASSPATH -Xmx128m -Dant.home=lib org.apache.tools.ant.Main "$@" -buildfile src/build.xml
