@echo off

if "%SERVLET_JAR%" == "" goto noservletjar

set _ANTHOME=%ANT_HOME%
if "%ANT_HOME%" == "" set ANT_HOME=..\..\jakarta-ant

if "%CLASSPATH%" == "" goto noclasspath

set _CLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\xml.jar;%JAVA_HOME%\lib\tools.jar
goto next

:noclasspath

set _CLASSPATH=
set CLASSPATH=%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\xml.jar;%JAVA_HOME%\lib\tools.jar
goto next

:next

java org.apache.tools.ant.Main -Dant.home=%ANT_HOME% -Dservlet.jar=%SERVLET_JAR% %1 %2 %3 %4 %5 %6 %7 %8 %9

:clean
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set ANT_HOME=%_ANTHOME%
set _ANTHOME=
set ARGS=%_ARGS%
set _ARGS=
goto done

:noservletjar
echo You must set SERVLET_JAR to that pathname of your servlet.jar file

:done

