package javax.script;

/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *
 */
public class ScriptEngineManagerTest extends BSFTestCase {
	private ScriptEngineManager mgr = null;
	
	public ScriptEngineManagerTest() {
		super("ScriptEngineManagerTest");
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		mgr = new ScriptEngineManager();
	}

	public void testScriptEngineManager() {
		//TODO Implement ScriptEngineManager().
	}

	public void testGet() {
		mgr.put("x", new Integer(1));
		Object retValue = mgr.get("x");
		assertEquals(new Integer(1), retValue);
	}

	public void testGetEngineByExtension() {
		ScriptEngine engine;
		
		engine =  mgr.getEngineByExtension("tEst");
		assertTrue(engine instanceof 
				org.apache.bsf.engines.testscript.TestScriptEngine);
		
		engine = mgr.getEngineByExtension("teSt");
		assertTrue(engine instanceof 
				org.apache.bsf.engines.testscript.TestScriptEngine);
	}

	public void testGetEngineByMimeType() {
	}

	public void testGetEngineByName() {
		ScriptEngine engine;
		
		engine =  mgr.getEngineByName("TestScript");
		assertTrue(engine instanceof 
				org.apache.bsf.engines.testscript.TestScriptEngine);
	}

	public void testGetEngineFactories() {
		boolean found = false;
		ScriptEngineFactory[] factories = mgr.getEngineFactories();
		
		for(int i = 0; i < factories.length; i++) {
			if (factories[i] instanceof 
					org.apache.bsf.engines.testscript.TestScriptEngineFactory) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			fail("ScriptEngineManager.getEngineFactories(): " +
					"TestScriptEngineFactory is not present ..");
		}
	}

	public void testGetNamespace() {
		//TODO Implement getNamespace().
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
