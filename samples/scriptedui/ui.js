
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
