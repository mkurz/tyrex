#! /bin/sh

# $Id: test.sh,v 1.1 2000/01/11 00:33:46 roro Exp $

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
CLASSPATH=build/classes/:$CLASSPATH

$JAVAC -cp $CLASSPATH tyrex.$1.Test $2 $3 $4

