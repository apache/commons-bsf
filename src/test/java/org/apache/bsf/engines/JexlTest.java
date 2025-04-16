/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.bsf.engines;

import org.apache.bsf.engines.jexl.JEXLEngine;
import org.apache.commons.jexl3.introspection.JexlPermissions;

public class JexlTest extends JavascriptTest {
  public JexlTest(String name) {
    super(name);
  }

  public void setUp() {
  super.setUp();
  try {
    JEXLEngine.setPermissions(JexlPermissions.UNRESTRICTED);
    engine = bsfManager.loadScriptingEngine("jexl");
  } catch (final Exception e) {
    fail(failMessage("Failure attempting to load jexl engine", e));
  }
}


  public void testCall() {
    final Object[] args = { Double.valueOf(1) };
    Double retval = null;

    try {
      engine.exec("Test.js", 0, 0, "addOne  = (f) -> {\n return f + 1;\n}");
      retval = Double.valueOf((engine.call(null, "addOne", args).toString()));
    } catch (final Exception e) {
      fail(failMessage("call() test failed", e));
    }

    assertEquals(Double.valueOf(2), retval);
  }
}
