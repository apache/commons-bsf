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
