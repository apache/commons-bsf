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

package org.apache.bsf.util.event.adapters;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.apache.bsf.util.event.EventAdapterImpl;

public class java_awt_event_FocusAdapter extends EventAdapterImpl implements FocusListener {

    public void focusGained(final FocusEvent e) {
        eventProcessor.processEvent("focusGained", new Object[] { e });
    }

    public void focusLost(final FocusEvent e) {
        eventProcessor.processEvent("focusLost", new Object[] { e });
    }
}
