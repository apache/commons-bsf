<html> 
<%@ page language="javascript" %>
<head> 
   <title>Temperature Table</title> 
</head> 
<body> 
<h1>Temperature Table <blink>sponsored by Rhino JavaScript</blink></h1> 
<p>American tourists visiting Canada can use this handy temperature 
table which converts from Fahrenheit to Celsius: 
<br><br> 

<table BORDER COLS=2 WIDTH="20%" > 
<tr BGCOLOR="#FFFF00"> 
<th>Fahrenheit</th> 
<th>Celsius</th> 
</tr> 

<%
  for(var i= 0; i <101; i+=10)
  {
    out.println("<tr ALIGN=RIGHT BGCOLOR=\"#CCCCCC\">");
    out.println("<td>" +  i + "</td>");
    out.println("<td>" + Math.round((i - 32)*5/9) + "</td>");
    out.println("</tr>");
  }
%>

</table> 
<p><i> <%= java.util.Date() %> </i></p> 

</body> 
</html>
