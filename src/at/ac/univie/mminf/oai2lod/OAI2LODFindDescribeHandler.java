package at.ac.univie.mminf.oai2lod;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A custom {@link DescribeHandler} that returns the results of a two-way
 * find.
 * 
 * TODO Is this thread-safe? ARQ uses just a single instance of this class.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 * @version $Id: FindDescribeHandler.java,v 1.11 2007/11/02 14:46:25 cyganiak Exp $
 */
public class OAI2LODFindDescribeHandler implements DescribeHandler {
	private Model dataModel;
	private Model resultModel;
	private final OAI2LODServer server;
	
	public OAI2LODFindDescribeHandler(Model dataModel, OAI2LODServer server) {
		this.dataModel = dataModel;
		this.server = server;
	}
	
	public void start(Model accumulateResultModel, Context qContext) {
		this.resultModel = accumulateResultModel;
		this.resultModel.setNsPrefix("rdfs", RDFS.getURI());
	}
	
	public void describe(Resource resource) {
		try {
		Model description = ModelFactory.createDefaultModel();
		Model seeAlsos = ModelFactory.createDefaultModel();
		description.add(this.dataModel.listStatements(resource, null, (RDFNode) null));
		description.add(this.dataModel.listStatements(null, null, resource));
		description.add(this.dataModel.listStatements(null, (Property) resource.as(Property.class), (RDFNode) null));
		ResIterator rit = description.listSubjects();
		while (rit.hasNext()) {
			addSeeAlsoStatement(rit.nextResource(), seeAlsos, resource.getURI());
		}
		NodeIterator nit = description.listObjects();
		while (nit.hasNext()) {
			addSeeAlsoStatement(nit.nextNode(), seeAlsos, resource.getURI());
		}
		resultModel.add(description);
		resultModel.add(seeAlsos);
		} catch (RuntimeException ex) {
			ex.printStackTrace(System.out);
			throw ex;
		}
	}

	public void finish() {
		// do nothing
	}

	private void addSeeAlsoStatement(RDFNode n, Model m, String currentResourceURI) {
		if (!n.isURIResource()) {
			return;
		}
		String resourceURI = n.asNode().getURI();
		if (currentResourceURI.equals(resourceURI)) {
			// Don't add seeAlso for the current resource pointing to its own description 
			return;
		}
		if (resourceURI.startsWith(server.baseURI())) {
			// Don't add seeAlso for dereferenceable URIs
			return;
		}
		String seeAlsoURI = server.graphURLDescribingResource(resourceURI);
		Resource nAsResource = m.getResource(resourceURI);
		nAsResource.addProperty(RDFS.seeAlso, m.createResource(seeAlsoURI));
	}
}
