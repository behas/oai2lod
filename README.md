# OAI2LOD Server

The OAI2LOD Server exposes any [OAI-PMH](http://www.openarchives.org/pmh/) compliant metadata repository according as [Linked Data](http://www.w3.org/DesignIssues/LinkedData.html). Its architecture, especially the front-end, is based on the [D2R Server](http://sourceforge.net/projects/d2rq-map/).

The Open Archives Initiatives Protocol for Metadata Harvesting (OAI-PMH) is a web-based protocol for harvesting metadata in any format from remote metadata repositories that provide an OAI-PMH enabled server. In recent years the protocol has gained much attention in the digital libraries and archives domain and many institutions already provide such a service. [Here](http://www.openarchives.org/Register/BrowseSites) is a list of registered data providers, among them the [Library of Congress OAI Repository](http://memory.loc.gov/cgi-bin/oai2_0?verb=Identify), the [National Library of Australia](http://www.nla.gov.au/apps/oaicat/servlet/OAIHandler?verb=Identify), or the [Austrian National Libraries Image Archive](http://oai-bdb.onb.ac.at/Script/oai2.aspx?verb=Identify).

## Installation

	git clone git://github.com/behas/oai2lod.git
	cd oai2lod
	ant build

## Starting OAI2LOD Server

You need to pass the name of a configuration file to the OAI2LOD server. Example configurations can be found in `doc/sample_config`. 

	cp doc/sample_config/loc_config_dc.n3 .
	chmod u+x oai2lod-server
	oai2lod-server loc_config_dc.n3

Open [http://localhost:2020](http://localhost:2020) in your browser.

Shut down OAI2LOD using CTRL-C or kill the process...

## Configuration Guide

The main configuration file specifying the OAI endpoint, the number of records to be harvested etc. is written in N3 syntax. Here is an example, for an OAI2LOD server running on port 2020, exposing metadata from the Austrian National Libraries Image Archive. It is linked with DBPedia and links resources based on specified types and properties in the source and target data sources:


	@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
	@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
	@prefix owl:  <http://www.w3.org/2002/07/owl#> .
	@prefix foaf: <http://xmlns.com/foaf/0.1/> .
	@prefix oai2lod: <http://www.mediaspaces.info/vocab/oai2lod-server-config.rdf#> .

	<> a oai2lod:Server;
		rdfs:label "Example OAI2LOD Server";
		oai2lod:port 2020;
		oai2lod:baseURI <http://localhost:2020/>;
		oai2lod:publishes <oai1>;
		oai2lod:linkedWith <link1>;	
		.
		
	<oai1> a oai2lod:OAIServer;
		oai2lod:serverURL <http://oai-bdb.onb.ac.at/Script/oai2.aspx>;
		oai2lod:metadataPrefix "oai_dc";
		oai2lod:styleSheet "xsl/oai_dc2rdf_xml.xsl";
		oai2lod:maxRecords 50;
		.
		
	<link1> a oai2lod:LinkedSPARQLEndpoint;
		oai2lod:sparqlService <http://DBpedia.org/sparql>;
		oai2lod:maxResults 5000;
		oai2lod:linkingRule <lrule1>;
		.

	<lrule1> a oai2lod:LinkingRule;
		oai2lod:sourceType <http://www.mediaspaces.info/vocab/oai-pmh.rdf#Item>;
		oai2lod:sourceProperty <http://purl.org/dc/elements/1.1/subject>;
		oai2lod:targetType <http://dbpedia.org/class/yago/Capital108518505>;
		oai2lod:targetProperty <http://www.w3.org/2000/01/rdf-schema#label>;
		oai2lod:linkingProperty <http://www.w3.org/2000/01/rdf-schema#seeAlso>;
		oai2lod:similarityMetrics "uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein";
		oai2lod:minSimilarity 1.0;
		.	
	
The first part after the namespace declarations contains the server settings:
  
- the server name: `rdfs:label "Example OAI2LOD Server";`
- the server port: `oai2lod:port 2020;`
- the URL where the server can be reached: `oai2lod:baseURI <http://localhost:2020/>;` - could also be `www.mediaspaces.info:2020` -- do not forget the trailing slash!!!
- a reference to an OAI-PMH definition, which represents the second part of the server settings: `oai2lod:publishes <oai1>;`
- a reference to a (remote) SPARQL endpoint which represents the third part of the server settings: `oai2lod:linkedWith <link1>;`
  
The second part defines the OAI-PMH endpoint (<em>NOTE: v.0.2. supports only a single endpoint</em>):
  
- the URL of the OAI-PMH server: `oai2lod:serverURL <http://memory.loc.gov/cgi-bin/oai2_0>;`
- the metadata format to be harvested, identified by its <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#metadataPrefix">metadataPrefix</a>: `oai2lod:metadataPrefix "oai_dc";`
- the path to the stylesheet for transforming OAI-PMH XML metadata into RDF/XML: `oai2lod:styleSheet "xsl/oai_dc2rdf_xml.xsl";`
- the maximum number of records to be harvested: `oai2lod:maxRecords 50;` -- <em>The more records you harvest, the more memory is required. Currently the OAI2LOD Server is tested with max. 25.000 records, which is already enough for many OAI-endpoints.</em>
  
The third part defines a SPARQL endpoint this OAI2LOD instance should be linked with (<em>NOTE: v.0.2. supports only a single endpoint</em>):

- the URL of the SPARQL Service: `oai2lod:sparqlService <http://DBpedia.org/sparql>;`
- the maximum number of results requested in a single SPARQL call - or actuall the LIMIT of a query: `oai2lod:maxResults 5000;`
- a reference to one or more linking rules: `oai2lod:linkingRule <lrule1>;`
	
Link rules tell the OAI2LOD Server the conditions for linking a resource in the OAI2LOD data set with a resource in the remote data set. For each data set one must define source/target types as well as source/target properties. The linking algorithm then compares all values X, which are objects of a certain source property in the source data set, with all values Y, which are objects of a certain target property in the target data set. If they are similar, a link using a given property is created between the resources. For each linking rule, the user can define a minimum similarity threshold (between 0 and 1) and the similarity algorithm to be used. One can choose any algorithm provided by the [SimMetrics library](http://sourceforge.net/projects/simmetrics/). Here is the [JavaDoc](http://www.dcs.shef.ac.uk/~sam/simmetrics/index.html).
	
## Publications

Further details about OAI2LOD have been published in the following papers:

- [Interweaving OAI-PMH data sources with the linked data cloud](http://eprints.cs.univie.ac.at/73/)

		@article{haslhofer2010interweaving,
		  title={Interweaving OAI-PMH data sources with the linked data cloud},
		  author={Haslhofer, B. and Schandl, B.},
		  journal={International Journal of Metadata, Semantics and Ontologies},
		  volume={5},
		  number={1},
		  pages={17--31},
		  year={2010},
		  publisher={Inderscience}
		}


- [The OAI2LOD Server: Exposing OAI-PMH metadata as linked data](http://www.ra.ethz.ch/cdstore/www2008/events.linkeddata.org/ldow2008/papers/03-haslhofer-schandl-oai2lod-server.pdf)

		@inproceedings{haslhofer2008oai2lod,
		  title={The OAI2LOD Server: Exposing OAI-PMH metadata as linked data},
		  author={Haslhofer, B. and Schandl, B.},
		  booktitle={1st International Workshop on Linked Data on the Web (LDOW2008), co-located with WWW 2008},
		  year={2008}
		}




## Open Issues

* RDF persistence (harvested metadata are currently stored in-memory)
* periodic updates / scheduler
* support for deleted/updated records
* full MODS support
