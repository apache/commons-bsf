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

	public void testGet() {
		mgr.put("x", new Integer(1));
		Object retValue = mgr.get("x");
		assertEquals(new Integer(1), retValue);
	}

	public void testGetEngineByExtension() {
		ScriptEngine engine;
		
		engine =  mgr.getEngineByExtension("tEst");
		assertTrue(engine instanceof TestScriptEngine);
		
		engine = mgr.getEngineByExtension("teSt");
		assertTrue(engine instanceof TestScriptEngine);
	}

	public void testGetEngineByMimeType() {
	}

	public void testGetEngineByName() {
		ScriptEngine engine;
		
		engine =  mgr.getEngineByName("TestScript");
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

	public void testPut() {
		//TODO Implement put().
	}

	public void testRegisterEngineExtension() {
		//TODO Implement registerEngineExtension().
	}

	public void testRegisterEngineName() {
		//TODO Implement registerEngineName().
	}

	public void testRegisterEngineMimeType() {
		//TODO Implement registerEngineMimeType().
	}

	public void testSetNamespace() {
		//TODO Implement setNamespace().
	}

}
