#! /bin/sh

# $Id: test.sh,v 1.9 2001/02/27 00:39:11 arkin Exp $

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

CLASSPATH=./build/classes:./build/tests:$CLASSPATH
CLASSPATH=`echo lib/*.jar | tr ' ' ':'`:$CLASSPATH

$JAVA -cp $CLASSPATH $1 $2 $3 $4 $5 $6


