#! /bin/sh

# $Id: test.sh,v 1.3 2000/01/18 04:56:13 arkin Exp $

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
CLASSPATH=build/classes/:build/tests/:src/etc/:$CLASSPATH

if [ -z $1 ] ; then
  echo "Usage: test.sh [Demo|<pkg> [<params>]]";
  exit 1;
fi
if [ $1 = "Demo" ] ; then
  $JAVAC -cp $CLASSPATH tyrex.tools.Demo $2 $3 $4
else
  $JAVAC -cp $CLASSPATH tests.$1 $2 $3 $4
fi

