/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

package org.apache.bsf.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.apache.bsf.util.cf.CodeFormatter;

/**
 * A <code>CodeBuffer</code> object is used as a code repository for generated Java code.
 * It provides buffers which correspond to the various sections of a Java class.
 * 
 * @author   Matthew J. Duftler
 */
public class CodeBuffer
{
  private StringWriter fieldDeclSW       = new StringWriter(),
					   methodDeclSW      = new StringWriter(),
					   initializerSW     = new StringWriter(),
					   constructorSW     = new StringWriter(),
					   serviceMethodSW   = new StringWriter();

  private PrintWriter  fieldDeclPW       = new PrintWriter(fieldDeclSW),
					   methodDeclPW      = new PrintWriter(methodDeclSW),
					   initializerPW     = new PrintWriter(initializerSW),
					   constructorPW     = new PrintWriter(constructorSW),
					   serviceMethodPW   = new PrintWriter(serviceMethodSW);

  private Stack        symbolTableStack  = new Stack();
  private Hashtable    symbolTable       = new Hashtable(),
					   usedSymbolIndices = new Hashtable();

  private ObjInfo      finalStatementInfo;
  private CodeBuffer   parent;


  {
	symbolTableStack.push(symbolTable);
  }

  // New stuff...
  private Vector imports                 = new Vector(),
				 constructorArguments    = new Vector(),
				 constructorExceptions   = new Vector(),
				 serviceMethodExceptions = new Vector(),
				 implementsVector        = new Vector();
  private String packageName             = null,
				 className               = "Test",
				 serviceMethodName       = "exec",
				 extendsName             = null;
  private Class  serviceMethodReturnType = void.class;

