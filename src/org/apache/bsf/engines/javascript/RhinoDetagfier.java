package org.apache.bsf.engines.javascript;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *
 */
public class RhinoDetagfier {
	
	/** */
	public static final String OUTPUT_START = "context.getWriter().println(\"";
	/** */
	public static final String OUTPUT_END = "\");";
	
	public RhinoDetagfier() {	
	}
	
	public String getDetagfiedString(Reader source) throws IOException{
		
		StringBuffer sBuf = new StringBuffer();
		int value = 0, state = 0;
		
		while ((value = source.read()) != -1) {
			state = append(state, (char)value, sBuf);
		}
		if (state == 4) {
			sBuf.append(OUTPUT_END);
		}
		
		return sBuf.toString();		
	}
	
	private int append(int state, char c, StringBuffer buf) {
		switch (state) {
			case 0:
				if (c == '<') {
					return 1;
				} else {
					return 0;
				}
			case 1:
				if (c =='%') {
					buf.append('\n');
					return 2;
				} else {
					buf.append(OUTPUT_START);
					buf.append('<');
					buf.append(c);
					return 4;
				}
			case 2:
				if (c == '%') {
					return 3;
				} else {
					buf.append(c);
					return 2;
				}
			case 3:
				if (c == '>') {
						buf.append('\n');
					return 0;
				} else {
					buf.append('%');
					return 2;
				}
			case 4:
				if (c == '<') {
					return 5;
				} else {
					switch (c) {
						case '\"':
							buf.append("\\\"");
							break;
						case '\n':
							buf.append(OUTPUT_END);
							buf.append(c);
							buf.append(OUTPUT_START);
							break;
						case '\r':
							// ignor
							break;
						default:
							buf.append(c);
					}
					return 4;
				}
			case 5:
				if (c == '%') {
					buf.append(OUTPUT_END);
					buf.append('\n');
					return 2;
				} else {
					buf.append('<');
					buf.append(c);
					return 4;
				}
			default: return -1;
		}
	}	
}
