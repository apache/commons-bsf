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

package org.apache.bsf.util;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.apache.bsf.util.cf.CodeFormatter;

/**
 * A <code>CodeBuffer</code> object is used as a code repository for generated Java code.
 * It provides buffers which correspond to the various sections of a Java class.
 */
public class CodeBuffer
{
  private final StringWriter fieldDeclSW       = new StringWriter(),
                       methodDeclSW      = new StringWriter(),
                       initializerSW     = new StringWriter(),
                       constructorSW     = new StringWriter(),
                       serviceMethodSW   = new StringWriter();

  private final PrintWriter  fieldDeclPW       = new PrintWriter(fieldDeclSW),
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
  private final Vector imports                 = new Vector(),
                 constructorArguments    = new Vector(),
                 constructorExceptions   = new Vector(),
                 serviceMethodExceptions = new Vector(),
                 implementsVector        = new Vector();
  private String packageName,
                 className               = "Test",
                 serviceMethodName       = "exec",
                 extendsName;
  private Class  serviceMethodReturnType = void.class;

  public CodeBuffer()
  {
  }
  public CodeBuffer(final CodeBuffer parent)
  {
    this.parent = parent;
  }
  public void addConstructorArgument(final ObjInfo arg)
  {
    constructorArguments.addElement(arg);
  }
  public void addConstructorException(final String exceptionName)
  {
    if (!constructorExceptions.contains(exceptionName))
    {
      constructorExceptions.addElement(exceptionName);
    }
  }
  public void addConstructorStatement(final String statement)
  {
    constructorPW.println(statement);
  }
  public void addFieldDeclaration(final String statement)
  {
    fieldDeclPW.println(statement);
  }
  public void addImplements(final String importName)
  {
    if (!implementsVector.contains(importName))
    {
      implementsVector.addElement(importName);
    }
  }
  public void addImport(final String importName)
  {
    if (!imports.contains(importName))
    {
      imports.addElement(importName);
    }
  }
  public void addInitializerStatement(final String statement)
  {
    initializerPW.println(statement);
  }
  public void addMethodDeclaration(final String statement)
  {
    methodDeclPW.println(statement);
  }
  public void addServiceMethodException(final String exceptionName)
  {
    if (!serviceMethodExceptions.contains(exceptionName))
    {
      serviceMethodExceptions.addElement(exceptionName);
    }
  }
  public void addServiceMethodStatement(final String statement)
  {
    serviceMethodPW.println(statement);
  }
  // Used internally by merge(...).
  private void appendIfNecessary(final PrintWriter pw, final StringBuffer buf)
  {
    if (buf.length() > 0)
    {
      pw.print(buf.toString());
    }
  }
  public String buildNewSymbol(final String prefix)
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
  public ObjInfo getSymbol(final String symbol)
  {
    ObjInfo ret = (ObjInfo)symbolTable.get(symbol);

    if (ret == null && parent != null) {
        ret = parent.getSymbol(symbol);
    }

    return ret;
  }
  Integer getSymbolIndex(final String prefix)
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
  public void merge(final CodeBuffer otherCB)
  {
    final Vector otherImports = otherCB.getImports();

    for (int i = 0; i < otherImports.size(); i++)
    {
      addImport((String)otherImports.elementAt(i));
    }

    appendIfNecessary(fieldDeclPW,     otherCB.getFieldBuffer());
    appendIfNecessary(methodDeclPW,    otherCB.getMethodBuffer());
    appendIfNecessary(initializerPW,   otherCB.getInitializerBuffer());
    appendIfNecessary(constructorPW,   otherCB.getConstructorBuffer());
    appendIfNecessary(serviceMethodPW, otherCB.getServiceMethodBuffer());

    final ObjInfo oldRet = getFinalServiceMethodStatement();

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
  public void print(final PrintWriter out, final boolean formatOutput)
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
  public void putSymbol(final String symbol, final ObjInfo obj)
  {
    symbolTable.put(symbol, obj);
  }
  void putSymbolIndex(final String prefix, final Integer index)
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
  public void setClassName(final String className)
  {
    this.className = className;
  }
  public void setExtends(final String extendsName)
  {
    this.extendsName = extendsName;
  }
  public void setFinalServiceMethodStatement(final ObjInfo finalStatementInfo)
  {
    this.finalStatementInfo = finalStatementInfo;
  }
  public void setPackageName(final String packageName)
  {
    this.packageName = packageName;
  }
  public void setServiceMethodName(final String serviceMethodName)
  {
    this.serviceMethodName = serviceMethodName;
  }
  public void setServiceMethodReturnType(final Class serviceMethodReturnType)
  {
    this.serviceMethodReturnType = serviceMethodReturnType;
  }
  public void setSymbolTable(final Hashtable symbolTable)
  {
    this.symbolTable = symbolTable;
  }
  public boolean symbolTableIsStacked()
  {
    return (symbolTable instanceof ScriptSymbolTable);
  }
  public String toString()
  {
    final StringWriter sw  = new StringWriter();
    final PrintWriter  pw  = new PrintWriter(sw);
    final ObjInfo      ret = finalStatementInfo;

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
