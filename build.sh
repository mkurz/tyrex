#! /bin/sh

# $Id: build.sh,v 1.3 2000/02/23 21:19:05 arkin Exp $

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

CLASSPATH=`echo lib/*.jar | tr ' ' ':'`:$CLASSPATH
CLASSPATH=$JAVA_HOME/lib/tools.jar:$CLASSPATH


$JAVA -classpath $CLASSPATH -Dant.home=lib org.apache.tools.ant.Main "$@" -buildfile src/build.xml
