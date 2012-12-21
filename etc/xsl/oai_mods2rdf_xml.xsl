<?xml version="1.0" encoding="UTF-8"?>

<!--
    This stylesheet converts metadata described in MODS (in OAI-PMH metadata prefix "mods")
    into rdf/xml; it uses the terms defined in http://www.mediaspaces.info/schemes/mods/v3#
    
    NOTE: the patterns "http://baseURI/" and http://sourceURI/ are replaced during runtime
-->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mods="http://www.mediaspaces.info/schemes/mods/v3#"
    xmlns:oai_mods="http://www.loc.gov/mods/v3"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:oai="http://www.openarchives.org/OAI/2.0/"
    xmlns:oai_voc="http://www.mediaspaces.info/vocab/oai-pmh.rdf#" exclude-result-prefixes="oai_mods oai">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
            xml:base="http://baseURI/">

            <xsl:apply-templates select="//oai:record"/>
        </rdf:RDF>
    </xsl:template>

    <xsl:template match="oai:record">
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
                    <xsl:value-of
                        select="concat('http://sourceURI/','?','verb=GetRecord','&amp;','metadataPrefix=oai_dc','&amp;','identifier=',oai:header/oai:identifier)"
                    />
                </xsl:attribute>
            </oai_voc:origin>

            <xsl:apply-templates select="oai:metadata/oai_mods:mods"/>

        </oai_voc:Item>

    </xsl:template>

    <xsl:template match="oai_mods:mods">
        <xsl:for-each select="oai_mods:titleInfo[empty(@type)]/oai_mods:title">
            <mods:title>
                <xsl:value-of select="."/>
            </mods:title>
        </xsl:for-each>
        <xsl:for-each select="oai_mods:titleInfo[empty(@type)]/oai_mods:subTitle">
            <mods:subTitle>
                <xsl:value-of select="."/>
            </mods:subTitle>
        </xsl:for-each>
        <xsl:for-each select="oai_mods:titleInfo[@type='alternative']/oai_mods:title">
            <mods:alternativeTitle>
                <xsl:value-of select="."/>
            </mods:alternativeTitle>
        </xsl:for-each>
        <xsl:for-each select="oai_mods:name[@type='personal']/oai_mods:namePart">
            <mods:personalName>
                <xsl:value-of select="."/>
            </mods:personalName>
        </xsl:for-each>
        <xsl:for-each select="oai_mods:name[@type='corporate']/oai_mods:namePart">
            <mods:corporateName>
                <xsl:value-of select="."/>
            </mods:corporateName>
        </xsl:for-each>
        <xsl:for-each select="oai_mods:note">
            <mods:note>
                <xsl:value-of select="."/>
            </mods:note>
        </xsl:for-each>
        <xsl:for-each select="oai_mods:abstract">
            <mods:abstract>
                <xsl:value-of select="."/>
            </mods:abstract>
        </xsl:for-each>
        <xsl:for-each select="oai_mods:location/oai_mods:url">
            <xsl:choose>
                <xsl:when test="starts-with(text(),'http://')">
                    <mods:urlLocation>
                        <xsl:attribute name="rdf:resource">
                            <xsl:value-of select="text()"/>
                        </xsl:attribute>
                    </mods:urlLocation>
                </xsl:when>
                <xsl:otherwise>
                    <mods:urlLocation>
                        <xsl:value-of select="text()"/>
                    </mods:urlLocation>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
        <xsl:for-each select="oai_mods:classification[@authority='lcc']">
            <mods:llcClassification>
                <xsl:value-of select="."/>
            </mods:llcClassification>
        </xsl:for-each>
        <xsl:for-each select="oai_mods:classification[@authority='ddc']">
            <mods:ddcClassification>
                <xsl:value-of select="."/>
            </mods:ddcClassification>
        </xsl:for-each>

        <xsl:apply-templates select="oai_mods:subject"/>

    </xsl:template>

    <!-- special templates eliminating duplicates -->
    <xsl:template match="oai_mods:subject">
        <xsl:if
            test="not(current()/oai_mods:topic = preceding-sibling::oai_mods:subject/oai_mods:topic)">

            <xsl:for-each select="oai_mods:topic">
                <mods:topic>
                    <xsl:value-of select="."/>
                </mods:topic>
            </xsl:for-each>
        </xsl:if>

        <xsl:if
            test="not(current()/oai_mods:geographic = preceding-sibling::oai_mods:subject/oai_mods:geographic)">

            <xsl:for-each select="oai_mods:geographic">
                <mods:geographic>
                    <xsl:value-of select="."/>
                </mods:geographic>
            </xsl:for-each>
        </xsl:if>


    </xsl:template>


</xsl:stylesheet>
