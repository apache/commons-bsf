package org.apache.bsf.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.bsf.BSFManager;

/**
 * Superclass for language engine tests.
 * @author   Victor J. Orlikowski <vjo@us.ibm.com>
 */
public abstract class BSFEngineTestTmpl extends TestCase {
    protected BSFManager bsfManager;
    protected PrintStream sysOut;

    private PrintStream tmpOut;
    private ByteArrayOutputStream tmpBaos;

    public BSFEngineTestTmpl(String name) {
        super(name);

        sysOut = System.out;
        tmpBaos = new ByteArrayOutputStream();
        tmpOut = new PrintStream(tmpBaos);
    }

    public void setUp() {
        bsfManager = new BSFManager();
        System.setOut(tmpOut);
    }

    public void tearDown() {
        System.setOut(sysOut);
        resetTmpOut();
    }
    
    protected String getTmpOutStr() {
        return tmpBaos.toString();
    }

    protected void resetTmpOut() {
        tmpBaos.reset();
    }
    
    protected String failMessage(String failure, Exception e) {
        String message = failure;
        message += "\nReason:\n";
        message += e.getMessage();
        message += "\n";
        return message;
    }
}
