#! /bin/sh

# $Id: build.sh,v 1.2 2000/01/17 22:10:08 arkin Exp $

if [ -z "$JAVA_HOME" ] ; then
  JAVAC=`which java`
  if [ -z "$JAVAC" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVAC`
  JAVA_HOME=$JAVA_BIN/..
fi

JAVAC=$JAVA_HOME/bin/java

CLASSPATH=`echo lib/*.jar | tr ' ' ':'`:$CLASSPATH
CLASSPATH=$JAVA_HOME/lib/tools.jar:$CLASSPATH
echo $CLASSPATH

$JAVAC -classpath $CLASSPATH -Dant.home=lib \
    org.apache.tools.ant.Main "$@" -buildfile src/build.xml
