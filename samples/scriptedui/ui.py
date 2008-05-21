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

