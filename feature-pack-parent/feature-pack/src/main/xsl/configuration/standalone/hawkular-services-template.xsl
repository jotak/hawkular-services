<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright 2016-2017 Red Hat, Inc. and/or its affiliates
    and other contributors as indicated by the @author tags.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" version="2.0" exclude-result-prefixes="xalan">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" xalan:indent-amount="4" standalone="no" />

  <xsl:param name="hawkular.embeddedc" select="'false'"/>

  <!-- Add the embedded cassandra if it was passed in through the parameters -->
  <xsl:template match="/*[local-name()='server']/*[local-name()='management']">
    <system-properties>
      <xsl:if test="$hawkular.embeddedc = 'true'">
        <xsl:element name="property" namespace="{namespace-uri()}">
          <xsl:attribute name="name">hawkular.backend</xsl:attribute>
          <xsl:attribute name="value">embedded_cassandra</xsl:attribute>
        </xsl:element>
      </xsl:if>
    </system-properties>
    <xsl:copy>
      <xsl:apply-templates select="node()|comment()|@*"/>
    </xsl:copy>
  </xsl:template>

  <!-- copy everything else as-is -->
  <xsl:template match="node()|comment()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|comment()|@*" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
