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
 *
 * @author   Rony G. Flatscher (added BSF_Log[Factory] to allow BSF to run without org.apache.commons.logging present)
 */

package org.apache.bsf.util;

import org.apache.bsf.BSF_Log;
import org.apache.bsf.BSF_LogFactory;

import java.io.IOException;

public class JavaUtils {
    // Temporarily copied from JavaEngine...

    private static final BSF_Log LOGGER;

    static {
                    // handle logger
                LOGGER = BSF_LogFactory.getLog((org.apache.bsf.util.JavaUtils.class).getName());
    }

    public static boolean JDKcompile(final String fileName, final String classPath) {
        final String option = (LOGGER.isDebugEnabled()) ? "-g" : "-O";
        final String args[] = { "javac", option, "-classpath", classPath, fileName };

        LOGGER.debug("JavaEngine: Compiling " + fileName);
        LOGGER.debug("JavaEngine: Classpath is " + classPath);

        try {
            final Process p = java.lang.Runtime.getRuntime().exec(args);
            p.waitFor();
            return (p.exitValue() != 0);
        } catch (final IOException e) {
            LOGGER.error("ERROR: IO exception during exec(javac).", e);
        } catch (final SecurityException e) {
            LOGGER.error("ERROR: Unable to create subprocess to exec(javac).",
                    e);
        } catch (final InterruptedException e) {
            LOGGER.error("ERROR: Wait for exec(javac) was interrupted.", e);
        }
        return false;
    }
}
