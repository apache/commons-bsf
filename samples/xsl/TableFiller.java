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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import org.apache.bsf.*;
import org.apache.bsf.util.IOUtils;

/**
 * This is an example of using an XSL script to fill in a Java
 * table with data obtained from an XML file.
 */
public class TableFiller {
  public static void main (String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println ("Usage: java TableFiller xslfilename xmlfilename");
      System.exit (0);
    }
    String xslfilename = args[0];
    String xmlfilename = args[1];

    Frame frame = new Frame ("Table Filler");
    frame.addWindowListener (new WindowAdapter () {
      public void windowClosing (WindowEvent e) {
	System.exit (0);
      }
    });
    Panel panel = new Panel (new GridLayout (-1, 2));
    Font f = new Font ("SansSerif", Font.BOLD, 14);
    Label l = new Label ("First");
    l.setFont (f);
    panel.add (l);
    l = new Label ("Last");
    l.setFont (f);
    panel.add (l);
    frame.add ("Center", panel);

    BSFManager mgr = new BSFManager ();

    // make the panel available for playing in XSL
    mgr.declareBean ("panel", panel, panel.getClass ());

    // tell lotusxsl what the input XML file is
    mgr.registerBean ("xslt:src", new FileReader (xmlfilename));

    // load and run the xsl file to fill in the table. Note that we're
    // running the xsl script for its side effect of filling in the table
    // and so we don't care what the resulting document is.
    mgr.exec ("xslt", xslfilename, 0, 0,
	      IOUtils.getStringFromReader (new FileReader (xslfilename)));

    // display the frame
    frame.pack ();
    // frame.show(); // javac 1.5 warns to use f.show(), Apache build scripts abort as a result :(
    frame.setVisible(true);     // available since Java 1.1
  }
}
