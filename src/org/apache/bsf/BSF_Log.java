/*
 * Copyright 2006-2007 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bsf;
import  java.lang.reflect.*;

/** This class is used in BSF for logging (a delegator for <em>org.apache.commons.logging</em>,
 *  which is needed for compilation) using the <code>org.apache.commons.logging.Log</code>
 *  methods.

 Therefore this class implements all the  <code>org.apache.commons.logging.Log</code>
 methods. If <code>org.apache.commons.logging.LogFactory</code> is available, then this
 * class is used to get an <code>org.apache.commons.logging.Log</code> instance to which to
 * forward the message.

 *  Therefore, if Apache's common logging is available, then it is employed.
 * If Apache's commons logging is <em>not</em> available then a <em>no-op</em> behaviour
 is employed, modelled after <code>org.apache.commons.logging.impl.NoOpLog</code>.

   @author Rony G. Flatscher, 2006-12-08
*/

/* ---rgf, 2007-01-29, loading and invoking all methods via reflection
*/

public class BSF_Log // implements org.apache.commons.logging.Log
{
    final private static int iDebug=0;  // don't show any debug-info
    static private Class       oac_Log                     = null;
    static private Class       oac_LogFactory              = null;
    static private Method      oac_LogFactoryGetLog_Clazz  = null;
    static private Method      oac_LogFactoryGetLog_String = null;

    static private Method meths[] = new Method [18];  // store the Log methods
        // define the slots in the array
    final private static int debug1 =  0 ;
    final private static int debug2 =  1 ;
    final private static int isDebugEnabled =  2 ;
    final private static int error1 =  3 ;
    final private static int error2 =  4 ;
    final private static int isErrorEnabled =  5 ;
    final private static int fatal1 =  6 ;
    final private static int fatal2 =  7 ;
    final private static int isFatalEnabled =  8 ;
    final private static int info1  =  9 ;
    final private static int info2  = 10 ;
    final private static int isInfoEnabled  = 11 ;
    final private static int trace1 = 12 ;
    final private static int trace2 = 13 ;
    final private static int isTraceEnabled = 14 ;
    final private static int warn1  = 15 ;
    final private static int warn2  = 16 ;
    final private static int isWarnEnabled  = 17 ;

    {           // try to demand load the apache commons logging LogFactory
        try
        {
            ClassLoader cl= Thread.currentThread().getContextClassLoader();

            oac_Log        = cl.loadClass("org.apache.commons.logging.Log"       );

            oac_LogFactory = cl.loadClass("org.apache.commons.logging.LogFactory");

                // get method with Class object argument
            oac_LogFactoryGetLog_Clazz = oac_LogFactory.getMethod("getLog", new Class[] {Class.class});

                // get method with String object argument
            oac_LogFactoryGetLog_String = oac_LogFactory.getMethod("getLog", new Class[] {String.class});

                // get the Log methods
            String str[][]={{"debug", "isDebugEnabled"},
                            {"error", "isErrorEnabled"},
                            {"fatal", "isFatalEnabled"},
                            {"info",  "isInfoEnabled" },
                            {"trace", "isTraceEnabled"},
                            {"warn",  "isWarnEnabled" }};
            int i=0;
            for ( ; i<6; i++)
            {
                int j=i*3;
                meths[j  ]=oac_Log.getMethod(str[i][0], new Class[] {Object.class});

                meths[j+1]=oac_Log.getMethod(str[i][0], new Class[] {Object.class, Throwable.class});

                meths[j+2]=oac_Log.getMethod(str[i][1], new Class[] {} );

            }
        }

        catch (ClassNotFoundException e)// o.k., so we do not use org.apache.commons.logging in this run
        {
            if (iDebug>1) e.printStackTrace();
        }
        catch (NoSuchMethodException  e)// o.k., so we do not use org.apache.commons.logging in this run
        {
            if (iDebug>1) e.printStackTrace();
        }
    }


    /** Name of the BSF_Log instance. */
    String name=null;

