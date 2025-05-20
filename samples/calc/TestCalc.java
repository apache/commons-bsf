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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.apache.bsf.*;
import org.apache.bsf.util.*;

public class TestCalc extends Frame {

  public TestCalc (String fileName) throws Exception {
    BSFManager manager = new BSFManager ();
    manager.declareBean("frame", this, this.getClass());
    try
    {
     manager.exec(manager.getLangFromFilename(fileName), fileName, 0, 0,
                 IOUtils.getStringFromReader(new FileReader(fileName)));
    }catch(BSFException e )
    {

     System.out.println("exception: " + e.getMessage());
     Throwable oe= e.getTargetException();
     if(null != oe) System.out.println("\nOriginal Exception:"+ oe.getMessage());
              e.printStackTrace();

    }
  }

  public static void main (String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Missing file name");
      System.exit(1);
    }

    Frame f = new TestCalc(args[0]);
    // f.show(); // javac 1.5 warns to use f.show(), Apache build scripts abort as a result :(
    f.setVisible(true);     // available since Java 1.1

    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    } );
  }

}
