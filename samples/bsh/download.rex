/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/* This is a simple demo of a Rexx (modelled after the JavaScript) script that
   uses the Java URL class to download some content from some URL.

   ooRexx (FOSS):   <http://www.ooRexx.org>
   BSF4Rexx (FOSS): <http://wi.wu-wien.ac.at/rgf/rexx/bsf4rexx/current/> or
                    eventually at <https://sourceforge.net/projects/bsf4rexx>
*/

   /* use the Java URL class to read data from a WWW server */
URL_ADDR = "http://www.RexxLA.org/";

say "connecting to:" URL_ADDR
url=.bsf~new("java.net.URL", URL_ADDR)   -- create a URL instance

   /* get the content, a <sun.net.www.http.KeepAliveStream> a subclass of: <sun.net.www.MeteredStream>, a subclass of: <java.io.FilterInputStream> */
content = url~getContent                  /* get the content object  */
say "Bytes available:" content~available  /* get # of bytes          */
say "Downloading .. "
ch=""
do until ch=-1                            /* read the content */
   ch=content~read                        /* returns an Integer value representing a Byte or -1 */
   if ch>=0 then call charout , ch~d2c    /* turn Byte integer value into character */
end

::requires BSF.CLS    -- Object Rexx wrapper classes
