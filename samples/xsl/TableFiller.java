/**
 * This is an example of using an XSL script to fill in a Java 
 * table with data obtained from an XML file. 
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import org.apache.bsf.*;
import org.apache.bsf.util.IOUtils;

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

    // tell lotusxsl what the input xml file is
    mgr.registerBean ("xslt:src", new FileReader (xmlfilename));

    // load and run the xsl file to fill in the table. Note that we're
    // running the xsl script for its side effect of filling in the table
    // and so we don't care what the resulting document is.
    mgr.exec ("xslt", xslfilename, 0, 0, 
	      IOUtils.getStringFromReader (new FileReader (xslfilename)));

    // display the frame
    frame.pack ();
    frame.show ();
  }
}
