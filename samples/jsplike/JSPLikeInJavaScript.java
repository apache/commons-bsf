// program to illustrate use of BSF in a JSP-generated servlet
// like scenario. The model is there's a series of Java code pieces
// intermixed with a series of JavaScript code pieces.

import org.apache.bsf.*;

public class JSPLikeInJavaScript {
  BSFManager mgr = new BSFManager ();
  BSFEngine js;

  /* assume that the JSP page is compiled into the constructor:
     intermix java and javascript code now. The .jsp could've looked
     like the following (I'm guess syntax here..). Note that it uses
     LiveConnect (part of rhino) to run. I'm assuming that language
     was set to javascript (rhino) somewhere before.
       <html>
       <head><title>JSPLike</title></head>
       <body>
       <% var response = bsf.lookupBean ("response");
          // above is the bean registered into bsf with the name "response"
	  // I can now call methods on that bean etc. using response.*.
          var startDate = new java.util.Date ();
          var startDateStr = startDate + ""; %>
       <h1>Welcome at <%= startDateStr %></h1>
       stuff
       Response bean's length is: <%= response.length %>
       Response bean's uppercase value is: <%= response.toUpperCase () %>
       <% var endDate = new java.util.Date ();
          var endDateStr = endDate + ""; %>
       <h5>Page generation done at <%= endDateStr %></h5>
       </body>
       </html>
   */
  public JSPLikeInJavaScript () throws BSFException {
    js = mgr.loadScriptingEngine ("javascript");

    // first register the response bean into the runtime 
    mgr.registerBean ("response", new String ("howdy-doody"));

    Object result;

    System.out.println ("<html>");
    System.out.println ("<head><title>JSPLike</title></head>");
    System.out.println ("<body>");
    
    result = js.eval ("", 0, 0,
		      "var response = bsf.lookupBean (\"response\");" +
		      "var startDate = new java.util.Date ();" +
		      "var startDateStr = startDate + \"\";");
    System.out.print ("<h1>Welcome at ");
    result = js.eval ("", 0, 0,"startDateStr");
    System.out.println (result + "</h1>");
    System.out.println ("stuff");
    System.out.print ("Response bean's length is: ");
    result = js.eval ("", 0, 0,"response.length() + \"\"");
    System.out.println (result);
    System.out.print ("Response bean's uppercase value is: ");
    result = js.eval ("", 0, 0,"response.toUpperCase() + \"\"");
    System.out.println (result);
    result = js.eval ("", 0, 0,"var endDate = new java.util.Date ();" +
		      "var endDateStr = endDate + \"\";");
    System.out.print ("<h5>Page generation done at ");
    result = js.eval ("", 0, 0,"endDateStr");
    System.out.println (result + "</h5>");
    System.out.println ("</body>");
    System.out.println ("</html>");
  }
  
  public static void main (String[] args) throws Exception {
    new JSPLikeInJavaScript ();
  }
}
