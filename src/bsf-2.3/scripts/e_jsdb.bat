set JAVA_HOME=c:\jdk1.3.1
set BSF_HOME=c:/Eclipse-1.0/workspaces/cvsroot
set VAME_PORT=8098
set LOG_LEVEL=1

rem ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
rem DON'T CHANGE ANYTHING BELOW...
rem ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

set JSDB_PATH=%BSF_HOME%/jsdb/bin;%BSF_HOME%/bsf_debug/bin;

set DBG=
if not "%1" == "debug"  goto exec
set DBG=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=%VAME_PORT%

:exec
java -Dorg.apache.bsf.debug.logLevel=%LOG_LEVEL% -cp %JSDB_PATH% %DBG% org.apache.bsf.dbline.JsDb



