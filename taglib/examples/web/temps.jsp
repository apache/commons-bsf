<html>
<!--
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * https://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
-->

<%@ taglib uri="http://jakarta.apache.org/taglibs/bsf-2.0" prefix="bsf" %>
<head>
   <title>Temperature Table</title>
</head>
<body>
<h1>Temperature Table</h1>
<p>American tourists visiting Canada can use this handy temperature
table which converts from Fahrenheit to Celsius:
<br><br>

In TCL
<table BORDER COLS=2 WIDTH="20%" >
<tr BGCOLOR="#FFFF00">
<th>Fahrenheit</th>
<th>Celsius</th>
</tr>
<bsf:scriptlet language="tcl">
  package require java

  for {set i 60} {$i<=100} {incr i 10} {
    $out println "<tr ALIGN=RIGHT BGCOLOR=\"#CCCCCC\">"
    $out println "<td>$i</td>"
    $out println [concat "<td>" [format %4.2f [expr ($i - 32.0)*5/9]] "</td>"]
    $out println "</tr>"
  }
</bsf:scriptlet>
</table>
<bsf:expression language="tcl">
    package require java ; java::new java.util.Date
</bsf:expression>


<hr>In Javascript
<table BORDER COLS=2 WIDTH="20%" >
<tr BGCOLOR="#FFFF00">
<th>Fahrenheit</th>
<th>Celsius</th>
</tr>
<bsf:scriptlet language="javascript">
  for (i=60; i<=100; i+=10) {
    out.println ("<tr ALIGN=RIGHT BGCOLOR=\"#CCCCCC\">")
    out.println ("<td>" +  i + "</td>")
    out.println ("<td>" + Math.round((i - 32)*5/9) + "</td>")
    out.println ("</tr>")
  }
</bsf:scriptlet>
</table>
<bsf:expression language="javascript"> new java.util.Date() </bsf:expression>

<hr>In Perl
<table BORDER COLS=2 WIDTH="20%" >
<tr BGCOLOR="#FFFF00">
<th>Fahrenheit</th>
<th>Celsius</th>
</tr>
<bsf:scriptlet language="perlscript">
  for ($i=60; $i<=100; $i+=10) {
    $out->println ("<tr ALIGN=RIGHT BGCOLOR=\"#CCCCCC\">");
    $out->println ("<td>$i</td>");
    $out->println ("<td>" . int(($i - 32)*5/9) . "</td>");
    $out->println ("</tr>");
  }
</bsf:scriptlet>
</table>
<bsf:expression language="perlscript"> CreateBean("java.util.Date") </bsf:expression>

<hr>In JACL
<table BORDER COLS=2 WIDTH="20%" >
<tr BGCOLOR="#FFFF00">
<th>Fahrenheit</th>
<th>Celsius</th>
</tr>
<bsf:scriptlet language="jacl">
package require java

for {set i 60} {$i<=100} {incr i 10} {
  $out println "<tr ALIGN=RIGHT BGCOLOR=\"#CCCCCC\">"
  $out println "<td>$i</td>"
  $out println [concat "<td>" [format %4.2f [expr ($i - 32.0)*5/9]] "</td>"]
  $out println "</tr>"
}
</bsf:scriptlet>
</table>


<hr>In JPython
<table BORDER COLS=2 WIDTH="20%" >
<tr BGCOLOR="#FFFF00">
<th>Fahrenheit</th>
<th>Celsius</th>
</tr>
<bsf:scriptlet language="jpython">
from java.util import Date;

for i in range(60,100,10):
  out.println ("<tr ALIGN=RIGHT BGCOLOR=\"#CCCCCC\">");
  out.println ("<td>%d</td>" % i);
  out.println ("<td>%4.2f</td>" % ((i - 32.0)*5/9));
  out.println ("</tr>");
</bsf:scriptlet>
</table>

<hr>In LotusScript
<table BORDER COLS=2 WIDTH="20%" >
<tr BGCOLOR="#FFFF00">
<th>Fahrenheit</th>
<th>Celsius</th>
</tr>
<bsf:scriptlet language="lotusscript">
for i = 60 to 100 step 10
    out.println "<tr ALIGN=RIGHT BGCOLOR=""#CCCCCC"">"
    out.println "<td>" &  i & "</td>"
    out.println "<td>" & format((i - 32)*5/9," 0.00") & "</td>"
    out.println "</tr>"
    next
</bsf:scriptlet>
</table>

<bsf:expression language="lotusscript"> CreateBean("java.util.Date") </bsf:expression>

</body>
</html>
