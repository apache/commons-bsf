#!/bin/bash
export JAVA_HOME=/usr/java/jdk
export BSF_HOME=/home/vjo/IBM/BSF/bsf23
export VAME_PORT=8098
export LOG_LEVEL=1

################################################################
### DON'T CHANGE ANYTHING BELOW...
################################################################

export JSDB_PATH=$BSF_HOME/build/lib/bsf.jar

export DBG=""
if [ "$1" = "debug" ]; then
    export DBG="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=$VAME_PORT"
fi

java -Dcom.ibm.bsf.debug.logLevel=$LOG_LEVEL -cp $JSDB_PATH $DBG com.ibm.bsf.dbline.JsDb



