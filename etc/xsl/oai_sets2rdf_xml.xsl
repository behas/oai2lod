<?xml version="1.0" encoding="UTF-8"?>
<!--
           This stylesheet converts metadata described in oai_dc
           into simple dc in rdf/xml (http://dublincore.org/documents/dcmes-xml/);
           
           NOTE: the patterns "http://baseURI/" and http://sourceURI/ are replaced during runtime
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:oai="http://www.openarchives.org/OAI/2.0/" xmlns:oai_voc="http://www.mediaspaces.info/vocab/oai-pmh.rdf#">
	<xsl:output method="xml" indent="yes"/>
	<xsl:template match="/">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xml:base="http://baseURI/">
			<xsl:apply-templates select="//oai:set"/>
		</rdf:RDF>
	</xsl:template>
	<xsl:template match="//oai:set">
		<oai_voc:Set>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('/resource/set/',oai:setSpec)"/></xsl:attribute>
			<oai_voc:setSpec>
				<xsl:value-of select="oai:setSpec"/>
			</oai_voc:setSpec>
			<oai_voc:setName>
				<xsl:value-of select="oai:setName"/>
			</oai_voc:setName>
			<!-- works only if desciption is provided accoring to best practices guidelines -->
			<xsl:if test="oai:setDescription/oai_dc:dc/dc:description">
				<oai_voc:setDescription>
					<xsl:value-of select="oai:setDescription/oai_dc:dc/dc:description"/>
				</oai_voc:setDescription>
			</xsl:if>
            <oai_voc:origin>
	            <xsl:attribute name="rdf:resource">
    	            <xsl:value-of select="concat('http://sourceURI/','?','verb=ListSets')"/>
                </xsl:attribute>
            </oai_voc:origin>
		</oai_voc:Set>
	</xsl:template>
</xsl:stylesheet>
