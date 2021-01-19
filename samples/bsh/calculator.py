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
"""

import java
from java import awt

def exit(e): java.lang.System.exit(0)

def getField (f):
	t = f.getText ()
	if t == '':
		return 0
	else:
		return java.lang.Integer.parseInt (t)

def doMath (e):
	n1 = getField (f1)
	n2 = getField (f2)
	sum.setText (repr (n1 + n2))
	diff.setText (repr (n1 - n2))
	prod.setText (repr (n1 * n2))
	quo.setText (repr (n1 / n2))

f = awt.Frame ('BSH Calculator (jpython)', windowClosing=exit)

f1 = awt.TextField (20, actionPerformed=doMath)
f2 = awt.TextField (20, textValueChanged=doMath)

p = awt.Panel ()
p.setLayout (awt.GridLayout (2, 2))
p.add (awt.Label ('Enter Operand'))
p.add (f1)
p.add (awt.Label ('Enter Operand'))
p.add (f2)

f.add ('North', p)

f.add ("Center", awt.Label ('Results:'))

p = awt.Panel ()
p.setLayout (awt.GridLayout (4, 2))
p.add (awt.Label ('Sum'))
sum = awt.TextField (20)
p.add (sum)
p.add (awt.Label ('Difference'))
diff = awt.TextField (20)
p.add (diff)
p.add (awt.Label ('Product'))
prod = awt.TextField (20)
p.add (prod)
p.add (awt.Label ('Quotient'))
quo = awt.TextField (20)
p.add (quo)
f.add ('South', p)

f.pack ()
f.show ()
f.toFront ()
