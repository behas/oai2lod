# OAI2LOD Server

The OAI2LOD Server exposes any [OAI-PMH](http://www.openarchives.org/pmh/) compliant metadata repository according as [Linked Data](http://www.w3.org/DesignIssues/LinkedData.html). Its architecture, especially the front-end, is based on the [D2R Server](http://sourceforge.net/projects/d2rq-map/).

The Open Archives Initiatives Protocol for Metadata Harvesting (OAI-PMH) is a web-based protocol for harvesting metadata in any format from remote metadata repositories that provide an OAI-PMH enabled server. In recent years the protocol has gained much attention in the digital libraries and archives domain and many institutions already provide such a service. [Here](http://www.openarchives.org/Register/BrowseSites) is a list of registered data providers, among them the [Library of Congress OAI Repository](http://memory.loc.gov/cgi-bin/oai2_0?verb=Identify), the [National Library of Australia](http://www.nla.gov.au/apps/oaicat/servlet/OAIHandler?verb=Identify), or the [Austrian National Libraries Image Archive](http://oai-bdb.onb.ac.at/Script/oai2.aspx?verb=Identify).

## Installation

	git clone
	cd oai2lod
	ant build


## Open Issues

* RDF persistence (harvested metadata are currently stored in-memory)
* periodic updates / scheduler
* support for deleted/updated records
* full MODS support