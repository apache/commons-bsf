/* This is a simple demo of a JavaScript script that uses the Java
   URL class to download some content from some URL. */

URL_ADDR = "http://www.cnn.com/";

/* use a Java bean to get at the URL */
java.lang.System.err.println ("Connecting to .. " + URL_ADDR);
url = new java.net.URL (URL_ADDR);

/* read the content */
java.lang.System.err.println ("Downloading .. ");
content = url.getContent ();
while ((ch = content.read ()) != -1) {
  java.lang.System.out.write (ch)
}
