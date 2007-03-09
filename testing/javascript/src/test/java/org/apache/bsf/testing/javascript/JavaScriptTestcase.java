package org.apache.bsf.testing.javascript;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;


public class JavaScriptTestcase extends TestCase {
	
	public void testEval() throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByExtension("js");
		assertTrue(((Boolean)engine.eval("true;")).booleanValue());
		assertFalse(((Boolean)engine.eval("false;")).booleanValue());
	}

	public void testInvokeFunction() throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByExtension("js");
		engine.eval("function hello(s) { return 'Hello ' + s; }" );
		assertTrue(engine instanceof Invocable);
		Invocable invocableScript = (Invocable) engine;
		assertEquals("Hello petra", invocableScript.invokeFunction("hello", new Object[]{"petra"}));
	}

	public void testInvokeMethod() throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByExtension("js");
		engine.eval("function hello(s) { return 'Hello ' + s; }" );
		assertTrue(engine instanceof Invocable);
		Invocable invocableScript = (Invocable) engine;
		
		Object thiz = engine.eval("this;");
		assertEquals("Hello petra", invocableScript.invokeMethod(thiz, "hello", new Object[]{"petra"}));
	}

}
