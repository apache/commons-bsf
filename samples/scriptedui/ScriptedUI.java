/* This example shows how a Java app can allow a script to customize
   a UI */

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import org.apache.bsf.*;
import org.apache.bsf.util.*;

public class ScriptedUI {
  BSFManager mgr = new BSFManager ();

  public ScriptedUI (String fileName) {
    Frame f = new Frame ("Application's Main Frame");
    f.addWindowListener (new WindowAdapter () {
      public void windowClosing (WindowEvent e) {
	System.exit (0);
      }
    });

    Panel p = new Panel ();
    f.add ("Center", p);
    f.add ("North", new Button ("North Button"));
    f.add ("South", new Button ("South Button"));

    mgr.registerBean ("centerPanel", p);

    // exec script engine code to do its thing for this
    try {
      String language = BSFManager.getLangFromFilename (fileName);
      FileReader in = new FileReader (fileName);
      String script = IOUtils.getStringFromReader (in);

      mgr.exec (language, fileName, -1, -1, script);
    } catch (BSFException e) {
      System.err.println ("Ouch: " + e.getMessage ());
      e.printStackTrace ();
    } catch (IOException e) {
      System.err.println ("Ouch: " + e.getMessage ());
      e.printStackTrace ();
    }

    // now pack and show the frame
    f.pack ();
    f.show ();
  }
  
  public static void main (String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println ("Usage: java ScriptedUI filename");
      System.err.println ("       where filename is the name of the script");
      System.exit (1);
    }
    new ScriptedUI (args[0]);
  }
}
