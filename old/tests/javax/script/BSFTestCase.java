package javax.script;

import java.io.IOException;
import java.io.PrintStream;

/**
 * 
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */

public class BSFTestCase extends junit.framework.TestCase {
	protected StringBuffer outBuffer = new StringBuffer(),
							errBuffer = new StringBuffer();
	private PrintStream sysOut;
	private PrintStream sysErr;
	
	public BSFTestCase(String name) {
		super(name);
	}
		
	protected void setUp() throws Exception {
		sysOut = System.out;
		sysErr = System.err;
		System.setOut(new PrintStream(new OutputStreamAdapter(outBuffer)));
		System.setErr(new PrintStream(new OutputStreamAdapter(errBuffer)));
	}
	
	
	public String getOutput() {
		return cleanString(outBuffer);
	}
	
	public String getErr() {
		return cleanString(errBuffer);
	}
	
	public void resetOutBuffer() {
		setOutBuffer(new StringBuffer());
	}
	
	public void resetErrBuffer() {
		setErrBuffer(new StringBuffer());
	}
	
	
	protected void tearDown() throws Exception {
		System.setOut(sysOut);
		System.setErr(sysErr);
	}
	
	private String cleanString(StringBuffer buffer) {
		
		StringBuffer sBuf = new StringBuffer();
		char[] cArray = buffer.toString().toCharArray();
		
		for (int i = 0; i < cArray.length; i++) {
			switch (cArray[i]) {
				case '\n':
					break;
				case '\r':
					break;
				default:
					sBuf.append(cArray[i]);
			}
		}
		
		return sBuf.toString();
	}
	
	private void setOutBuffer(StringBuffer outBuffer) {
		this.outBuffer = outBuffer;
	}
	
	private void setErrBuffer(StringBuffer errBuffer) {
		this.errBuffer = errBuffer;
	}
	
	public String buildMessage(String description, String cause) {
		StringBuffer sBuf = new StringBuffer();
		sBuf.append(description);
		sBuf.append("/n/t");
		sBuf.append("reason : " + cause);
		return sBuf.toString();
		
	}
}

class OutputStreamAdapter extends java.io.OutputStream {
	private StringBuffer buffer;
	
	public OutputStreamAdapter(StringBuffer buffer) {
		this.buffer = buffer;		
	}
	public void write(int b) throws IOException {
		buffer.append((char) b);
	}
}
