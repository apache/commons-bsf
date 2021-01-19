#! /bin/sh

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

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
