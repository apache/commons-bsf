
package javax.script;

import org.apache.bsf.engines.javascript.RhinoScriptEngineTest;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class BSFTest extends BSFTestCase {
	private static String[] testNames;
	public BSFTest() {
		super("BSFTest");
	}
	
	public static void main(String[] args) {
		TestRunner runner = new TestRunner();
		TestSuite suite = (TestSuite) suite();
		TestResult result;
		
		for (int i = 0; i < suite.testCount(); i++) {
			System.out.print(testNames[i]);
			result = runner.doRun(suite.testAt(i), false);
			String resultString = "Tests run: " + result.runCount() +
					", Failures: "			+ result.failureCount() +", Errors: " +
					result.errorCount();
			System.out.println(resultString);
			System.out.println(getCharsString('-', resultString.length()));
			System.out.println();
		}
		
		System.out.println();
	}
	
	public static Test suite() {
		testNames = new String[2];
		TestSuite suite = new TestSuite();
		testNames[0] = "ScriptEngineManagerTest";
		suite.addTestSuite(ScriptEngineManagerTest.class);
		testNames[1] = "RhinoScriptEngineTest";
		suite.addTestSuite(RhinoScriptEngineTest.class);
		return suite;
	}
	
	public static String getCharsString(char c, int num) {
		StringBuffer sBuf = new StringBuffer();
		for (int i = 0; i < num; i++) {
			sBuf.append(c);
		}
		return sBuf.toString();
	}

}
