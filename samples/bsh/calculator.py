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

f = awt.Frame ('BSH Calculator', windowClosing=exit)

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
