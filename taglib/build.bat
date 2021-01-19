@rem
@rem Licensed to the Apache Software Foundation (ASF) under one or more
@rem contributor license agreements.  See the NOTICE file distributed with
@rem this work for additional information regarding copyright ownership.
@rem The ASF licenses this file to You under the Apache License, Version 2.0
@rem (the "License"); you may not use this file except in compliance with
@rem the License.  You may obtain a copy of the License at
@rem
@rem      http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
 
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

