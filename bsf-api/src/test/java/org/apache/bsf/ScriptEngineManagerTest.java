/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.bsf;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.apache.bsf.utils.TestScriptEngine;
import org.apache.bsf.utils.TestScriptEngineFactory;

import junit.framework.TestCase;

public class ScriptEngineManagerTest extends TestCase {
	private ScriptEngineManager mgr = null;
	
	public ScriptEngineManagerTest() {
		super("ScriptEngineManagerTest");
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		mgr = new ScriptEngineManager();
	}

	public void testScriptEngineManager() {
		assertNotNull(mgr);
		List facs = mgr.getEngineFactories();
		assertNotNull(facs);
		assertTrue(facs.size() > 0); // need at least one
	}

	public void testGetPut() {
		mgr.put("x", new Integer(1));
		Object retValue = mgr.get("x");
		assertEquals(new Integer(1), retValue);
		try {
            mgr.get(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
		try {
            mgr.get("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertNull(mgr.get("missing_Key"));
        assertNull(mgr.get("null_Key"));
        mgr.put("null_Key", null);
        assertNull(mgr.get("null_Key"));
	}

	public void testGetEngineByExtension() {
		ScriptEngine engine;
		
		engine =  mgr.getEngineByExtension("tEst");
        assertNotNull(engine);
		assertTrue(engine instanceof TestScriptEngine);
		
		engine = mgr.getEngineByExtension("teSt");
        assertNotNull(engine);
		assertTrue(engine instanceof TestScriptEngine);
	}

	public void testGetEngineByMimeType() {
        ScriptEngine engine;
        engine =  mgr.getEngineByMimeType("application/junit");
        assertNotNull(engine);
        assertTrue(engine instanceof TestScriptEngine);
	}

	public void testGetEngineByName() {
		ScriptEngine engine;
		
		engine =  mgr.getEngineByName("JUnit");
		assertNotNull(engine);
		assertTrue(engine instanceof TestScriptEngine);
	}

	public void testGetEngineFactories() {
		boolean found = false;
		List factories = mgr.getEngineFactories();
		
		for(int i = 0; i < factories.size(); i++) {
			if (factories.get(i) instanceof TestScriptEngineFactory) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			fail("ScriptEngineManager.getEngineFactories(): " +
					"TestScriptEngineFactory is not present ..");
		}
	}

	public void testRegisterEngineExtension() {
        try {
            mgr.registerEngineExtension(null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            mgr.registerEngineExtension(null, new TestScriptEngineFactory());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            mgr.registerEngineExtension("", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        assertNull(mgr.getEngineByExtension("")); // not yet defined
        // Empty extensions are allowed
        mgr.registerEngineExtension("", new TestScriptEngineFactory());
        assertNotNull(mgr.getEngineByExtension("")); //now defined
        assertNull(mgr.getEngineByExtension("junit2")); // not yet defined
        mgr.registerEngineExtension("junit2", new TestScriptEngineFactory());
        assertNotNull(mgr.getEngineByExtension("junit2")); //now defined
	}

	public void testRegisterEngineName() {
        try {
            mgr.registerEngineName(null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            mgr.registerEngineName(null, new TestScriptEngineFactory());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            mgr.registerEngineName("", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        assertNull(mgr.getEngineByName("")); // not yet defined
        // Empty extensions are allowed
        mgr.registerEngineName("", new TestScriptEngineFactory());
        assertNotNull(mgr.getEngineByName("")); //now defined
        assertNull(mgr.getEngineByName("junit2")); // not yet defined
        mgr.registerEngineName("junit2", new TestScriptEngineFactory());
        assertNotNull(mgr.getEngineByName("junit2")); //now defined
	}

	public void testRegisterEngineMimeType() {
        try {
            mgr.registerEngineMimeType(null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            mgr.registerEngineMimeType(null, new TestScriptEngineFactory());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            mgr.registerEngineMimeType("", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        assertNull(mgr.getEngineByMimeType("")); // not yet defined
        // Empty extensions are allowed
        mgr.registerEngineMimeType("", new TestScriptEngineFactory());
        assertNotNull(mgr.getEngineByMimeType("")); //now defined
        assertNull(mgr.getEngineByMimeType("junit2")); // not yet defined
        mgr.registerEngineMimeType("junit2", new TestScriptEngineFactory());
        assertNotNull(mgr.getEngineByMimeType("junit2")); //now defined
	}

	public void testSetBindings() {
	    mgr.getBindings();
		try {
            mgr.setBindings(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        mgr.setBindings(new SimpleBindings());
	}

}
