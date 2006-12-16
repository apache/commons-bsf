/*
 * Copyright 2006 The Apache Software Foundation.
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

public class BSF_Log // implements org.apache.commons.logging.Log
{
    static private Class       oac_LogFactory              = null;
    static private Method      oac_LogFactoryGetLog_Clazz  = null;
    static private Method      oac_LogFactoryGetLog_String = null;

    {           // try to demand load the apache commons logging LogFactory
        try
        {
            ClassLoader cl= Thread.currentThread().getContextClassLoader();
            oac_LogFactory = cl.loadClass("org.apache.commons.logging.LogFactory");

                // get method with Class object argument
            oac_LogFactoryGetLog_Clazz = oac_LogFactory.getMethod("getLog", new Class[] {Class.class});

                // get method with String object argument
            oac_LogFactoryGetLog_String = oac_LogFactory.getMethod("getLog", new Class[] {String.class});
        }

        catch (ClassNotFoundException e) { ; }  // o.k., so we do not use org.apache.commons.logging in this run
        catch (NoSuchMethodException  e) { ; }  // o.k., so we do not use org.apache.commons.logging in this run
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
            ((org.apache.commons.logging.Log) oac_logger).debug(msg);
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void debug(Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            ((org.apache.commons.logging.Log) oac_logger).debug(msg, t);
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    // --------------------------------------------------------------------
    public void error(Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            ((org.apache.commons.logging.Log) oac_logger).error(msg);
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void error(Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            ((org.apache.commons.logging.Log) oac_logger).error(msg, t);
        }
        catch (Exception e) { e.printStackTrace(); }
    };


    // --------------------------------------------------------------------
    public void fatal(Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            ((org.apache.commons.logging.Log) oac_logger).fatal(msg);
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void fatal(Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?
        try
        {
            ((org.apache.commons.logging.Log) oac_logger).fatal(msg, t);
        }
        catch (Exception e) { e.printStackTrace(); }
    };


    // --------------------------------------------------------------------
    public void info (Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            ((org.apache.commons.logging.Log) oac_logger).info(msg);
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void info (Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            ((org.apache.commons.logging.Log) oac_logger).info(msg, t);
        }
        catch (Exception e) { e.printStackTrace(); }
    };


    // --------------------------------------------------------------------
    public void trace(Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            ((org.apache.commons.logging.Log) oac_logger).trace(msg);
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void trace(Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            ((org.apache.commons.logging.Log) oac_logger).trace(msg, t);
        }
        catch (Exception e) { e.printStackTrace(); }
    };


    // --------------------------------------------------------------------
    public void warn (Object msg)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            ((org.apache.commons.logging.Log) oac_logger).warn(msg);
        }
        catch (Exception e) { e.printStackTrace(); }
    };

    public void warn (Object msg, Throwable t)
    {
        if (oac_logger==null) return;   // no org.apache.commons.logging.Log object ?

        try
        {
            ((org.apache.commons.logging.Log) oac_logger).warn(msg, t);
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
            return ((org.apache.commons.logging.Log) oac_logger).isDebugEnabled();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }

    public boolean isErrorEnabled()
    {
        if (oac_logger==null) return false;     // no org.apache.commons.logging.Log object ?

        try
        {
            return ((org.apache.commons.logging.Log) oac_logger).isErrorEnabled();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }

    public boolean isFatalEnabled()
    {
        if (oac_logger==null) return false;     // no org.apache.commons.logging.Log object ?

        try
        {
            return ((org.apache.commons.logging.Log) oac_logger).isFatalEnabled();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }

    public boolean isInfoEnabled ()
    {
        if (oac_logger==null) return false;     // no org.apache.commons.logging.Log object ?

        try
        {
            return ((org.apache.commons.logging.Log) oac_logger).isInfoEnabled();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }

    public boolean isTraceEnabled()
    {
        if (oac_logger==null) return false;     // no org.apache.commons.logging.Log object ?

        try
        {
            return ((org.apache.commons.logging.Log) oac_logger).isTraceEnabled();
        }
        catch (Exception e) { ; }
        finally             { return false; }
    }

    public boolean isWarnEnabled ()
    {
        if (oac_logger==null) return false;     // no org.apache.commons.logging.Log object ?

        try
        {
            return ((org.apache.commons.logging.Log) oac_logger).isWarnEnabled();
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
        System.out.println("\n\tbl=["+bl+"] <<<---   <<<---   <<<---");
        bl.debug("debug message. ");
        bl.error("error message. ");
        bl.fatal("fatal message. ");
        bl.info ("info  message. ");
        bl.trace("trace message. ");
        bl.warn ("warn  message. ");

        System.out.println("\tisDebugEnabled: "+bl.isDebugEnabled());
        System.out.println("\tisErrorEnabled: "+bl.isErrorEnabled());
        System.out.println("\tisFatalEnabled: "+bl.isFatalEnabled());
        System.out.println("\tisInfo Enabled: "+bl.isInfoEnabled());
        System.out.println("\tisTraceEnabled: "+bl.isTraceEnabled());
        System.out.println("\tisWarn Enabled: "+bl.isWarnEnabled());

        System.out.println("\tbl=["+bl+"] --->>>   --->>>   --->>>");
        System.out.println("--------------------------------------------------------");
    }
}

