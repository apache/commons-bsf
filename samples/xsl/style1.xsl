<?xml version='1.0'?>
<!--
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * https://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
-->

<!-- This stylesheet fills in the data by sorting on the first name. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xslt/java"
                version="1.0">

<!-- get access to the panel -->
<xsl:param name="panel"/>

<xsl:template match="data">
  <xsl:apply-templates select="person">
    <xsl:sort select="@first"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="person">
  <xsl:variable name="junk1" select="java:add ($panel, java:java.awt.Label.new (string(@first)))"/>
  <xsl:variable name="junk2" select="java:add ($panel, java:java.awt.Label.new (string(@last)))"/>
</xsl:template>

</xsl:stylesheet>