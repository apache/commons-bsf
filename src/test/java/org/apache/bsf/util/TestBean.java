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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.bsf.util;

import java.io.Serializable;

/**
 * This is the org.apache.bsf.test.utilTests.TestBean.java used in
 * EngineUtils.java
 */

public class TestBean implements Serializable{
    private String strValue = null;
    private Number numValue;

    public TestBean(){
    }

    public TestBean(final String value){
        this.strValue = value;
    }

    public void setValue(final String value){
        this.strValue = value;
    }

    public void setValue(final String sValue, final Number nValue){
        this.strValue = sValue;
        this.numValue = nValue;
    }

    public String getStringValue(){
        return strValue;
    }

    public Number getNumericValue(){
        return numValue;
    }
}
