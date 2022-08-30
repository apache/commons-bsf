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

package org.apache.bsf.util.event;

/**
 * <em>EventAdapterImpl</em> is a default implementation of the EventAdapter
 * interface that specific event adapters may choose to subclass from
 * instead of implementing the interface themselves. Saves 5 lines of code
 * mebbe.
 *
 * @see      EventAdapter
 */
public class EventAdapterImpl implements EventAdapter {
  protected EventProcessor eventProcessor;

  public void setEventProcessor (final EventProcessor eventProcessor) {
    this.eventProcessor = eventProcessor;
  }
}
