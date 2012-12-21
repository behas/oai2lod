<?xml version="1.0" encoding="UTF-8"?>

<!--
           This stylesheet converts metadata described in oai_dc
           into simple dc in rdf/xml (http://dublincore.org/documents/dcmes-xml/);
           
           NOTE: the patterns "http://baseURI/" and http://sourceURI/ are replaced during runtime
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dc="http://purl.org/dc/elements/1.1/" 
    xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:oai="http://www.openarchives.org/OAI/2.0/"
    xmlns:oai_voc="http://www.mediaspaces.info/vocab/oai-pmh.rdf#"
    xmlns:onbba="http://oai-onb-dev-ext.gideon.at/OAI/onbba/"
    xmlns:tel_onbba="http://oai-onb-dev-ext.gideon.at/OAI/tel_onbba/">
    
    <xsl:output method="xml" indent="yes"/>
    
    <xsl:template match="/">
        <rdf:RDF 
        	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        	xmlns:dc="http://purl.org/dc/elements/1.1/"
        	xml:base="http://baseURI/">
        	
        <xsl:apply-templates select="//oai:record"/>
        </rdf:RDF>
    </xsl:template>
    
    <xsl:template match="//oai:record">
        <oai_voc:Item>
            <xsl:attribute name="rdf:about">
                <xsl:value-of select="concat('/resource/item/',oai:header/oai:identifier)"/>
            </xsl:attribute>

            <xsl:for-each select="oai:header/oai:setSpec">
                <oai_voc:set>
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="concat('/resource/set/', .)"/>
                    </xsl:attribute>
                </oai_voc:set>
            </xsl:for-each>

            <oai_voc:origin>
	            <xsl:attribute name="rdf:resource">
    	            <xsl:value-of select="concat('http://sourceURI/','?','verb=GetRecord','&amp;','metadataPrefix=oai_dc','&amp;','identifier=',oai:header/oai:identifier)"/>
                </xsl:attribute>
            </oai_voc:origin>

            <xsl:apply-templates select="oai:metadata"/>
            
        </oai_voc:Item>
        
    </xsl:template>
    
    <xsl:template match="oai:metadata">

        <xsl:for-each select="tel_onbba:tel_onbba/onbba:personDepicted">
            <onbba:personDepicted><xsl:value-of select="."/></onbba:personDepicted>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:title">
            <dc:title><xsl:value-of select="."/></dc:title>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:creator">
            <dc:creator><xsl:value-of select="."/></dc:creator>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:subject">
            <dc:subject><xsl:value-of select="."/></dc:subject>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:description">
            <dc:description><xsl:value-of select="."/></dc:description>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:publisher">
            <dc:publisher><xsl:value-of select="."/></dc:publisher>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:contributor">
            <dc:contributor><xsl:value-of select="."/></dc:contributor>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:date">
            <dc:date><xsl:value-of select="."/></dc:date>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:type">
            <dc:type><xsl:value-of select="."/></dc:type>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:format">
            <dc:format><xsl:value-of select="."/></dc:format>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:identifier">
            <xsl:choose>
                <xsl:when test="starts-with(text(),'http://')">
                    <dc:identifier>
                        <xsl:attribute name="rdf:resource">
                            <xsl:value-of select="text()"/>
                        </xsl:attribute>
                    </dc:identifier>
                </xsl:when>
                <xsl:otherwise>
                    <dc:identifier><xsl:value-of select="text()"/></dc:identifier>
                </xsl:otherwise>
            </xsl:choose>   
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:source">
            <dc:source><xsl:value-of select="."/></dc:source>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:language">
            <dc:language><xsl:value-of select="."/></dc:language>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:relation">
            <dc:relation><xsl:value-of select="."/></dc:relation>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:coverage">
            <dc:coverage><xsl:value-of select="."/></dc:coverage>
        </xsl:for-each>
        <xsl:for-each select="tel_onbba:tel_onbba/dc:rights">
            <dc:rights><xsl:value-of select="."/></dc:rights>
        </xsl:for-each>                                                                                                                                                                
        
    </xsl:template>

</xsl:stylesheet>