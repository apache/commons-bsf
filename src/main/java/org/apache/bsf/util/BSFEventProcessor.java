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
 *
 * 2012-01-15, Rony G. Flatscher
 *     - make event name comparisons case independent, Jira issue [BSF-19]
 */

package org.apache.bsf.util;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.event.EventProcessor;

/**
 * This is used to support binding scripts to be run when an event occurs.
 */
public class BSFEventProcessor implements EventProcessor {
    BSFEngine engine;
    BSFManager manager;
    String filter;
    String source;
    int lineNo;
    int columnNo;
    Object script;

    /**
     * Package-protected constructor makes this class unavailable for public use.
     */
    BSFEventProcessor(final BSFEngine engine, final BSFManager manager, final String filter, final String source, final int lineNo, final int columnNo,
            final Object script) throws BSFException {
        this.engine = engine;
        this.manager = manager;
        this.filter = filter;
        this.source = source;
        this.lineNo = lineNo;
        this.columnNo = columnNo;
        this.script = script;
    }
    //////////////////////////////////////////////////////////////////////////
    //
    // event is delegated to me by the adapters using this. inFilter is
    // in general the name of the method via which the event was received
    // at the adapter. For prop/veto change events, inFilter is the name
    // of the property. In any case, in the event processor, I only forward
    // those events if for which the filters match (if one is specified).

    public void processEvent(final String inFilter, final Object[] evtInfo) {
        try {
            processExceptionableEvent(inFilter, evtInfo);
        } catch (final RuntimeException re) {
            // rethrow this .. I don't want to intercept run-time stuff
            // that can in fact occur legit
            throw re;
        } catch (final Exception e) {
            // should not occur
            System.err.println("BSFError: non-exceptionable event delivery " + "threw exception (that's not nice): " + e);
            e.printStackTrace();
        }
    }
    //////////////////////////////////////////////////////////////////////////
    //
    // same as above, but used when the method event method may generate
    // an exception which must go all the way back to the source (as in
    // the vetoableChange case)

    public void processExceptionableEvent(final String inFilter, final Object[] evtInfo) throws Exception {
        if ((filter != null) && !isFilteredEvent(filter, inFilter)) {
            // ignore this event
            return;
        }

        // run the script
        engine.exec(source, lineNo, columnNo, script);
// System.err.println("returned from engine.exec.");
    }

    private static boolean isFilteredEvent(final String filter, final String inFilter) {
        boolean bRes = filter.equalsIgnoreCase(inFilter);
        if (bRes) {
            return bRes;
        }

        final String[] chunks = filter.replace('+', ' ').split(" ");
        for (int i = 0; i < chunks.length; i++) {
            bRes = chunks[i].equalsIgnoreCase(inFilter);
            if (bRes) {
                return bRes;
            }
        }
        return bRes;
    }
}
