<html>
<jsp:directive.page language="javascript"/>

<body bgcolor="white">

<ul>

<li>
<jsp:scriptlet>
  out.println("Hello from JavaScript");
</jsp:scriptlet>
</li>

<li><% out.println ("Hello from Java<br/>"); %></li>

<li>
<jsp:scriptlet language="java">out.println("Hey. (from Java)");</jsp:scriptlet>
</li>

<li>
<jsp:scriptlet language="bml">
  <call-method target="out" name="println">
    <bean class="java.lang.StringBuffer">
      <args>
        <string value="Java Version: "/>
      </args>
      
      <call-method name="append">
        <call-method target="class:java.lang.System" name="getProperty">
          <string value="java.version"/>
        </call-method>
      </call-method>
      
      <call-method name="append">
        <string value=" (from BML)"/>
      </call-method>
    </bean>
  </call-method>
</jsp:scriptlet>
</li>

<li>
<jsp:scriptlet language="bml">
  <call-method target="out" name="println">
    <bean class="java.lang.StringBuffer">
      <args>
        <string value="Java Home: "/>
      </args>
      
      <call-method name="append">
        <call-method target="class:java.lang.System" name="getProperty">
          <string value="java.home"/>
        </call-method>
      </call-method>
      
      <call-method name="append">
        <string value=" (from BML, again)"/>
      </call-method>
    </bean>
  </call-method>
</jsp:scriptlet>
</li>

</ul>
</body>
</html>
