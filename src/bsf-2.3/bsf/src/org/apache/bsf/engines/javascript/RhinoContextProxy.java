/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Apache BSF", "Apache", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from
 *    this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * Sanjiva Weerawarana and others at International Business Machines
 * Corporation. For more information on the Apache Software Foundation,
 * please see <http://www.apache.org/>.
 */

package org.apache.bsf.engines.javascript;

/**
 * Insert the type's description here.
 * Creation date: (8/23/2001 4:09:36 PM)
 * @author: Administrator
 */

import java.util.Hashtable;

import org.apache.bsf.*;
import org.apache.bsf.debug.jsdi.*;
import org.mozilla.javascript.*;
import org.mozilla.javascript.debug.*;

import java.rmi.RemoteException;

public class RhinoContextProxy {

    RhinoEngineDebugger m_reDbg;
    Context m_context;
    JsContextStub m_contextStub;

    DebuggableEngine m_engine;

    boolean m_atBreakpoint;
    int m_frameCount;
    JsContextStub m_frames[];

    private static final int NO_STEP = 0, STEP_IN = 1, STEP_OVER = 2,
        STEP_OUT = 3, STOP_ENGINE = 4, RUNNING = 5;

    private int m_stepCmd, m_stepDepth;

    RhinoContextProxy(RhinoEngineDebugger reDbg, Context cx) {
        m_reDbg = reDbg;
        m_context = cx;
        m_engine = cx.getDebuggableEngine();
    }

    public void cancelStepping() {
        m_stepCmd = NO_STEP;
        m_stepDepth = -1;
        m_engine.setBreakNextLine(false);
    }

    public JsContextStub getContext(int depth) {
        return m_frames[depth];
    }

    public int getContextCount() {
        return m_frameCount;
    }

    public JsContextStub getFrame(int no) {
        if (no < 0 || no > m_frameCount)
            return null;
        if (no == m_frameCount)
            return m_contextStub;
        else
            return m_frames[no];
    }

    public int getLineNumber() {
        DebugFrame frame = m_engine.getFrame(0);

        return frame.getLineNumber();
    }

    public RhinoEngineDebugger getRhinoEngineDebugger() {
        return m_reDbg;
    }

    String getSourceName() {
        DebugFrame frame = m_engine.getFrame(0);

        return frame.getSourceName();
    }


    // We hit a known breakpoint.
    // We need to update the stack.
    // Also, cancel any pending stepping operation.
    public JsContextStub hitBreakpoint() throws RemoteException {
        cancelStepping();
        updateStack();
        return m_frames[0];
    }


    public JsContextStub exceptionThrown() throws RemoteException {
        cancelStepping();
        updateStack();
        return m_frames[0];
    }

    public void resumed() {
        JsContextStub stub;
        DebugFrame frame;

        m_atBreakpoint = false;

        for (int f = 0; f < m_frameCount; f++) {
            stub = m_frames[f];
            stub.atBreakpoint(false);
        }
    }

    public void run() {
        m_engine.setBreakNextLine(false);
        m_stepCmd = RUNNING;
        m_stepDepth = -1;

    }

    public void stepIn() {
        m_engine.setBreakNextLine(true);
        m_stepCmd = STEP_IN;
        m_stepDepth = m_frameCount;
    }

    public void stepOut() {
        m_engine.setBreakNextLine(true);
        m_stepCmd = STEP_OUT;
        m_stepDepth = m_frameCount;

    }

    public void stepOver() {
        m_engine.setBreakNextLine(true);
        m_stepCmd = STEP_OVER;
        m_stepDepth = m_frameCount;
    }

    public JsContextStub entry_exit_mode() throws RemoteException {
        cancelStepping();
        updateStack();
        return m_frames[0];
    }

    public JsContextStub stepping() {
        // Did we hit a known breakpoint?

        int frameCount = m_engine.getFrameCount();

        try {
            switch (m_stepCmd) {
            case NO_STEP :
                cancelStepping();
                break;
            case STOP_ENGINE :
                updateStack();
                cancelStepping();
                return m_frames[0];
            case STEP_IN :
                // OG if ((frameCount == m_stepDepth + 1) || 
                // (frameCount == m_stepDepth)) {
                // step if we are in the same frame (nothing to step in... :-)
                // if we are in a called frame...
                // but also if we stepped out of the current frame...
                    updateStack();
                    cancelStepping();
                    return m_frames[0];
            case STEP_OVER :
                // OG if (frameCount == m_stepDepth) {
                // step if we are in the same frame or above...
                // this basically avoids any children frame but 
                // covers the return of the current frame.
                if (frameCount <= m_stepDepth) {
                    updateStack();
                    cancelStepping();
                    return m_frames[0];
                }
                break;
            case STEP_OUT :
                // OG if (frameCount == m_stepDepth - 1) {
                if (frameCount < m_stepDepth) {
                    updateStack();
                    cancelStepping();
                    return m_frames[0];
                }
                break;
            default :
                throw new Error("Unknown command.");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            cancelStepping();
        }
        return null;
    }

    public void stopEngine() {
        m_engine.setBreakNextLine(true);
        m_stepCmd = STOP_ENGINE;
        m_stepDepth = -1;
    }

    public void updateStack() throws RemoteException {
        int nf, of, frameCount = m_engine.getFrameCount();
        JsContextStub frames[] = new JsContextStub[frameCount];
        DebugFrame frame;

        m_atBreakpoint = true;

        // scan the stacks from the outer frame down
        // to the inner one of the shortest of the old
        // and the new known stack.
        // The goal is to recognize the DebugFrame objects
        // that are the sames so that we can reuse the 
        // stubs for those. 
        // As soon as a DebugFrame object is found different,
        // the rest of the stack is different, all the old
        // stubs can be dropped and invalidated, new ones
        // must be created.

        for (nf = 0, of = 0;
             nf < frameCount && of < m_frameCount;
             nf++, of++) {
            frame = m_engine.getFrame(nf);
            if (frame == m_frames[of].m_frame) {
                frames[nf] = m_frames[of];
            } else
                break;
        }
        // now drop all old frames that diverged.
        // Also invalidate the frame stubs so to
        // tracked that they are no longer valid.
        for (; of < m_frameCount; of++) {
            m_reDbg.dropStub(m_frames[of].m_frame);
            m_frames[of].invalidate();
        }
        for (; nf < frameCount; nf++) {
            frame = m_engine.getFrame(nf);
            frames[nf] = new JsContextStub(this, frame, nf);
        }
        m_frames = frames;
        m_frameCount = frameCount;
    }
}
