<html>
<%@ page language="bml" %>
<head><title>Some Information</title></head>
<h1> Some Information </h1>
<body bgcolor="white">

<font size=4>
<ul>
<li>  Client Name:
<%
  <call-method target="out" name="println">
    <call-method target="request" name="getRemoteHost"/>
  </call-method>
  <call-method target="out" name="println">
    <string value="(from BML)"/>
  </call-method>
%>
<li>  Client Address:
<%
  <call-method target="out" name="println">
    <call-method target="request" name="getRemoteAddr"/>
  </call-method>
%>
<li>  Server Name:
<%
  <call-method target="out" name="println">
    <call-method target="request" name="getServerName"/>
  </call-method>
%>
<li>  Server Port:
<%
  <call-method target="out" name="println">
    <call-method target="request" name="getServerPort"/>
  </call-method>
%>
<li> Application Major Version:
<%
  <call-method target="out" name="println">
    <call-method target="application" name="getMajorVersion"/>
  </call-method>
%>
<li> Server Info
<%
  <call-method target="out" name="println">
    <call-method target="application" name="getServerInfo"/>
  </call-method>
%>
</ul>
</font>
</body>
</html>
