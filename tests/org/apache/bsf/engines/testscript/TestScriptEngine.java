package org.apache.bsf.engines.testscript;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

import javax.script.GenericScriptEngine;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *
 */
public class TestScriptEngine extends GenericScriptEngine {

	public Object eval(Reader reader, ScriptContext context)
			throws ScriptException {
		PrintWriter pw = new PrintWriter(context.getWriter());
		pw.println("eval():PASSED");
		pw.flush();
		return Boolean.TRUE;
	}
	
	public Object eval(String script, ScriptContext context)
			throws ScriptException {
		return eval(new StringReader(script), context);
	}

	public ScriptEngineFactory getFactory() {
		return new TestScriptEngineFactory();
	}

}
