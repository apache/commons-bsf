package org.apache.bsf.test;

import org.apache.bsf.BSFException;
import org.apache.bsf.util.BSFEngineImpl;

public class fakeEngine extends BSFEngineImpl {

    public Object call(Object object, String method, Object[] args)
        throws BSFException
    {
        return Boolean.TRUE;
    }

    public Object eval(String source, int lineNo, int columnNo, Object expr)
        throws BSFException
    {
        return Boolean.TRUE;
    }

    public void iexec(String source, int lineNo, int columnNo, Object script)
        throws BSFException
    {
        System.out.print("PASSED");
    }

    public void exec(String source, int lineNo, int columnNo, Object script)
        throws BSFException
    {
        System.out.print("PASSED");
    }

    public void terminate() {
        super.terminate();
        System.out.print("PASSED");
    }
}
