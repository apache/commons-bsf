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

/** This class is used in BSF as BSF_LogFactory returning a BSF_Log instance, which is
 * a delegator for an <code>org.apache.commons.logging.Log</code> object.
 *
 It implements the static <code>org.apache.commons.logging.LogFactory.getLog({String|Class} object)</code>
 * methods which return an instance of the class <code>org.apache.bsf.BSF_Log</code>, which in
 * turn implements all the methods of the <code>org.apache.commons.logging.Log</code> interface class.
 *
*/

public class BSF_LogFactory
{
    protected BSF_LogFactory() {}              // mimickries org.apache.commons.logging.LogFactory

    static public BSF_Log getLog (final String name)
    {
        return new BSF_Log(name);
    }

    static public BSF_Log getLog (final Class clz)
    {
        return new BSF_Log(clz);
    }
}

