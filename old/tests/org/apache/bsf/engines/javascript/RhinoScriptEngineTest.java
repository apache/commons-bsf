
package org.apache.bsf.engines.javascript;

import java.io.StringReader;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleNamespace;
import javax.script.BSFTestCase;

/**
 * This is a testcase for RhinoScriptEngine.
 * 
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class RhinoScriptEngineTest extends BSFTestCase {
	private RhinoScriptEngine engine;
	
	public RhinoScriptEngineTest() {
		super("RhinoScriptEngineTest");
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		engine = new RhinoScriptEngine();
	}
	
	public void testCall() {
		StringReader reader = new StringReader(
				"function addOne(x) {return x+1;}");
		try {
			engine.eval(reader);
		} catch (ScriptException ex) {
			fail(buildMessage("RhinoScriptEngine.eval(): Unexpected " +
					"ScriptException ..", ex.getMessage()));
		}
		try {
			Object retVal = engine.call("addOne", null,
					new Object[]{new Double(0.0)});
			assertEquals(new Double(1.0), (Double)retVal);
			
		} catch (ScriptException ex) {
			fail(buildMessage("RhinoScriptEngine.call(): Unexpected " +
					"ScriptException ..", ex.getMessage()));
		}	
		resetOutBuffer();
	}
	
	public void testEval() {
		StringReader reader = new StringReader(
				"context.getWriter().println(\"PASSED\");");
		try {
			engine.eval(reader);
		} catch (ScriptException ex) {
			fail(buildMessage("RhinoScriptEngine.eval(): Unexpected " +
					"ScriptException ..", ex.getMessage()));
		}
		assertEquals("PASSED", getOutput());
		resetOutBuffer();
	}
	
	public void testCompileScript() {
		CompiledScript script = null;
		
		StringReader reader = new StringReader(
				"context.getWriter().println(\"PASSED\");");
		try {
			script = engine.compile(reader);
		} catch (ScriptException ex) {
			fail(buildMessage("RhinoScriptEngine.compileScript(): Unexpected " +
					"ScriptException ..", ex.getMessage()));
		}
		
		try {
			script.eval();
			String str = getOutput();
			assertEquals("PASSED", str);
		} catch (ScriptException ex) {
			fail(buildMessage("RhinoScriptEngine.compileScript(): Unexpected " +
					"ScriptException ..", ex.getMessage()));
		}
		resetOutBuffer();
	}
	
	public void testPut() {
		engine.put("x", new Integer(1));
		try {
			Object retValue = engine.eval(new StringReader("x;"));
			assertEquals(new Integer(1),retValue);
			
		} catch (ScriptException ex) {
			fail(buildMessage("RhinoScriptEngine.put(): Unexpected " +
					"ScriptException ..", ex.getMessage()));
		}
	}
	
	public void testGet() {
		try {
			engine.eval("var ENGINE_SCOPE = 100;" +
					"context.getNamespace(ENGINE_SCOPE).put(\"x\",10)");
			
		} catch (ScriptException ex) {
			fail(buildMessage("RhinoScriptEngine.get(): Unexpected " +
					"ScriptException ..", ex.getMessage()));
		}
		assertEquals(new Double(10), engine.get("x"));
	}
	
	public void testSetNamespace() {
		SimpleNamespace ns = new SimpleNamespace();
		ns.put("x", new Integer(11));
		engine.setNamespace(ns, ScriptContext.ENGINE_SCOPE);
		try {
			Object retValue = engine.eval("x;");
			assertEquals(new Integer(11), retValue);
		}catch (Exception ex) {
			fail(buildMessage("RhinoScriptEngine.setNamespace(): Unexpected " +
					"ScriptException ..", ex.getMessage()));
		}
	}
	
	protected void tearDown() throws Exception {
		engine = null;
		super.tearDown();
	}
}
