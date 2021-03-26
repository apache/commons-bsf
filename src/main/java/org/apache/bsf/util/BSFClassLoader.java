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

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;

/**
 * This class loader knows to load a class from the tempDir dir
 * of the environment of the given manager.
 *
 * @author   Sanjiva Weerawarana
 */
class BSFClassLoader extends ClassLoader {
  final Hashtable cache = new Hashtable ();
  String tempDir = ".";

  // note the non-public constructor - this is only avail within
  // this package.
  BSFClassLoader () {
  }
  public synchronized Class loadClass (final String name, final boolean resolve)
                                               throws ClassNotFoundException {
    Class c = (Class) cache.get (name);
    if (c == null) {
      // is it a system class
      try {
    c = findSystemClass (name);
    cache.put (name, c);
    return c;
      } catch (final ClassNotFoundException e) {
    // nope
      }
      try {
    final byte[] data = loadClassData (name);
    c = defineClass (name, data, 0, data.length);
    cache.put (name, c);
      } catch (final Exception e) {
    e.printStackTrace ();
    throw new ClassNotFoundException ("unable to resolve class '" +
                      name + "'");
      }
    }
    if (resolve) {
        resolveClass (c);
    }
    return c;
  }
  private byte[] loadClassData (final String name) throws Exception {
    final String fileName = tempDir + File.separatorChar + name + ".class";
    final FileInputStream fi = new FileInputStream (fileName);
    final byte[] data = new byte[fi.available ()];
    fi.read (data);
    fi.close();
    return data;
  }
  public void setTempDir (final String tempDir) {
    this.tempDir = tempDir;
  }
}
