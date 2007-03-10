
package org.apache.bsf.engines.javascript;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.script.http.GenericHttpScriptContext;


public class RhinoScriptContext extends GenericHttpScriptContext{
	private RhinoDetagfier detagfier;
	
	public RhinoScriptContext(){
		super();
		detagfier = new RhinoDetagfier();
    }
	
	public Reader getScriptSource() {
		try {
			String cleanString  = detagfier.getDetagfiedString(
					super.getScriptSource());
			return new StringReader(cleanString);
		} catch (IOException ex) {
			System.err.println("Error : " + ex.getMessage());
		}
		return null;
	}
}
