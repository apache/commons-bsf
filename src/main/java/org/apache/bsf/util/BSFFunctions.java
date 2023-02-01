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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bsf.util;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

/**
 * This is a utility that engine implementors may use as the Java
 * object they expose in the scripting language as "bsf". This has
 * essentially a subset of the methods in BSFManager plus some
 * stuff from the utils. Currently used by Javascript (Rhino) & BML.
 */
public class BSFFunctions {
  final BSFManager mgr;
  final BSFEngine engine;

  public BSFFunctions (final BSFManager mgr, final BSFEngine engine) {
    this.mgr = mgr;
    this.engine = engine;
  }
  public void addEventListener (final Object src, final String eventSetName,
                final String filter, final Object script)
       throws BSFException {
    EngineUtils.addEventListener (src, eventSetName, filter, engine,
                  mgr, "<event-binding>", 0, 0, script);
  }
  public  Object lookupBean (final String name) {
    return mgr.lookupBean (name);
  }
  public void registerBean (final String name, final Object bean) {
    mgr.registerBean (name, bean);
  }
  public void unregisterBean (final String name) {
    mgr.unregisterBean (name);
  }
}
