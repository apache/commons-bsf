#! /bin/sh

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../../jakarta-ant
fi

args=""
if [ "$SERVLET_JAR" != "" ] ; then
  args="$args -Dservlet.jar=$SERVLET_JAR"
fi
args="$args -Dant.home=$ANT_HOME"

cp=$ANT_HOME/lib/ant.jar:$ANT_HOME/lib/xml.jar:$JAVA_HOME/lib/tools.jar

java -classpath $cp:$CLASSPATH org.apache.tools.ant.Main $args "$@"
