/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Apache BSF", "Apache", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from
 *    this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * Sanjiva Weerawarana and others at International Business Machines
 * Corporation. For more information on the Apache Software Foundation,
 * please see <http://www.apache.org/>.
 */

package org.apache.bsf;

import java.util.*;
import java.io.*;
import java.beans.*;
import java.security.*;

import org.apache.bsf.util.*;
import org.apache.bsf.util.DebugLog;

import javax.naming.*;

/**
 * This class is the entry point to the bean scripting framework. An
 * application wishing to integrate scripting to a Java app would 
 * place an instance of a BSFManager in their code and use its services
 * to register the beans they want to make available for scripting,
 * load scripting engines, and run scripts. 
 * <p>
 * BSFManager serves as the registry of available scripting engines
 * as well. Loading and unloading of scripting engines is
 * supported as well. Each BSFManager loads one engine per language.
 * Several BSFManagers can be created per JVM.
 * 
 * @author   Sanjiva Weerawarana
 * @author   Matthew J. Duftler
 * @author   Sam Ruby
 * @author   Olivier Gruber (added original debugging support)
 */
public class BSFManager {
    // table of registered scripting engines
    protected static Hashtable registeredEngines = new Hashtable();

    // mapping of file extensions to languages
    protected static Hashtable extn2Lang = new Hashtable();

    // table of scripting engine instances created by this manager. 
    // only one instance of a given language engine is created by a single
    // manager instance.
    protected Hashtable loadedEngines = new Hashtable();

    // table of registered beans for use by scripting engines.
    protected ObjectRegistry objectRegistry = new ObjectRegistry();

    // prop change support containing loaded engines to inform when any
    // of my interesting properties change
    protected PropertyChangeSupport pcs;

    // the class loader to use if a class loader is needed. Default is
    // he who loaded me (which may be null in which case its Class.forName).
    protected ClassLoader classLoader = getClass().getClassLoader();

    // temporary directory to use to dump temporary files into. Note that
    // if class files are dropped here then unless this dir is in the 
    // classpath or unless the classloader knows to look here, the classes
    // will not be found.
    protected String tempDir = ".";

    // classpath used by those that need a classpath
    protected String classPath;

    // stores BSFDeclaredBeans representing objects 
    // introduced by a client of BSFManager
    protected Vector declaredBeans = new Vector();

    //////////////////////////////////////////////////////////////////////
    //
    // pre-register engines that BSF supports off the shelf
    //
    //////////////////////////////////////////////////////////////////////

