<html> 
<%@ page language="vbscript" %>
<head> 
   <title>Temperature Table</title> 
</head> 
<body> 
<h1>Temperature Table <blink>sponsored by Microsoft VBScript</blink></h1> 
<p>American tourists visiting Canada can use this handy temperature 
table which converts from Fahrenheit to Celsius: 
<br><br> 

<table BORDER COLS=2 WIDTH="20%" > 
<tr BGCOLOR="#FFFF00"> 
<th>Fahrenheit</th> 
<th>Celsius</th> 
</tr> 

<%
  for i = 0 to 100 step 10
    out.println "<tr ALIGN=RIGHT BGCOLOR=""#CCCCCC"">"
    out.println "<td>" &  i & "</td>"
    out.println "<td>" & form((i - 32)*5/9) & "</td>"
    out.println "</tr>"
  next
  function form(n)
  form= CDbl(CInt(n*100))/100.0
  end function
%>

</table> 
<p><i> <%= CreateBean("java.util.Date").toString() %> </i></p> 

</body> 
</html>