  public CodeBuffer()
  {
  }
  public CodeBuffer(CodeBuffer parent)
  {
	this.parent = parent;
  }
  public void addConstructorArgument(ObjInfo arg)
  {
	constructorArguments.addElement(arg);
  }
  public void addConstructorException(String exceptionName)
  {
	if (!constructorExceptions.contains(exceptionName))
	{
	  constructorExceptions.addElement(exceptionName);
	}
  }
  public void addConstructorStatement(String statement)
  {
	constructorPW.println(statement);
  }
  public void addFieldDeclaration(String statement)
  {
	fieldDeclPW.println(statement);
  }
  public void addImplements(String importName)
  {
	if (!implementsVector.contains(importName))
	{
	  implementsVector.addElement(importName);
	}
  }
  public void addImport(String importName)
  {
	if (!imports.contains(importName))
	{
	  imports.addElement(importName);
	}
  }
  public void addInitializerStatement(String statement)
  {
	initializerPW.println(statement);
  }
  public void addMethodDeclaration(String statement)
  {
	methodDeclPW.println(statement);
  }
  public void addServiceMethodException(String exceptionName)
  {
	if (!serviceMethodExceptions.contains(exceptionName))
	{
	  serviceMethodExceptions.addElement(exceptionName);
	}
  }
  public void addServiceMethodStatement(String statement)
  {
	serviceMethodPW.println(statement);
  }
  // Used internally by merge(...).
  private void appendIfNecessary(PrintWriter pw, StringBuffer buf)
  {
	if (buf.length() > 0)
	{
	  pw.print(buf.toString());
	}
  }
  public String buildNewSymbol(String prefix)
  {
	Integer nextNum = getSymbolIndex(prefix);

	if (nextNum == null)
	{
	  nextNum = new Integer(0);
	}

	int    iNextNum = nextNum.intValue();
	String symbol   = prefix + "_" + iNextNum;

	while (getSymbol(symbol) != null)
	{
	  iNextNum++;
	  symbol = prefix + "_" + iNextNum;
	}

	putSymbolIndex(prefix, new Integer(iNextNum + 1));

	return symbol;
  }
  public void clearSymbolTable()
  {
	symbolTable       = new Hashtable();
	symbolTableStack  = new Stack();
	symbolTableStack.push(symbolTable);

	usedSymbolIndices = new Hashtable();
  }
  public String getClassName()
  {
	return className;
  }
  public Vector getConstructorArguments()
  {
	return constructorArguments;
  }
  public StringBuffer getConstructorBuffer()
  {
	constructorPW.flush();

	return constructorSW.getBuffer();
  }
  public Vector getConstructorExceptions()
  {
	return constructorExceptions;
  }
  public String getExtends()
  {
	return extendsName;
  }
  public StringBuffer getFieldBuffer()
  {
	fieldDeclPW.flush();

	return fieldDeclSW.getBuffer();
  }
  public ObjInfo getFinalServiceMethodStatement()
  {
	return finalStatementInfo;
  }
  public Vector getImplements()
  {
	return implementsVector;
  }
  public Vector getImports()
  {
	return imports;
  }
  public StringBuffer getInitializerBuffer()
  {
	initializerPW.flush();

	return initializerSW.getBuffer();
  }
  public StringBuffer getMethodBuffer()
  {
	methodDeclPW.flush();

	return methodDeclSW.getBuffer();
  }
  public String getPackageName()
  {
	return packageName;
  }
  public StringBuffer getServiceMethodBuffer()
  {
	serviceMethodPW.flush();

	return serviceMethodSW.getBuffer();
  }
  public Vector getServiceMethodExceptions()
  {
	return serviceMethodExceptions;
  }
  public String getServiceMethodName()
  {
	return serviceMethodName;
  }
  public Class getServiceMethodReturnType()
  {
	if (finalStatementInfo != null)
	{
	  return finalStatementInfo.objClass;
	}
	else if (serviceMethodReturnType != null)
	{
	  return serviceMethodReturnType;
	}
	else
	{
	  return void.class;
	}
  }
  public ObjInfo getSymbol(String symbol)
  {
	ObjInfo ret = (ObjInfo)symbolTable.get(symbol);

	if (ret == null && parent != null)
	  ret = parent.getSymbol(symbol);

	return ret;
  }
  Integer getSymbolIndex(String prefix)
  {
	if (parent != null)
	{
	  return parent.getSymbolIndex(prefix);
	}
	else
	{
	  return (Integer)usedSymbolIndices.get(prefix);
	}
  }
  public Hashtable getSymbolTable()
  {
	return symbolTable;
  }
  public void merge(CodeBuffer otherCB)
  {
	Vector otherImports = otherCB.getImports();

	for (int i = 0; i < otherImports.size(); i++)
	{
	  addImport((String)otherImports.elementAt(i));
	}

	appendIfNecessary(fieldDeclPW,     otherCB.getFieldBuffer());
	appendIfNecessary(methodDeclPW,    otherCB.getMethodBuffer());
	appendIfNecessary(initializerPW,   otherCB.getInitializerBuffer());
	appendIfNecessary(constructorPW,   otherCB.getConstructorBuffer());
	appendIfNecessary(serviceMethodPW, otherCB.getServiceMethodBuffer());

	ObjInfo oldRet = getFinalServiceMethodStatement();

	if (oldRet != null && oldRet.isExecutable())
	{
	  addServiceMethodStatement(oldRet.objName + ";");
	}

	setFinalServiceMethodStatement(otherCB.getFinalServiceMethodStatement());
  }
  public void popSymbolTable()
  {
	symbolTableStack.pop();
	symbolTable = (Hashtable)symbolTableStack.peek();
  }
  public void print(PrintWriter out, boolean formatOutput)
  {
	if (formatOutput)
	{
	  new CodeFormatter().formatCode(new StringReader(toString()), out);
	}
	else
	{
	  out.print(toString());
	}

	out.flush();
  }
  public void pushSymbolTable()
  {
	symbolTable = (Hashtable)symbolTableStack.push(new ScriptSymbolTable(symbolTable));
  }
  public void putSymbol(String symbol, ObjInfo obj)
  {
	symbolTable.put(symbol, obj);
  }
  void putSymbolIndex(String prefix, Integer index)
  {
	if (parent != null)
	{
	  parent.putSymbolIndex(prefix, index);
	}
	else
	{
	  usedSymbolIndices.put(prefix, index);
	}
  }
  public void setClassName(String className)
  {
	this.className = className;
  }
  public void setExtends(String extendsName)
  {
	this.extendsName = extendsName;
  }
  public void setFinalServiceMethodStatement(ObjInfo finalStatementInfo)
  {
	this.finalStatementInfo = finalStatementInfo;
  }
  public void setPackageName(String packageName)
  {
	this.packageName = packageName;
  }
  public void setServiceMethodName(String serviceMethodName)
  {
	this.serviceMethodName = serviceMethodName;
  }
  public void setServiceMethodReturnType(Class serviceMethodReturnType)
  {
	this.serviceMethodReturnType = serviceMethodReturnType;
  }
  public void setSymbolTable(Hashtable symbolTable)
  {
	this.symbolTable = symbolTable;
  }
  public boolean symbolTableIsStacked()
  {
	return (symbolTable instanceof ScriptSymbolTable);
  }
  public String toString()
  {
	StringWriter sw  = new StringWriter();
	PrintWriter  pw  = new PrintWriter(sw);
	ObjInfo      ret = finalStatementInfo;

	if (packageName != null && !packageName.equals(""))
	{
	  pw.println("package " + packageName + ";");
	  pw.println();
	}

	if (imports.size() > 0)
	{
	  for (int i = 0; i < imports.size(); i++)
	  {
		pw.println("import " + imports.elementAt(i) + ";");
	  }

	  pw.println();
	}

	pw.println("public class " + className +
			   (extendsName != null && !extendsName.equals("")
				? " extends " + extendsName
				: "") +
			   (implementsVector.size() > 0
				? " implements " +
				  StringUtils.getCommaListFromVector(implementsVector)
				: "")
			  );
	pw.println("{");

	pw.print(getFieldBuffer().toString());

	StringBuffer buf = getInitializerBuffer();

	if (buf.length() > 0)
	{
	  pw.println();
	  pw.println("{");
	  pw.print(buf.toString());
	  pw.println("}");
	}

	buf = getConstructorBuffer();

	if (buf.length() > 0)
	{
	  pw.println();
	  pw.println("public " + className + "(" +
				 (constructorArguments.size() > 0
				  ? StringUtils.getCommaListFromVector(constructorArguments)
				  : ""
				 ) + ")" +
				 (constructorExceptions.size() > 0
				  ? " throws " +
					StringUtils.getCommaListFromVector(constructorExceptions)
				  : ""
				 )
				);
	  pw.println("{");
	  pw.print(buf.toString());
	  pw.println("}");
	}

	buf = getServiceMethodBuffer();

	if (buf.length() > 0 || ret != null)
	{
	  pw.println();
	  pw.println("public " +
				  StringUtils.getClassName(getServiceMethodReturnType()) + " " +
				  serviceMethodName + "()" +
				 (serviceMethodExceptions.size() > 0
				  ? " throws " +
					StringUtils.getCommaListFromVector(serviceMethodExceptions)
				  : ""
				 )
				);
	  pw.println("{");

	  pw.print(buf.toString());

	  if (ret != null)
	  {
		if (ret.isValueReturning())
		{
		  pw.println();
		  pw.println("return " + ret.objName + ";");
		}
		else if (ret.isExecutable())
		{
		  pw.println(ret.objName + ";");
		}
	  }

	  pw.println("}");
	}

	pw.print(getMethodBuffer().toString());

	pw.println("}");

	pw.flush();

	return sw.toString();
  }
}
