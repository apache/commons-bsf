<?xml version='1.0'?>

<!-- This stylesheet fills in the data without sorting the list of names. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xslt/java"
                version="1.0">

<!-- get access to the panel -->
<xsl:param name="panel"/>

<xsl:template match="data">
  <xsl:apply-templates select="person"/>
</xsl:template>
    
<xsl:template match="person">
  <xsl:variable name="junk" select="
    java:add ($panel, java:java.awt.Label.new (string(@first)))"/>
  <xsl:variable name="junk" select="
    java:add ($panel, java:java.awt.Label.new (string(@last)))"/>
</xsl:template>

</xsl:stylesheet>
