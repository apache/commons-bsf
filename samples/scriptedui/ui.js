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

/* pick up the center panel bean */
p = bsf.lookupBean ("centerPanel");

/* set the layout manager to border */
p.setLayout (new java.awt.BorderLayout ());

/* add a few things */
p.add ("Center", new java.awt.Label ("Middle from JavaScript"));
p.add ("North", new java.awt.TextField ("north text from JavaScript"));
p.add ("South", new java.awt.TextField ("south text from JavaScript"));
p.add ("East", new java.awt.Button ("inner east from JavaScript"));
p.add ("West", new java.awt.Button ("inner west from JavaScript"));

/* configure p a bit */
p.setBackground (java.awt.Color.red);

/* configure the frame that p is in */
f = p.getParent ();
f.setTitle ("Hello from JavaScript (title reset from JavaScript)");
