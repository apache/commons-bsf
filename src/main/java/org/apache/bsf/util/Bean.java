/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bsf.util;

/**
 * A <em>Bean</em> is the class used to represent a bean: it holds a type and a value. This is needed because otherwise we can't represent the types of
 * null-valued beans (or primitives) correctly. This was originally in the BML player.
 */
public class Bean {
    // type of this bean
    public Class type;

    // its current value (mebbe null)
    public Object value;

    public Bean(final Class type, final Object value) {
        this.type = type;
        this.value = value;
    }
}
