// program to illustrate use of BSF in a JSP-generated servlet
// like scenario. The model is there's a series of Java code pieces
// intermixed with a series of NetRexx code pieces.

import org.apache.bsf.*;

public class JSPLikeInNetRexx {
  BSFManager mgr = new BSFManager ();
  { mgr.setTempDir ("/tmp"); } // test the tmp dir stuff
//{ mgr.setDebug (true); }
  BSFEngine rexx;

  /* assume that the JSP page is compiled into the constructor:
     intermix java and javascript code now. The .jsp could've looked
     like the following (I'm accepting Sanjiva's guesses about syntax here.).
     I'm assuming that language was set to NetRexx somewhere before.

     Unlike Javascript, standard NetRexx doesn't have the concept of a
     default persistant execution environment. Any data to be stored
     between NetRexx invocations has to be explicitly placed in a
     Property if it's local (which, given the way the NetRexx binding
     has been done in this prototype, means working with minor
     classes) or stashed in the BSF environment's registry if it's
     global. Neither is needed for this simple example; obtaining a
     new Date is painless (we'd need to provide the classname for the
     variable anyway) and "response" is already in the registry.

       <html>
       <head><title>JSPLike</title></head>
       <body>
       <h1>Welcome at <%= return java.util.Date().toString %></h1>
       stuff
       Response bean's length is: <%= 
           return (java.lang.String bsf.lookupBean('response')).length %>
       Response bean's uppercase value is: <%=
           return (java.lang.String bsf.lookupBean('response')).toUpperCase () %>
       <h5>Page generation done at <%= return java.util.Date().toString() %></h5>
       </body>
       </html> */
  public JSPLikeInNetRexx () throws BSFException {
    rexx = mgr.loadScriptingEngine ("netrexx");

    System.out.println("<!- Using "+rexx.eval ("Version")+" ->");
    
    // first register the response bean into the runtime 
    mgr.registerBean ("response", new String ("howdy-doody"));

    Object result;

    System.out.println ("<html>");
    System.out.println ("<head><title>JSPLike</title></head>");
    System.out.println ("<body>");
    
    System.out.print ("<h1>Welcome at ");
    result = rexx.eval ("java.util.Date().toString()");
    System.out.println (result + "</h1>");
    System.out.println ("stuff");
    System.out.print ("Response bean's length is: ");
    result = rexx.eval ("java.lang.Integer((java.lang.String " +
                                 "bsf.lookupBean('response')).length)");
    System.out.println (result);
    System.out.print ("Response bean's uppercase value is: ");
    result = rexx.eval ("(java.lang.String bsf.lookupBean('response')).toUpperCase()");
    System.out.println (result);
    System.out.print ("<h5>Page generation done at ");
    result = rexx.eval ("java.util.Date().toString()");
    System.out.println (result + "</h5>");
    System.out.println ("</body>");
    System.out.println ("</html>");
  }
  
  public static void main (String[] args) throws Exception {
    new JSPLikeInNetRexx ();
  }
}
