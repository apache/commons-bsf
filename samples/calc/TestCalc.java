import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.apache.bsf.*;
import org.apache.bsf.util.*;

public class TestCalc extends Frame {

  static final protected Hashtable extensionmap= new Hashtable();
  static
  {
    extensionmap.put("pl", "perlscript");
    extensionmap.put("vbs", "vbscript");
    extensionmap.put("js", "jscript");
  }
  static final String languageFromExtension( String s) throws Exception
  {
    int dot= s.lastIndexOf('.');
    if(dot != -1) s= s.substring( dot+1);
    String lang= (String) extensionmap.get(s);
    if(null == lang) throw new BSFException(BSFException.REASON_OTHER_ERROR, "File extension " + s + " unknown language!"); 
 
    return lang; 
    
  }

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
    f.show();

    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    } );
  }

}
