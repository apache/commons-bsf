/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package javax.script;

/**
 * Generic Exception class for the Scripting APIs.
 *
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public class ScriptException extends Exception {

    private static final long serialVersionUID = 2L;

    /** Stores the file name of the script */
    private final String fileName; // default null

    /** 
     * Stores the line number of the script in which the error has 
     * occured
     */ 
    private final int lineNumber; // default = -1;

    /** 
     * Stores the column number of the script in which the error has 
     * occured
     */
    private final int columnNumber; // default = -1;

    /** Stores the message which describes the cause of error */
    private final String message; // default null

    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param exception the cause of exception
     */
    public ScriptException(Exception exception) {
              super(exception);
            this.message = null;
            this.fileName = null;
            this.lineNumber = -1;
            this.columnNumber = -1;
    }

    /**
     * Constructs a new exception with the specified detailed 
     * message.
     *  
     * @param message the datailed message which caused the 
     *        exception 
     */
    public ScriptException(String message) {
        this(message, null, -1, -1);
    }

    /**
     * Constructs a new exception with the spcified detailed message 
     * of cause, the file name of the source of script and the line 
     * number of the script where the error has occured.
     * 
     * @param message    the detailed message of cause of exception
     * @param fileName   the file name which contains the script
     * @param lineNumber the line number of the script where the error has 
     *                   occured
     */
    public ScriptException(String message,String fileName,int lineNumber) {
        this(message, fileName, lineNumber, -1);
    }

    /**
     * Constructs a new exception using the detailed message of 
     * cause, file name which contains the script, line number and
     * column number in which the error has occured.
     *  
     * @param message      the detailed message of the cause of 
     *                     exception
     * @param fileName     the name of the file which contains the
     *                     script
     * @param lineNumber   the line number of the script where the 
     *                     error has occured
     * @param columnNumber the column number of the script where the
     *                     error has occured
     */
    public ScriptException(String message, String fileName, int lineNumber, int columnNumber) {
        super(message);
        this.message = message;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    /**
     * Retrieves the file name in which the script is contained.
     * 
     * @return Returns the file name in which the script is contained
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Retrieves the column number of the script where the error has 
     * occured. If the information is not available, returns -1.
     * 
     * @return Returns the column number of the script where the 
     *         error has occured
     */
    public int getColumnNumber() {        
        return columnNumber;
    }

    /**
     * Retrieves the line number of the script where the error has 
     * occured. If the information is not available, returns -1.
     * 
     * @return Returns the line number of the script where the error 
     *         has occured
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Retrieves a String describing the cause of error.
     * 
     * @return a String describing the cause of error
     */
    public String getMessage(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(message);
        if (fileName != null) {
            buffer.append("in: " + fileName);
        }
        if (lineNumber != -1) {
            buffer.append("at line no: " + lineNumber);
        }
        if (columnNumber != -1) {
            buffer.append("at column number: " + columnNumber);
        }
        return buffer.toString();        
    }
}
