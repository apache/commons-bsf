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

/* A silly little calculator implemented in Javascript (Rhino) using
   Java components for the UI. */


f = new java.awt.Frame ("BSH Calculator (js)");
bsf.addEventListener (f, "window", "windowClosing",
                      "java.lang.System.exit (0);");

f1 = new java.awt.TextField (20);
bsf.addEventListener (f1, "action", null, "doMath ()");
f2 = new java.awt.TextField (20);
bsf.addEventListener (f2, "text", null, "doMath ()");

p = new java.awt.Panel ();
p.setLayout (new java.awt.GridLayout (2, 2));
p.add (new java.awt.Label ("Enter Operand"));
p.add (f1);
p.add (new java.awt.Label ("Enter Operand"));
p.add (f2);

f.add ("North", p);

f.add ("Center", new java.awt.Label ("Results:"));

p = new java.awt.Panel ();
p.setLayout (new java.awt.GridLayout (4, 2));
p.add (new java.awt.Label ("Sum"));
p.add (sum = new java.awt.TextField (20))
p.add (new java.awt.Label ("Difference"));
p.add (diff = new java.awt.TextField (20));
p.add (new java.awt.Label ("Product"));
p.add (prod = new java.awt.TextField (20));
p.add (new java.awt.Label ("Quotient"));
p.add (quo = new java.awt.TextField (20));
f.add ("South", p);

f.pack ();
f.show ();
f.toFront ();

function getField (f) {
  t = f.getText ();
  return (t == "") ? 0 : java.lang.Integer.parseInt (t);
}

function doMath () {
  n1 = getField (f1);
  n2 = getField (f2);
  sum.setText ((n1 + n2) + "");
  diff.setText ((n1 - n2) + "");
  prod.setText ((n1 * n2) + "");
  quo.setText ((n1 / n2) + "");
}
