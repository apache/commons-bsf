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

package org.apache.bsf.util.event;

/**
 * <em>EventAdapter</em> is the interface that all event adapters must implement in order to work with the automatic event adapter generation model. This
 * interface requires that the adapter implement a method that allows setting the event processor delegated to process the event after the adapter has received
 * the event from the event source. The task of any event adapter is to receive the event and then delegate it to the event processor assigned to it, using
 * either eventProcessor.processEvent or eventProcessor.processExceptionableEvent.
 *
 * @see EventProcessor
 */
public interface EventAdapter {
    void setEventProcessor(EventProcessor eventProcessor);
}
