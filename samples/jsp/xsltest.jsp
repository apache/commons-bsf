<html>
<%@ page language="lotusxsl" %>
<body bgcolor="white">
<font size=4>

<%=
  <b xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
     xmlns:java="http://xsl.lotus.com/java">
Hello from XSL at <xsl:value-of select="java:java.util.Date.new()"/>
  </b>
%>

</font>
</body>

</html>
