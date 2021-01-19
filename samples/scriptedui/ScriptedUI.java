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

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import org.apache.bsf.*;
import org.apache.bsf.util.*;

/* This example shows how a Java app can allow a script to customize
a UI */
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
    mgr.registerBean ("parentFrame", f); // --rgf, 2006-08-08: to allow Jacl to get to frame ...

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
    // f.show(); // javac 1.5 warns to use f.show(), Apache build scripts abort as a result :(
    f.setVisible(true);     // available since Java 1.1
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
