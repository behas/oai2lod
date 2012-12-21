<?xml version="1.0" encoding="UTF-8"?>

<!--
    This stylesheet converts metadata described in oai_dc
    into simple dc in rdf/xml (http://dublincore.org/documents/dcmes-xml/);
    
    NOTE: the patterns "http://baseURI/" and http://sourceURI/ are replaced during runtime
-->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dc="http://purl.org/dc/elements/1.1/" 
    xmlns:onb="http://www.mediaspaces.info/schemes/onb#" 
    xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:oai="http://www.openarchives.org/OAI/2.0/"
    xmlns:oai_voc="http://www.mediaspaces.info/vocab/oai-pmh.rdf#">
    
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
            <oai_voc:set>
                <xsl:attribute name="rdf:resource">
                    <xsl:value-of select="concat('/resource/set/',oai:header/oai:setSpec)"/>
                </xsl:attribute>
            </oai_voc:set>
            <oai_voc:origin>
                <xsl:attribute name="rdf:resource">
                    <xsl:value-of select="concat('http://sourceURI/','?','verb=GetRecord','&amp;','metadataPrefix=oai_dc','&amp;','identifier=',oai:header/oai:identifier)"/>
                </xsl:attribute>
            </oai_voc:origin>
            
            <xsl:apply-templates select="oai:metadata"/>
            
        </oai_voc:Item>
        
    </xsl:template>
    
    <xsl:template match="oai:metadata">
        <xsl:for-each select="oai_dc:dc/dc:title">
            <onb:title><xsl:value-of select="."/></onb:title>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:creator">
            <xsl:choose>
                <xsl:when test="contains(text(), ',')">
                    <onb:authorfirstname><xsl:value-of select="normalize-space(substring-after(., ','))"/></onb:authorfirstname>
                    <onb:authorlastname><xsl:value-of select="normalize-space(substring-before(., ','))"/></onb:authorlastname>
                </xsl:when>
                <xsl:otherwise>
                    <onb:author><xsl:value-of select="."/></onb:author>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:subject">
            <onb:topic><xsl:value-of select="."/></onb:topic>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:description">
            <onb:info><xsl:value-of select="."/></onb:info>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:publisher">
            <onb:publisher><xsl:value-of select="."/></onb:publisher>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:contributor">
            <onb:contributor><xsl:value-of select="."/></onb:contributor>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:date">
            <onb:date><xsl:value-of select="."/></onb:date>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:type">
            <onb:classification><xsl:value-of select="."/></onb:classification>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:format">
            <onb:mimeType><xsl:value-of select="."/></onb:mimeType>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:identifier">
            <xsl:choose>
                <xsl:when test="starts-with(text(),'http://')">
                    <onb:url>
                        <xsl:attribute name="rdf:resource">
                            <xsl:value-of select="text()"/>
                        </xsl:attribute>
                    </onb:url>
                </xsl:when>
                <xsl:otherwise>
                    <onb:url><xsl:value-of select="text()"/></onb:url>
                </xsl:otherwise>
            </xsl:choose>   
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:source">
            <onb:origin><xsl:value-of select="."/></onb:origin>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:language">
            <onb:language><xsl:value-of select="."/></onb:language>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:coverage">
            <onb:topic><xsl:value-of select="."/></onb:topic>
        </xsl:for-each>
        <xsl:for-each select="oai_dc:dc/dc:rights">
            <onb:rights><xsl:value-of select="."/></onb:rights>
        </xsl:for-each>                                                                                                                                                                
        
    </xsl:template>
    
</xsl:stylesheet>