    static {
        try {
            ResourceBundle rb =
                ResourceBundle.getBundle("org.apache.bsf.Languages");
            Enumeration keys = rb.getKeys();
            
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = rb.getString(key);
                
                StringTokenizer tokens = new StringTokenizer(value, ",");
                String className = (String) tokens.nextToken();
                
                // get the extensions for this language
                String exts = (String) tokens.nextToken();
                StringTokenizer st = new StringTokenizer(exts, "|");
                String[] extensions = new String[st.countTokens()];
                for (int i = 0; st.hasMoreTokens(); i++) {
                    extensions[i] = ((String) st.nextToken()).trim();
                }
                
                registerScriptingEngine(key, className, extensions);
            }
        }
        catch (NoSuchElementException nsee) {
            nsee.printStackTrace();
            System.err.println("Syntax error in Languages resource bundle");
        } 
        catch (MissingResourceException mre) {
            mre.printStackTrace();
            System.err.println("Initialization error: " + mre.toString());
        }
    }

    public BSFManager() {
        pcs = new PropertyChangeSupport(this);
    }

    /**
     * Apply the given anonymous function of the given language to the given
     * parameters and return the resulting value.
     *
     * @param lang language identifier
     * @param source (context info) the source of this expression
     (e.g., filename)
     * @param lineNo (context info) the line number in source for expr
     * @param columnNo (context info) the column number in source for expr
     * @param funcBody the multi-line, value returning script to evaluate
     * @param paramNames the names of the parameters above assumes
     * @param arguments values of the above parameters
     *
     * @exception BSFException if anything goes wrong while running the script
     */
    public Object apply(String lang,
                        String source,
                        int lineNo,
                        int columnNo,
                        Object funcBody,
                        Vector paramNames,
                        Vector arguments)
        throws BSFException {
        final BSFEngine e = loadScriptingEngine(lang);
        final String sourcef = source;
        final int lineNof = lineNo, columnNof = columnNo;
        final Object funcBodyf = funcBody;
        final Vector paramNamesf = paramNames;
        final Vector argumentsf = arguments;
        Object result = null;

        try {
            final Object resultf = 
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws Exception {
                            return e.apply(sourcef, lineNof, columnNof, 
                                           funcBodyf, paramNamesf, argumentsf);
                        }
                    });
            result = resultf;
        }
        catch (PrivilegedActionException prive) {
            throw (BSFException) prive.getException();
        }

        return result;
    }

    /**
     * Compile the application of the given anonymous function of the given
     * language to the given parameters into the given <tt>CodeBuffer</tt>.
     *
     * @param lang language identifier
     * @param source (context info) the source of this expression
     (e.g., filename)
     * @param lineNo (context info) the line number in source for expr
     * @param columnNo (context info) the column number in source for expr
     * @param funcBody the multi-line, value returning script to evaluate
     * @param paramNames the names of the parameters above assumes
     * @param arguments values of the above parameters
     * @param cb       code buffer to compile into
     *
     * @exception BSFException if anything goes wrong while running the script
     */
    public void compileApply(String lang,
                             String source,
                             int lineNo,
                             int columnNo,
                             Object funcBody,
                             Vector paramNames,
                             Vector arguments,
                             CodeBuffer cb)
        throws BSFException {
        final BSFEngine e = loadScriptingEngine(lang);
        final String sourcef = source;
        final int lineNof = lineNo, columnNof = columnNo;
        final Object funcBodyf = funcBody;
        final Vector paramNamesf = paramNames;
        final Vector argumentsf = arguments;
        final CodeBuffer cbf = cb;
        
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        e.compileApply(sourcef, lineNof, columnNof, 
                                       funcBodyf, paramNamesf, 
                                       argumentsf, cbf);
                        return null;
                    }
                });
        }
        catch (PrivilegedActionException prive) {
            throw (BSFException) prive.getException();
        }
    }

    /**
     * Compile the given expression of the given language into the given 
     * <tt>CodeBuffer</tt>.
     *
     * @param lang     language identifier
     * @param source   (context info) the source of this expression
     (e.g., filename)
     * @param lineNo   (context info) the line number in source for expr
     * @param columnNo (context info) the column number in source for expr
     * @param expr     the expression to compile
     * @param cb       code buffer to compile into
     *
     * @exception BSFException if any error while compiling the expression
     */
    public void compileExpr(String lang,
                            String source,
                            int lineNo,
                            int columnNo,
                            Object expr,
                            CodeBuffer cb)
        throws BSFException {
        final BSFEngine e = loadScriptingEngine(lang);
        final String sourcef = source;
        final int lineNof = lineNo, columnNof = columnNo;
        final Object exprf = expr;
        final CodeBuffer cbf = cb;

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        e.compileExpr(sourcef, lineNof, columnNof, exprf, cbf);
                        return null;
                    }
                });
        }
        catch (PrivilegedActionException prive) {
            throw (BSFException) prive.getException();
        }
    }

    /**
     * Compile the given script of the given language into the given 
     * <tt>CodeBuffer</tt>.
     *
     * @param lang     language identifier
     * @param source   (context info) the source of this script
     (e.g., filename)
     * @param lineNo   (context info) the line number in source for script
     * @param columnNo (context info) the column number in source for script
     * @param script   the script to compile
     * @param cb       code buffer to compile into
     *
     * @exception BSFException if any error while compiling the script
     */
    public void compileScript(String lang,
                              String source,
                              int lineNo,
                              int columnNo,
                              Object script,
                              CodeBuffer cb)
        throws BSFException {
        final BSFEngine e = loadScriptingEngine(lang);
        final String sourcef = source;
        final int lineNof = lineNo, columnNof = columnNo;
        final Object scriptf = script;
        final CodeBuffer cbf = cb;

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        e.compileScript(sourcef, lineNof, columnNof, 
                                        scriptf, cbf);
                        return null;
                    }
                });
        }
        catch (PrivilegedActionException prive) {
            throw (BSFException) prive.getException();
        }
    }

    /**
     * Declare a bean. The difference between declaring and registering 
     * is that engines are spsed to make declared beans "pre-available"
     * in the scripts as far as possible. That is, if a script author
     * needs a registered bean, he needs to look it up in some way. However
     * if he needs a declared bean, the language has the responsibility to
     * make those beans avaialable "automatically."
     * <p>
     * When a bean is declared it is automatically registered as well
     * so that any declared bean can be gotton to by looking it up as well.
     * <p>
     * If any of the languages that are already running in this manager
     * says they don't like this (by throwing an exception) then this
     * method will simply quit with that exception. That is, any engines
     * that come after than in the engine enumeration will not even be
     * told about this new bean. 
     * <p>
     * So, in general its best to declare beans before the manager has
     * been asked to load any engines because then the user can be informed
     * when an engine rejects it. Also, its much more likely that an engine
     * can declare a bean at start time than it can at any time.
     *
     * @param beanName name to declare bean as
     * @param bean     the bean that's being declared
     * @param type     the type to represent the bean as
     *
     * @exception BSFException if any of the languages that are already
     *            running decides to throw an exception when asked to
     *            declare this bean.
     */
    public void declareBean(String beanName, Object bean, Class type)
        throws BSFException {
        registerBean(beanName, bean);

        BSFDeclaredBean tempBean = new BSFDeclaredBean(beanName, bean, type);
        declaredBeans.addElement(tempBean);

        Enumeration enginesEnum = loadedEngines.elements();
        BSFEngine engine;
        while (enginesEnum.hasMoreElements()) {
            engine = (BSFEngine) enginesEnum.nextElement();
            engine.declareBean(tempBean);
        }
    }

    /**
     * Evaluate the given expression of the given language and return the
     * resulting value.
     *
     * @param lang language identifier
     * @param source (context info) the source of this expression
     (e.g., filename)
     * @param lineNo (context info) the line number in source for expr
     * @param columnNo (context info) the column number in source for expr
     * @param expr the expression to evaluate
     *
     * @exception BSFException if anything goes wrong while running the script
     */
    public Object eval(String lang,
                       String source,
                       int lineNo,
                       int columnNo,
                       Object expr)
        throws BSFException {
        final BSFEngine e = loadScriptingEngine(lang);
        final String sourcef = source;
        final int lineNof = lineNo, columnNof = columnNo;
        final Object exprf = expr;
        Object result = null;
        
        try {
            final Object resultf =
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws Exception {
                            return e.eval(sourcef, lineNof, columnNof, exprf);
                        }
                    });
            result = resultf;
        }
        catch (PrivilegedActionException prive) {
            throw (BSFException) prive.getException();
        }

        return result;
    }

    //////////////////////////////////////////////////////////////////////
    //
    // Convenience functions for exec'ing and eval'ing scripts directly
    // without loading and dealing with engines etc..
    //
    //////////////////////////////////////////////////////////////////////

    /**
     * Execute the given script of the given language.
     *
     * @param lang     language identifier
     * @param source   (context info) the source of this expression
     (e.g., filename)
     * @param lineNo   (context info) the line number in source for expr
     * @param columnNo (context info) the column number in source for expr
     * @param script   the script to execute
     *
     * @exception BSFException if anything goes wrong while running the script
     */
    public void exec(String lang,
                     String source,
                     int lineNo,
                     int columnNo,
                     Object script)
        throws BSFException {
        final BSFEngine e = loadScriptingEngine(lang);
        final String sourcef = source;
        final int lineNof = lineNo, columnNof = columnNo;
        final Object scriptf = script;

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        e.exec(sourcef, lineNof, columnNof, scriptf);
                        return null;
                    }
                });
        }
        catch (PrivilegedActionException prive) {
            throw (BSFException) prive.getException();
        }
    }

    /**
     * Execute the given script of the given language, attempting to
     * emulate an interactive session w/ the language.
     *
     * @param lang     language identifier
     * @param source   (context info) the source of this expression 
     *                 (e.g., filename)
     * @param lineNo   (context info) the line number in source for expr
     * @param columnNo (context info) the column number in source for expr
     * @param script   the script to execute
     *
     * @exception BSFException if anything goes wrong while running the script
     */
    public void iexec(String lang,
                     String source,
                     int lineNo,
                     int columnNo,
                     Object script)
        throws BSFException {
        final BSFEngine e = loadScriptingEngine(lang);
        final String sourcef = source;
        final int lineNof = lineNo, columnNof = columnNo;
        final Object scriptf = script;

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        e.iexec(sourcef, lineNof, columnNof, scriptf);
                        return null;
                    }
                });
        }
        catch (PrivilegedActionException prive) {
            throw (BSFException) prive.getException();
        }
    }

    /**
     * Get classLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Get classPath
     */
    public String getClassPath() {
        if (classPath == null) {
            try {
                classPath = System.getProperty("java.class.path");
            } 
            catch (Throwable t) {
                // prolly a security exception .. so no can do
            }
        }
        return classPath;
    }

    /**
     * Determine the language of a script file by looking at the file
     * extension. 
     *
     * @param fileName the name of the file
     *
     * @return the scripting language the file is in if the file extension
     *         is known to me (must have been registered via 
     *         registerScriptingEngine).
     *
     * @exception BSFException if file's extension is unknown.
     */
    public static String getLangFromFilename(String fileName) 
        throws BSFException {
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex != -1) {
            String extn = fileName.substring(dotIndex + 1);
            String langval = (String) extn2Lang.get(extn), lang = null;
            int index = 0, loops = 0;

            if (langval != null) {
                while ((index = langval.indexOf(":", 0)) != -1) {
                    // Great. Multiple language engines registered
                    // for this extension.
                    // Try to find first one that is in our classpath.
                    lang = langval.substring(0, index);
                    langval = langval.substring(index + 1);
                    loops++;

                    // Test to see if in classpath
                    try {
                        String engineName = 
                            (String) registeredEngines.get(lang);
                        Class.forName(engineName);
                    }
                    catch (ClassNotFoundException cnfe) {
                        // Bummer.
                        lang = langval;
                        continue;
                    }

                    // Got past that? Good.
                    break;
                }
                if (loops == 0) lang = langval;
            }
            
            if (lang != null && lang != "") {
                return lang;
            }
        }
        throw new BSFException(BSFException.REASON_OTHER_ERROR,
                               "file extension missing or unknown: "
                               + "unable to determine language for '"
                               + fileName
                               + "'");
    }

    /**
     * Return the current object registry of the manager.
     *
     * @return the current registry.
     */
    public ObjectRegistry getObjectRegistry() {
        return objectRegistry;
    }

    /**
     * Get tempDir
     */
    public String getTempDir() {
        return tempDir;
    }

    /**
     * Determine whether a language is registered.
     *
     * @param lang string identifying a language
     *
     * @return true iff it is
     */
    public static boolean isLanguageRegistered(String lang) {
        return (registeredEngines.get(lang) != null);
    }

    //////////////////////////////////////////////////////////////////////
    //
    // Bean scripting framework services
    //
    //////////////////////////////////////////////////////////////////////

    /**
     * Load a scripting engine based on the lang string identifying it.
     *
     * @param lang string identifying language
     * @exception BSFException if the language is unknown (i.e., if it
     *            has not been registered) with a reason of 
     *            REASON_UNKNOWN_LANGUAGE. If the language is known but
     *            if the interface can't be created for some reason, then
     *            the reason is set to REASON_OTHER_ERROR and the actual
     *            exception is passed on as well.
     */
    public BSFEngine loadScriptingEngine(String lang) throws BSFException {
        // if its already loaded return that
        BSFEngine eng = (BSFEngine) loadedEngines.get(lang);
        if (eng != null) {
            return eng;
        }

        // is it a registered language?
        String engineClassName = (String) registeredEngines.get(lang);
        if (engineClassName == null) {
            throw new BSFException(BSFException.REASON_UNKNOWN_LANGUAGE,
                                   "unsupported language: " + lang);
        }

        // create the engine and initialize it. if anything goes wrong
        // except.
        try {
            Class engineClass =
                (classLoader == null)
                ? Class.forName(engineClassName)
                : classLoader.loadClass(engineClassName);
            final BSFEngine engf = (BSFEngine) engineClass.newInstance();
            final BSFManager thisf = this;
            final String langf = lang;
            final Vector dbf = declaredBeans;
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        engf.initialize(thisf, langf, dbf);
                        return null;
                    }
                });
            eng = engf;
            loadedEngines.put(lang, eng);
            pcs.addPropertyChangeListener(eng);
            return eng;
        } 
        catch (PrivilegedActionException prive) {
                throw (BSFException) prive.getException();
        }
        catch (Throwable t) {
            throw new BSFException(BSFException.REASON_OTHER_ERROR,
                                   "unable to load language: " + lang,
                                   t);
        }
    }

    /**
     * return a handle to a bean registered in the bean registry by the
     * application or a scripting engine. Returns null if bean is not found. 
     *
     * @param beanName name of bean to look up
     *
     * @return the bean if its found or null
     */
    public Object lookupBean(String beanName) {
        try {
            return ((BSFDeclaredBean)objectRegistry.lookup(beanName)).bean;
        } 
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 
     * Registering a bean allows a scripting engine or the application to 
     * access that bean by name and to manipulate it.
     *
     * @param beanName name to register under
     * @param bean     the bean to register
     */
    public void registerBean(String beanName, Object bean) {
        BSFDeclaredBean tempBean;

        if(bean == null) {
            tempBean = new BSFDeclaredBean(beanName, null, null);
        }
        else {
            tempBean = new BSFDeclaredBean(beanName, bean, bean.getClass());
        }
        objectRegistry.register(beanName, tempBean);
    }

    /**
     * Register a scripting engine in the static registry of the 
     * BSFManager.
     *
     * @param lang string identifying language
     * @param engineClassName fully qualified name of the class interfacing
     *        the language to BSF.
     * @param extensions array of file extensions that should be mapped to
     *        this language type. may be null.
     */
    public static void registerScriptingEngine(String lang,
                                               String engineClassName,
                                               String[] extensions) {
        registeredEngines.put(lang, engineClassName);
        if (extensions != null) {
            for (int i = 0; i < extensions.length; i++) {
                String langstr = (String) extn2Lang.get(extensions[i]);
                langstr = (langstr == null) ? lang : lang + ":" + langstr;
                extn2Lang.put(extensions[i], langstr);
            }
        }
    }

    /**
     * Set the class loader for those that need to use it. Default is he
     * who loaded me or null (i.e., its Class.forName).
     *
     * @param classLoader the class loader to use.
     */
    public void setClassLoader(ClassLoader classLoader) {
        pcs.firePropertyChange("classLoader", this.classLoader, classLoader);
        this.classLoader = classLoader;
    }

    /**
     * Set the classpath for those that need to use it. Default is the value
     * of the java.class.path property.
     *
     * @param classPath the classpath to use
     */
    public void setClassPath(String classPath) {
        pcs.firePropertyChange("classPath", this.classPath, classPath);
        this.classPath = classPath;
    }

    /**
     * Set the object registry used by this manager. By default a new
     * one is created when the manager is new'ed and this overwrites 
     * that one.
     *
     * @param objectRegistry the registry to use
     */
    public void setObjectRegistry(ObjectRegistry objectRegistry) {
        this.objectRegistry = objectRegistry;
    }

    /**
     * Temporary directory to put stuff into (for those who need to). Note
     * that unless this directory is in the classpath or unless the 
     * classloader knows to look in here, any classes here will not 
     * be found! BSFManager provides a service method to load a class 
     * which uses either the classLoader provided by the class loader 
     * property or, if that fails, a class loader which knows to load from 
     * the tempdir to try to load the class. Default value of tempDir 
     * is "." (current working dir). 
     *
     * @param tempDir the temporary directory
     */
    public void setTempDir(String tempDir) {
        pcs.firePropertyChange("tempDir", this.tempDir, tempDir);
        this.tempDir = tempDir;
    }

    /**
     * Gracefully terminate all engines
     */
    public void terminate() {
        Enumeration enginesEnum = loadedEngines.elements();
        BSFEngine engine;
        while (enginesEnum.hasMoreElements()) {
            engine = (BSFEngine) enginesEnum.nextElement();
            engine.terminate();
        }

        loadedEngines = new Hashtable();
    }

    /**
     * Undeclare a previously declared bean. This removes the bean from
     * the list of declared beans in the manager as well as asks every
     * running engine to undeclared the bean. As with above, if any
     * of the engines except when asked to undeclare, this method does
     * not catch that exception. Quietly returns if the bean is unknown.
     *
     * @param beanName name of bean to undeclare
     *
     * @exception BSFException if any of the languages that are already
     *            running decides to throw an exception when asked to
     *            undeclare this bean.
     */
    public void undeclareBean(String beanName) throws BSFException {
        unregisterBean(beanName);

        BSFDeclaredBean tempBean = null;
        for (int i = 0; i < declaredBeans.size(); i++) {
            tempBean = (BSFDeclaredBean) declaredBeans.elementAt(i);
            if (tempBean.name.equals(beanName)) {
                break;
            }
        }

        if (tempBean != null) {
            declaredBeans.removeElement(tempBean);

            Enumeration enginesEnum = loadedEngines.elements();
            while (enginesEnum.hasMoreElements()) {
                BSFEngine engine = (BSFEngine) enginesEnum.nextElement();
                engine.undeclareBean(tempBean);
            }
        }
    }

    /** 
     * Unregister a previously registered bean. Silent if name is not found.
     *
     * @param beanName name of bean to unregister
     */
    public void unregisterBean(String beanName) {
        objectRegistry.unregister(beanName);
    }
}
