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

package org.apache.bsf.debug.util;

import java.io.*;
import org.apache.bsf.debug.jsdi.JsEngine;

public class ResultCell {

	public int val32; 
	public long val64;
	public float fval;
	public double dval;
	public Object oval;
	public boolean bool;
	
	private Exception exception; // when an exception occurs at the server.
	private byte stackTraceBytes[];

	public int tid, uid, classId, methodId, cmdId, waitingForCode;
	public ThreadCell thread;
	public ResultCell parent;	
	public JsEngine engine;
	public Stub selfStub;
	public Skeleton selfSkel;
	public boolean done, disconnected;

	private SocketConnection m_con;
	private DataOutputStream fDataOutputStream;
	private DataInputStream fDataInputStream;
	private ByteArrayInputStream fInPacket;
	private ByteArrayOutputStream fOutPacket;

	//------------------------------------------------------------
	public void print() {
		DebugLog.stdoutPrintln("ResultCell...", DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("	tid=" + 
                                       DebugConstants.getConstantName(tid), 
                                       DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("	classId [" + classId +
                                       "] =" + 
                                       DebugConstants.getConstantName(classId),
                                       DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("	methodId [" + methodId + 
                                       "] =" + DebugConstants.getConstantName(methodId), DebugLog.BSF_LOG_L3);

		DebugLog.stdoutPrintln("	bool="+bool, DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("	val32="+val32, DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("	val64="+val64, DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("	fval="+fval, DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("	dval="+dval, DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("	oval="+oval, DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("	exception=" + exception,
                                       DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("		message=" + 
                                       exception.getMessage(), DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln("		stack trace:", 
                                       DebugLog.BSF_LOG_L3);
		DebugLog.stdoutPrintln(new String(stackTraceBytes), 
                                       DebugLog.BSF_LOG_L3);
	}	

	public Exception getException() {
		return exception;
	}
	
	public void setException(Exception ex) {
		exception = ex;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(bytes);

		stream.println(ex.getMessage());
		ex.printStackTrace(stream);
		stackTraceBytes = bytes.toByteArray();
	}
	public void writeException() throws IOException {
		writeObject(exception);
		writeObject(stackTraceBytes);
	}
	public void readException() throws IOException {
		
		exception = (Exception)readObject();
		stackTraceBytes = (byte[])readObject();
	}
	//------------------------------------------------------------
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("ResultCell...\n");
		buf.append("	tid="+DebugConstants.getConstantName(tid)+"\n");
		buf.append("	classId ["+classId+"] ="+DebugConstants.getConstantName(classId)+"\n");
		buf.append("	methodId ["+methodId+"] ="+DebugConstants.getConstantName(methodId)+"\n");

		buf.append("	bool="+bool+"\n");
		buf.append("	val32="+val32+"\n");
		buf.append("	val64="+val64+"\n");
		buf.append("	fval="+fval+"\n");
		buf.append("	dval="+dval+"\n");
		buf.append("	oval="+oval+"\n");
		buf.append("	exception="+exception+"\n");
		buf.append("		message="+exception.getMessage()+"\n");
		return buf.toString();
	}	
	//------------------------------------------------------------
	ResultCell(SocketConnection con) 
	throws IOException {
		m_con = con;
	}	

	void outgoingInvocation(int cmdId, int classId, int methodId, Stub self) 
	throws IOException {
		this.done = false;
		this.cmdId = cmdId;
		this.classId = classId;
		this.methodId = methodId;
		this.selfStub = self;

		// allocate the outgoing packet...
		this.fOutPacket = new ByteArrayOutputStream();
		this.fDataOutputStream = new DataOutputStream(fOutPacket);	
		// no in packet, it will be provided upon
		// completion...
		this.fInPacket = null; 
		this.fDataInputStream = null;
		
		// format the header of the packet.
		// Pre-packet header is composed of the size,
		// the thid, and the cmdId.
		// They are written when the packet is emitted.
		writeId(classId);
		writeId(methodId);
		writeObject(self);
	}	
	//------------------------------------------------------------
	void incomingInvocation(int cmdId, byte bytes[]) 
	throws IOException {

		this.done = false;
		this.cmdId = cmdId;

		// set the incoming packet...
		this.fInPacket = new ByteArrayInputStream(bytes); 
		this.fDataInputStream = new DataInputStream(fInPacket);	
		
		// pre-allocate the outgoing packet.
		this.fOutPacket = new ByteArrayOutputStream();
		this.fDataOutputStream = new DataOutputStream(fOutPacket);	

		this.classId = readId();
		this.methodId = readId();
		this.selfSkel = (Skeleton)readObject();

                DebugLog.stdoutPrintln("	" + 
                                       DebugConstants.getConstantName(classId) + 
                                       "::" + 
                                       DebugConstants.getConstantName(methodId), 
                                       DebugLog.BSF_LOG_L3);
                DebugLog.stdoutPrintln("	on " + this.selfSkel, 
                                       DebugLog.BSF_LOG_L3);
	}
	
	//------------------------------------------------------------
	/**
	 * Once a packet has been read from the socket,
	 * it is passed to the ResultCell and further processed
	 * to parse the remaining data item. 
	 */
	public void setPacketBytes(byte bytes[]) {
		fInPacket = new ByteArrayInputStream(bytes);
		this.fDataInputStream = new DataInputStream(fInPacket);	
		fOutPacket = new ByteArrayOutputStream();
		this.fDataOutputStream = new DataOutputStream(fOutPacket);	
	}
	//------------------------------------------------------------
	public void completionNotify() {
		if (thread!=null) {
			thread.completionNotify(this);
		}
	}
	//------------------------------------------------------------
	public void parseResult() {
		try {
			_parseResult();
		} catch (Exception ex) {
			m_con.wireExceptionNotify(ex);
		}		
	}
	//------------------------------------------------------------
	public void sendResult() {
		try {
			_sendResult();
		} catch (Exception ex) {
			m_con.wireExceptionNotify(ex);
		}		
	}
	//------------------------------------------------------------
	private void _parseResult()
		throws Exception {
		double dval;
					
		switch(waitingForCode) {
			case DebugConstants.WAIT_FOR_VOID:
				break;
			case DebugConstants.WAIT_FOR_BOOLEAN:
				bool = readBoolean();
				break;
			case DebugConstants.WAIT_FOR_INT:
				val32 = readInt();
				break;
			case DebugConstants.WAIT_FOR_LONG:
				val64 = readLong();
				break;
			case DebugConstants.WAIT_FOR_FLOAT:
				fval = readFloat();
				break;
			case DebugConstants.WAIT_FOR_DOUBLE:
				dval = readDouble();
				break;
			case DebugConstants.WAIT_FOR_OBJECT:
				oval = readObject();
				break;
			default :
				throw new Error("Error in the request/answer protocol");
		}				
	}

	private void _sendResult()
		throws Exception {
					
		byte bytes[];

		if (exception != null) {
			DebugLog.stdoutPrintln("\n**** Exception occurred while invoking...", DebugLog.BSF_LOG_L0);
			DebugLog.stdoutPrintln("	message is " + 
                                               exception.getMessage(),
                                               DebugLog.BSF_LOG_L0);
			exception.printStackTrace();

			fOutPacket = new ByteArrayOutputStream();
			this.fDataOutputStream = new DataOutputStream(fOutPacket);	
			
			writeException();
			 
			bytes = fOutPacket.toByteArray();
			m_con.sendPacket(thread.getThId(),cmdId,true,bytes,true);

			DebugLog.stdoutPrintln("	Exception has been sent back...", DebugLog.BSF_LOG_L0);
			return;
		}
		
		switch(waitingForCode) {
			case DebugConstants.WAIT_FOR_VOID:
				break;
			case DebugConstants.WAIT_FOR_BOOLEAN:
				writeBoolean(bool);
				break;
			case DebugConstants.WAIT_FOR_INT:
				writeInt(val32);
				break;
			case DebugConstants.WAIT_FOR_LONG:
				writeLong(val64);
				break;
			case DebugConstants.WAIT_FOR_FLOAT:
				writeFloat(fval);
				break;
			case DebugConstants.WAIT_FOR_DOUBLE:
				writeDouble(dval);
				break;
			case DebugConstants.WAIT_FOR_OBJECT:
				writeObject(oval);
				break;
			default :
				throw new Error("Error in the request/answer protocol");
		}				
		bytes = fOutPacket.toByteArray();
		m_con.sendPacket(thread.getThId(),cmdId,true,bytes,false);
	}
	//------------------------------------------------------------
	public void sendInvocation()
		throws Exception {
					
		byte bytes[];
		bytes = fOutPacket.toByteArray();
		m_con.sendPacket(thread.getThId(),cmdId,false,bytes,false);
	}
	//--------------------------------------------
	public void voidResult() {
		waitingForCode = DebugConstants.WAIT_FOR_VOID;
	}
	public void booleanResult(boolean val) {
		bool = val;
		waitingForCode = DebugConstants.WAIT_FOR_BOOLEAN;
	}
	public void intResult(int val) {
		val32 = val;
		waitingForCode = DebugConstants.WAIT_FOR_INT;
	}
	public void longResult(long val) {
		val64 = val;
		waitingForCode = DebugConstants.WAIT_FOR_LONG;
	}
	public void floatResult(float val) {
		fval =val;
		waitingForCode = DebugConstants.WAIT_FOR_FLOAT;
	}
	public void doubleResult(double val) {
		dval = val;
		waitingForCode = DebugConstants.WAIT_FOR_DOUBLE;
	}
	public void objectResult(Object obj) {
		oval = obj;
		waitingForCode = DebugConstants.WAIT_FOR_OBJECT;
	}

	//////////////////////////////////////////////////////////
	/** 
	 * Default writing methods for marshalling out parameters
	 * in remote method calls.
	 */
	//////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////

	public void writeBoolean(boolean bool) throws IOException {
            DebugLog.stdoutPrintln("	marshalling bool" + bool, DebugLog.BSF_LOG_L3);
            fDataOutputStream.writeBoolean(bool);
	}

	public void writeId(int id) throws IOException {
            DebugLog.stdoutPrintln("	marshalling id=" + 
                                   DebugConstants.getConstantName(id),
                                   DebugLog.BSF_LOG_L3);
            fDataOutputStream.writeInt(id);
	}

	public void writeInt(int val32) throws IOException {
            DebugLog.stdoutPrintln("	marshalling int " + val32, DebugLog.BSF_LOG_L3);
            fDataOutputStream.writeInt(val32);
	}

	public void writeLong(long val64) throws IOException {
            DebugLog.stdoutPrintln("	marshalling long " + val64, 
                                   DebugLog.BSF_LOG_L3);
            fDataOutputStream.writeLong(val64);
	}

	public void writeFloat(float fval) throws IOException {
            DebugLog.stdoutPrintln("	marshalling float " + fval, 
                                   DebugLog.BSF_LOG_L3);
            fDataOutputStream.writeFloat(fval);
	}

	public void writeDouble(double dval) throws IOException {
            DebugLog.stdoutPrintln("	marshalling double " + dval, 
                                   DebugLog.BSF_LOG_L3);
            fDataOutputStream.writeDouble(dval);
	}

	public void writeObject(Object object) throws IOException {
            if (object == null) {
                DebugLog.stdoutPrintln("	marshalling null object ",
                                       DebugLog.BSF_LOG_L3);
                fDataOutputStream.writeInt(DebugConstants.NULL_OBJECT);
            } 
            else {
                if (object instanceof Skeleton) {
                    Skeleton skel = (Skeleton)object;
                    m_con.exportSkeleton(skel);
                    
                    DebugLog.stdoutPrintln("	marshalling (iid=" + 
                                           skel.getTid() + ";uid=" + 
                                           skel.getUid() + " skeleton= " +
                                           skel, DebugLog.BSF_LOG_L3);
                    fDataOutputStream.writeInt(DebugConstants.SKEL_OBJECT);
                    fDataOutputStream.writeInt(skel.getTid());
                    fDataOutputStream.writeInt(skel.getUid());
                } 
                else if (object instanceof Stub) {
                    Stub stub = (Stub)object;
                    
                    DebugLog.stdoutPrintln("	marshalling (tid=" + 
                                           tid + ";uid=" + stub.getUid() + 
                                           " stub= " + stub, DebugLog.BSF_LOG_L3);
                    fDataOutputStream.writeInt(DebugConstants.STUB_OBJECT);
                    // no need to send the tid, the uid identifies 
                    // the skeleton on the other side that already exists.
                    fDataOutputStream.writeInt(stub.getUid());
                } 
                else {
                    ObjectOutputStream oos;
                   
                    DebugLog.stdoutPrintln("Connection marshalling " +
                                           "value object " + object, 
                                           DebugLog.BSF_LOG_L3);
                    fDataOutputStream.writeInt(DebugConstants.VALUE_OBJECT);
                    oos = new ObjectOutputStream(fOutPacket);
                    oos.writeObject(object);
                }
            }
	}

	//////////////////////////////////////////////////////////
	/** 
	 * Default reading methods for unmarshalling in parameters
	 * from remote method calls.
	 */
	//////////////////////////////////////////////////////////

	public boolean readBoolean() throws IOException {
            boolean bool = fDataInputStream.readBoolean();
            DebugLog.stdoutPrintln("Connection marshalling boolean " + 
                                   bool, DebugLog.BSF_LOG_L3);
            return bool;
	}

	public int readId() throws IOException {
            int val32 = fDataInputStream.readInt();
            DebugLog.stdoutPrintln("	Unmarshalling id=[" + 
                                   DebugConstants.getConstantName(val32) +
                                   " (" + val32 + ")", DebugLog.BSF_LOG_L3);
            return val32;
	}

	public int readInt() throws IOException {
            int val32 = fDataInputStream.readInt();
            DebugLog.stdoutPrintln("	Unmarshalling int " + val32, 
                                   DebugLog.BSF_LOG_L3);
            return val32;
	}
    
	public long readLong() throws IOException {
            long val64 = fDataInputStream.readLong();
            DebugLog.stdoutPrintln("	Unmarshalling long " + val64,
                                   DebugLog.BSF_LOG_L3);
            return val64;
	}

	public float readFloat() throws IOException {
            float fval = fDataInputStream.readFloat();
            DebugLog.stdoutPrintln("	Unmarshalling float " + fval,
                                   DebugLog.BSF_LOG_L3);
            return fval;
	}

        public double readDouble() throws IOException {
            double dval = fDataInputStream.readDouble();
            DebugLog.stdoutPrintln("	Unmarshalling double " + dval,
                                   DebugLog.BSF_LOG_L3);
            return dval;
	}

	public Object readObject() throws IOException {
            ObjectInputStream ois;
            
            Object object = null;
            int tag;
            int tid, uid, enguid;
            
            DebugLog.stdoutPrintln("	Unmarshalling an object...", 
                                   DebugLog.BSF_LOG_L3);
            
            tag = fDataInputStream.readInt();
            switch (tag) {
            case DebugConstants.NULL_OBJECT :
                object = null;
                DebugLog.stdoutPrintln("		null object", 
                                       DebugLog.BSF_LOG_L3);
                break;
            case DebugConstants.VALUE_OBJECT :
                try {
                    ois = new ObjectInputStream(fInPacket);
                    object = ois.readObject();
                    DebugLog.stdoutPrintln("		value object= " + 
                                           object, DebugLog.BSF_LOG_L3);
                } catch (ClassNotFoundException ex) {
                    object = null;
                }
                break;
            case DebugConstants.STUB_OBJECT :
                // it was a stub on the sender side,
                // so it maps to a local skeleton 
                // that was remoted...
                uid = fDataInputStream.readInt();
                object = m_con.getSkeleton(uid);
                DebugLog.stdoutPrintln("		Local skel object= " +
                                       object + "(uid=" + uid + ")", 
                                       DebugLog.BSF_LOG_L3);
                break;
            case DebugConstants.SKEL_OBJECT :
                // it was a skeleton on the sender's side
                // so it has to swizzled into a stub on this
                // side...
                tid = fDataInputStream.readInt();
                uid = fDataInputStream.readInt();
                object = m_con.getStub(tid,uid);
                DebugLog.stdoutPrintln("		Local stub object= " +
                                       object + "(tid=" + tid + "; uid=" + 
                                       uid + ")", DebugLog.BSF_LOG_L3);
                break;
            default:
                throw new Error("Wire Protocol Error: unknown object format.");
            }
            return object;
	}

	////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	/**
	 * The following methods are for waiting for the result of an
	 * outgoing method invocation.
	 */
	////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public Object waitForValueObject() throws Exception {
		
		waitingForCode = DebugConstants.WAIT_FOR_OBJECT;
		thread.waitOnCompletion(this);
		return oval;
	}
	//-----------------------------------------------------------
	public boolean waitForBooleanValue() throws Exception {
		waitingForCode = DebugConstants.WAIT_FOR_BOOLEAN;
		thread.waitOnCompletion(this);
		return bool;
	}
	//-----------------------------------------------------------
	public int waitForIntValue() throws Exception {
		waitingForCode = DebugConstants.WAIT_FOR_INT;
		thread.waitOnCompletion(this);
		return val32;
	}
	//-----------------------------------------------------------
	public long waitForLongValue() throws Exception {
		waitingForCode = DebugConstants.WAIT_FOR_LONG;
		thread.waitOnCompletion(this);
		return val64;
	}
	//-----------------------------------------------------------
	public float waitForFloatValue() throws Exception {
		waitingForCode = DebugConstants.WAIT_FOR_FLOAT;
		thread.waitOnCompletion(this);
		return fval;
	}
	//-----------------------------------------------------------
	public double waitForDoubleValue() throws Exception {
		waitingForCode = DebugConstants.WAIT_FOR_DOUBLE;
		thread.waitOnCompletion(this);
		return dval;
	}
	//-----------------------------------------------------------

	public void waitForCompletion() throws Exception {
		waitingForCode = DebugConstants.WAIT_FOR_VOID;
		thread.waitOnCompletion(this);
	}
	//-----------------------------------------------------------
	public Object waitForObject() throws Exception {
		waitingForCode = DebugConstants.WAIT_FOR_OBJECT;
		thread.waitOnCompletion(this);
		return oval;
	}
}
