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

/* This is a simple demo of a JavaScript script that uses the Java
   URL class to download some content from some URL.
   */

URL_ADDR = "http://www.cnn.com/";

/* use a Java bean to get at the URL */
java.lang.System.err.println ("Connecting to .. " + URL_ADDR);
url = new java.net.URL (URL_ADDR);

/* read the content */
java.lang.System.err.println ("Downloading .. ");
content = url.getContent ();
while ((ch = content.read ()) != -1) {
  java.lang.System.out.write (ch)
}
