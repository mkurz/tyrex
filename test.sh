#! /bin/sh

# $Id: test.sh,v 1.8 2001/02/23 19:35:19 omodica Exp $

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

$JAVA -cp $CLASSPATH -Djava.security.manager -Djava.security.policy=./test.policy tests.RunTests $1 $2 $3 $4 $5 $6


