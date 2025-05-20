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

import java.util.Hashtable;

/**
 * The <em>ObjectRegistry</em> is used to do name-to-object reference lookups. If an <em>ObjectRegistry</em> is passed as a constructor argument, then this
 * <em>ObjectRegistry</em> will be a cascading registry: when a lookup is invoked, it will first look in its own table for a name, and if it's not there, it
 * will cascade to the parent <em>ObjectRegistry</em>. All registration is always local. [??]
 */
public class ObjectRegistry {
    Hashtable reg = new Hashtable();
    ObjectRegistry parent = null;

    public ObjectRegistry() {
    }

    public ObjectRegistry(final ObjectRegistry parent) {
        this.parent = parent;
    }

    // lookup an object: cascade up if needed
    public Object lookup(final String name) throws IllegalArgumentException {
        Object obj = reg.get(name);

        if (obj == null && parent != null) {
            obj = parent.lookup(name);
        }

        if (obj == null) {
            throw new IllegalArgumentException("object '" + name + "' not in registry");
        }

        return obj;
    }

    // register an object
    public void register(final String name, final Object obj) {
        reg.put(name, obj);
    }

    // unregister an object (silent if unknown name)
    public void unregister(final String name) {
        reg.remove(name);
    }
}
