# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

"""\
A silly little calculator implemented in JPython using
Java components for the UI.
Rony G. Flatscher, 2006-08-08
"""

import java
from java import awt

p = bsf.lookupBean('centerPanel')
p.setLayout ( awt.BorderLayout () )

p.add ("Center", java.awt.Label ("Middle from Jython"))
p.add ("North",  java.awt.TextField ("north text from Jython"))
p.add ("South",  java.awt.TextField ("south text from Jython"))
p.add ("East",   java.awt.Button ("inner east from Jython"))
p.add ("West",   java.awt.Button ("inner west from Jython"))

p.setBackground (java.awt.Color.orange)

f = p.getParent ()
f.setTitle ("Hello from Jython (title reset from Jython)")

