#! /bin/sh

# $Id: test.sh,v 1.4 2000/02/23 21:19:05 arkin Exp $

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
CLASSPATH=$JAVA_HOME/lib/tools.jar:$CLASSPATH

if [ -z $1 ] ; then
  echo "Usage: test <pkg>";
  exit;
fi
$JAVA -cp $CLASSPATH tests.$1 $2 $3 $4 $5 $6


