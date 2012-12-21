package at.ac.univie.mminf.oai2lod;

import com.hp.hpl.jena.vocabulary.RDFS;

import at.ac.univie.mminf.oai2lod.config.OAI2LODConfigLoader;
import at.ac.univie.mminf.oai2lod.config.OAI2LODConfig;
import junit.framework.TestCase;

public class OAI2LODConfigTest extends TestCase {

	public OAI2LODConfigTest(String msg) throws Exception {
		super(msg);
	}

	public void testServerConfig() throws Exception {
		String configFile = "test-config2.n3";

		OAI2LODConfigLoader loader = new OAI2LODConfigLoader(configFile);
		OAI2LODConfig config = loader.load();

		OAI2LODConfig.Server server_config = config.getServer();

		assertEquals("http://localhost:2020/", server_config.getBaseURL());
		assertEquals(2020, server_config.getPort());
		assertEquals("Example OAI2LOD Server", server_config.getServerName());

		// test oai endpoint settings
		OAI2LODConfig.OAIServer oai_config = server_config.getOaiServer();

		assertEquals("http://memory.loc.gov/cgi-bin/oai2_0", oai_config
				.getServerURL());
		assertEquals("oai_dc", oai_config.getMetadataPrefix());
		assertEquals("xsl/oai_dc2rdf_xml.xsl", oai_config.getStyleSheet());
		assertEquals(50, oai_config.getMaxRecords());

		// test linking settings
		OAI2LODConfig.LinkedSparqlEndpoint linking_config = server_config
				.getSparqlEndpoint();
		assertEquals("http://DBpedia.org/sparql", linking_config
				.getSparqlService());
		assertEquals(5000, linking_config.getMaxResults());
		assertEquals(1, linking_config.getLinkingRules().size());

		OAI2LODConfig.LinkingRule rule1 = linking_config.getLinkingRules()
				.iterator().next();

		assertNotNull(rule1);

		assertEquals("http://www.mediaspaces.info/vocab/oai-pmh.rdf#Item",
				rule1.getSourceType());
		assertEquals("http://purl.org/dc/elements/1.1/subject", rule1
				.getSourceProperty());
		assertEquals("http://dbpedia.org/class/yago/City108524735", rule1
				.getTargetType());
		assertEquals("http://www.w3.org/2000/01/rdf-schema#label", rule1
				.getTargetProperty());
		assertEquals(RDFS.seeAlso.getURI().toString(), rule1
				.getLinkingProperty());
		assertEquals("uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein",
				rule1.getSimilarityMetrics());
		assertEquals(0.25f, rule1.getMinSimilarity());

	}

}
