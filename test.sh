#! /bin/sh

# $Id: test.sh,v 1.2 2000/01/17 22:10:08 arkin Exp $

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

if [ -z $1 ] ; then
  echo "Usage: test.sh [Demo|<pkg> [<params>]]";
  exit 1;
fi
if [ $1 = "Demo" ] ; then
  $JAVAC -cp $CLASSPATH tyrex.server.Demo $2 $3 $4
else
  $JAVAC -cp $CLASSPATH tyrex.$1.Test $2 $3 $4
fi

