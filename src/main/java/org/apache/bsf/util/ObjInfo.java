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
 * An {@code ObjInfo} object is used by a compiler to track the name and type of a bean.
 */
public class ObjInfo {
    static private final String QUOTE_CHARS = "\'\"";
    static private final String EXEC_CHARS = "(=";
    public String objName;
    public Class objClass;

    public ObjInfo(final Class objClass, final String objName) {
        this.objClass = objClass;
        this.objName = objName;
    }

    public boolean isExecutable() {
        final char[] chars = objName.toCharArray();
        char openingChar = ' ';
        boolean inString = false, inEscapeSequence = false;

        for (int i = 0; i < chars.length; i++) {
            if (inEscapeSequence) {
                inEscapeSequence = false;
            } else if (QUOTE_CHARS.indexOf(chars[i]) != -1) {
                if (!inString) {
                    openingChar = chars[i];
                    inString = true;
                } else {
                    if (chars[i] == openingChar) {
                        inString = false;
                    }
                }
            } else if (EXEC_CHARS.indexOf(chars[i]) != -1) {
                if (!inString) {
                    return true;
                }
            } else if (inString && chars[i] == '\\') {
                inEscapeSequence = true;
            }
        }

        return false;
    }

    public boolean isValueReturning() {
        return (objClass != void.class && objClass != Void.class);
    }

    public String toString() {
        return StringUtils.getClassName(objClass) + " " + objName;
    }
}