    /** Proxy object for <em>org.apache.commons.logging.Log</em>, if available. */
    private Object oac_logger=null;


    public BSF_Log()
    {
        this.name="<?>";
        if (oac_LogFactory!=null)
        {
            try     // try to get an org.apache.commons.logging.Log object from the LogFactory
            {
                oac_logger=oac_LogFactoryGetLog_String.invoke(oac_LogFactory, new Object[] {this.name});
            }
            catch (Exception e) { e.printStackTrace(); }
        }
    };

    public BSF_Log(String name)
    {
        this.name=name;
        if (oac_LogFactory!=null)
        {
            try     // try to get an org.apache.commons.logging.Log object from the LogFactory
            {
                oac_logger=oac_LogFactoryGetLog_String.invoke(oac_LogFactory, new Object[] {name});
            }
            catch (Exception e) { e.printStackTrace(); }
        }
    };

    public BSF_Log(Class clazz)
    {
        this.name=clazz.getName();
        if (oac_LogFactory!=null)
        {
            try     // try to get an org.apache.commons.logging.Log object from the LogFactory
            {
                oac_logger=oac_LogFactoryGetLog_Clazz.invoke(oac_LogFactory, new Object[] {clazz});
            }
            catch (Exception e) { e.printStackTrace(); }
        }
    };

