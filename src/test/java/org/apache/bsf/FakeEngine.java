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

package org.apache.bsf;

import org.apache.bsf.util.BSFEngineImpl;

public class FakeEngine extends BSFEngineImpl {

    public Object call(final Object object, final String method, final Object[] args) throws BSFException {
        return Boolean.TRUE;
    }

    public Object eval(final String source, final int lineNo, final int columnNo, final Object expr) throws BSFException {
        return Boolean.TRUE;
    }

    public void iexec(final String source, final int lineNo, final int columnNo, final Object script) throws BSFException {
        System.out.print("PASSED");
    }

    public void exec(final String source, final int lineNo, final int columnNo, final Object script) throws BSFException {
        System.out.print("PASSED");
    }

    public void terminate() {
        super.terminate();
        System.out.print("PASSED");
    }
}
