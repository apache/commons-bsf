#!/bin/bash
export JAVA_HOME=/usr/java/jdk
export BSF_HOME=/home/vjo/IBM/BSF/bsf23
export CATALINA_HOME=/home/vjo/IBM/BSF/bsf23/tomcat/build

export VAME_PORT=8097

################################################################
### DON'T CHANGE ANYTHING BELOW
################################################################

export CATALINA_PATH=$CATALINA_HOME/bin/bootstrap.jar:$JAVA_HOME/lib/tools.jar

export BSF_PATH=$BSF_HOME/build/lib/bsf.jar:$BSF_HOME/rhino/build/rhino1_5R2/js.jar

export DBG=""
if [ "$1" = "debug" ]; then
    export DBG="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=$VAME_PORT"
fi

java -Dcom.ibm.bsf.debug.logLevel=1 -Dcom.ibm.bsf.serverLaunch=true -Dbsf.home=$BSF_HOME -Dcatalina.home=$CATALINA_HOME -cp $CATALINA_PATH:$BSF_PATH $DBG org.apache.catalina.startup.Bootstrap start
