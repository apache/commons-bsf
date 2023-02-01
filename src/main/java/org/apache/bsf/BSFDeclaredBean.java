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

/**
 * BSFDeclaredBeans are used internally by BSF to encapsulate information being
 * passed between a BSFManager and its various BSFEngines. Note that the
 * constructor is not public because this is not a public class.
 */
public class BSFDeclaredBean {
    public final String name;
    public final Object bean;
    public final Class type;

    BSFDeclaredBean(final String name, final Object bean, final Class type) {
        this.name = name;
        this.bean = bean;
        this.type = type;
    }
}