    // --------------------------------------------------------------------
    public void debug(Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).debug(msg);
            meths[debug1].invoke(oac_logger, new Object [] {msg});

        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void debug(Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).debug(msg, t);
            meths[debug2].invoke(oac_logger, new Object [] {msg, t});
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    // --------------------------------------------------------------------
    public void error(Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).error(msg);
            meths[error1].invoke(oac_logger, new Object [] {msg});
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void error(Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).error(msg, t);
            meths[error2].invoke(oac_logger, new Object [] {msg, t});
        }
        catch (Exception e) { e.printStackTrace(); }
    };


    // --------------------------------------------------------------------
    public void fatal(Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).fatal(msg);
            meths[fatal1].invoke(oac_logger, new Object [] {msg});
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void fatal(Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?
        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).fatal(msg, t);
            meths[fatal2].invoke(oac_logger, new Object [] {msg, t});
        }
        catch (Exception e) { e.printStackTrace(); }
    };


    // --------------------------------------------------------------------
    public void info (Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).info(msg);
            meths[info1].invoke(oac_logger, new Object [] {msg});
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void info (Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).info(msg, t);
            meths[info2].invoke(oac_logger, new Object [] {msg, t});
        }
        catch (Exception e) { e.printStackTrace(); }
    };


    // --------------------------------------------------------------------
    public void trace(Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).trace(msg);
            meths[trace1].invoke(oac_logger, new Object [] {msg});
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void trace(Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).trace(msg, t);
            meths[trace2].invoke(oac_logger, new Object [] {msg, t});
        }
        catch (Exception e) { e.printStackTrace(); }
    };


    // --------------------------------------------------------------------
    public void warn (Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).warn(msg);
            meths[warn1].invoke(oac_logger, new Object [] {msg});
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void warn (Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            // ((org.apache.commons.logging.Log) oac_logger).warn(msg, t);
            meths[warn2].invoke(oac_logger, new Object [] {msg, t});
        }
        catch (Exception e) { e.printStackTrace(); }
    };


    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    public boolean isDebugEnabled()
    {
        if (oac_logger==null) {return false;}   // no org.apache.commons.logging.Log object ?

        try
        {
            // return ((org.apache.commons.logging.Log) oac_logger).isDebugEnabled();
            return ((Boolean) meths[isDebugEnabled].invoke(oac_logger, new Object [] {})).booleanValue();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }

    public boolean isErrorEnabled()
    {
        if (oac_logger==null) return false;     // no org.apache.commons.logging.Log object ?

        try
        {
            // return ((org.apache.commons.logging.Log) oac_logger).isErrorEnabled();
            return ((Boolean) meths[isErrorEnabled].invoke(oac_logger, new Object [] {})).booleanValue();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }

    public boolean isFatalEnabled()
    {
        if (oac_logger==null) return false;     // no org.apache.commons.logging.Log object ?

        try
        {
            // return ((org.apache.commons.logging.Log) oac_logger).isFatalEnabled();
            return ((Boolean) meths[isFatalEnabled].invoke(oac_logger, new Object [] {})).booleanValue();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }

    public boolean isInfoEnabled ()
    {
        if (oac_logger==null) return false;     // no org.apache.commons.logging.Log object ?

        try
        {
            // return ((org.apache.commons.logging.Log) oac_logger).isInfoEnabled();
            return ((Boolean) meths[isInfoEnabled].invoke(oac_logger, new Object [] {})).booleanValue();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }

    public boolean isTraceEnabled()
    {
        if (oac_logger==null) return false;     // no org.apache.commons.logging.Log object ?

        try
        {
            // return ((org.apache.commons.logging.Log) oac_logger).isTraceEnabled();
            return ((Boolean) meths[isTraceEnabled].invoke(oac_logger, new Object [] {})).booleanValue();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }

    public boolean isWarnEnabled ()
    {
        if (oac_logger==null) return false;     // no org.apache.commons.logging.Log object ?

        try
        {
            // return ((org.apache.commons.logging.Log) oac_logger).isWarnEnabled();
            return ((Boolean) meths[isWarnEnabled].invoke(oac_logger, new Object [] {})).booleanValue();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }


        // for development purposes only (to debug this class on its own)
    public static void main (String args[]) {
        System.out.println("in BSF_Log ...");
        System.out.println("--------------------------------------------------------");
        System.out.println("--------------------------------------------------------");
        BSF_Log bl=new BSF_Log();
        dump(bl);
        bl=new BSF_Log(Class.class);
        dump(bl);
        bl=new BSF_Log("Rony was here...");
        dump(bl);

    }

    static void dump(BSF_Log bl)
    {
        System.out.println("\n\tbl=["+bl+"] --->>>   --->>>   --->>>");
        System.err.print("/debug **/"); bl.debug("debug message. "); System.err.println("\\** debug.\\");
        System.err.print("/error **/"); bl.error("error message. "); System.err.println("\\** error.\\");
        System.err.print("/fatal **/"); bl.fatal("fatal message. "); System.err.println("\\** fatal.\\");
        System.err.print("/info  **/"); bl.info ("info  message. "); System.err.println("\\** info .\\");
        System.err.print("/trace **/"); bl.trace("trace message. "); System.err.println("\\** trace.\\");
        System.err.print("/warn  **/"); bl.warn ("warn  message. "); System.err.println("\\** warn .\\");
        System.err.println();

        Throwable t=new Throwable ("Test from Rony for: "+bl);
        System.err.print("/debug **/"); bl.debug("debug message. ", t); System.err.println("\\** debug.\\");
        System.err.print("/error **/"); bl.error("error message. ", t); System.err.println("\\** error.\\");
        System.err.print("/fatal **/"); bl.fatal("fatal message. ", t); System.err.println("\\** fatal.\\");
        System.err.print("/info  **/"); bl.info ("info  message. ", t); System.err.println("\\** info .\\");
        System.err.print("/trace **/"); bl.trace("trace message. ", t); System.err.println("\\** trace.\\");
        System.err.print("/warn  **/"); bl.warn ("warn  message. ", t); System.err.println("\\** warn .\\");
        System.err.println();

        System.out.println("\tisDebugEnabled: "+bl.isDebugEnabled());
        System.out.println("\tisErrorEnabled: "+bl.isErrorEnabled());
        System.out.println("\tisFatalEnabled: "+bl.isFatalEnabled());
        System.out.println("\tisInfo Enabled: "+bl.isInfoEnabled());
        System.out.println("\tisTraceEnabled: "+bl.isTraceEnabled());
        System.out.println("\tisWarn Enabled: "+bl.isWarnEnabled());

        System.out.println("\tbl=["+bl+"] <<<---   <<<---   <<<---");
        System.out.println("--------------------------------------------------------");
    }
}

