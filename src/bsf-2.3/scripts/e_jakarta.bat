set JAVA_HOME=c:\jdk1.3.1
set BSF_HOME=c:/Eclipse-1.0/workspaces/cvsroot
set CATALINA_HOME=c:/Apache/jakarta-tomcat-4.0/build

set VAME_PORT=8097

rem ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
rem DON'T CHANGE ANYTHING BELOW...
rem ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

set CATALINA_PATH=%CATALINA_HOME%/bin/bootstrap.jar;%JAVA_HOME%/lib/tools.jar;

set BSF_PATH=%BSF_HOME%/bsf_debug/bin;%BSF_HOME%/bsf/bin;%BSF_HOME%/rhino/src;c:/temp/rhinotest/rhino1_5R2/js.jar

set DBG=
if not "%1" == "debug"  goto exec
set DBG=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=%VAME_PORT%

:exec
rem  -Djava.home="c:\jdk1.3.1" 
java -Dorg.apache.bsf.debug.logLevel=1 -Dorg.apache.bsf.serverLaunch=true -Dbsf.home=%BSF_HOME% -Dcatalina.home=%CATALINA_HOME% -cp %CATALINA_PATH%;%BSF_PATH% %DBG% org.apache.catalina.startup.Bootstrap start


