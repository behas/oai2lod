package at.ac.univie.mminf.oai2lod.linking;

import junit.framework.TestCase;
import at.ac.univie.mminf.oai2lod.config.OAI2LODConfig;
import at.ac.univie.mminf.oai2lod.vocab.OAI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class LinkingJobTest extends TestCase {

	public LinkingJobTest() {
		super("LinkingJob Test");
	}

	public void testRetrieveRemoteData() throws Exception {

		String sparqlService = "http://www.mediaspaces.info:2020/sparql";

		LinkingJob job = new LinkingJob();

		Model result = job.retrieveRemoteData(sparqlService, OAI.Item
				.toString(), DC.title.toString(), 100, 0);

		// result.write(System.out);

	}

	public void testLinking() throws Exception {

		// test source model
		String sourceNS = "http://bla.com/";

		Model targetModel = ModelFactory.createDefaultModel();

		Resource item1 = targetModel.createResource(sourceNS + "1");
		item1.addProperty(RDF.type, OAI.Item);
		item1.addProperty(DC.title, "TestTitle");

		// test target model
		String targetNS = "http://blub.com/";

		Model remoteModel = ModelFactory.createDefaultModel();

		Resource item2 = remoteModel.createResource(targetNS + "1");
		item2.addProperty(RDF.type, OAI.Item);
		item2.addProperty(DC.title, "TestTitle");

		// create dummy config
		OAI2LODConfig config = new OAI2LODConfig();
		OAI2LODConfig.LinkedSparqlEndpoint endpoint_config = config
				.createServerConfig("http://bla.com")
				.createLinkedSparqlEndpoint("http://blub.com", 100);
		endpoint_config
				.createLinkingRule(
						OAI.Item.toString(),
						DC.title.toString(),
						OAI.Item.toString(),
						DC.title.toString(),
						RDFS.seeAlso.toString(),
						"uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein",
						1.0f);

		LinkingJob linkingJob = new LinkingJobMock(config, targetModel,
				remoteModel);

		linkingJob.linkData();

		Model result = linkingJob.getTargetModel();

		result.write(System.out);

	}

}
