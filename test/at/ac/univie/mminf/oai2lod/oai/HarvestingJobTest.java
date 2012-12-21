package at.ac.univie.mminf.oai2lod.oai;

import java.util.List;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import at.ac.univie.mminf.oai2lod.TestDataSets;
import at.ac.univie.mminf.oai2lod.config.OAI2LODConfig;
import at.ac.univie.mminf.oai2lod.vocab.OAI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class HarvestingJobTest extends TestCase {

	private OAI2LODConfig dc_config = null;

	public HarvestingJobTest() {
		super("HarvestingJob Test");
	}

	public void setUp() throws Exception {
		// dc_config = new OAI2LODConfig(
		// "http://localhost:1234/bla", "http://inst1.com/oai.aspx", null,
		// "xsl/oai_dc2rdf_xml.xsl");

		dc_config = new OAI2LODConfig();
		dc_config.createServerConfig("http://localhost:1234/bla")
				.createOAIServerConfig("http://inst1.com/oai.aspx", "oai_dc",
						"xsl/oai_dc2rdf_xml.xsl");

	}

	public void testErrorResponse() throws Exception {

		OAIHarvestingJobMock job = new OAIHarvestingJobMock();
		job.setListRecordResponses(TestDataSets.INFOMOTIONS_LISTRECORD_ERROR);
		job.setConfig(dc_config);

		Document response = job.sendListRecordsRequest(null);

		Document response_rdf_xml = job.handleOAIPMHResponse(response);
		
		assertNull(response_rdf_xml);

	}

	public void testListRecordResponse() throws Exception {

		OAIHarvestingJobMock job = new OAIHarvestingJobMock();
		job.setListRecordResponses(TestDataSets.INFOMOTIONS_LISTRECORD_SINGLE);
		job.setConfig(dc_config);
		job.initTransformation(dc_config.getServer().getOaiServer()
				.getStyleSheet());

		Document response = job.sendListRecordsRequest(null);

		Document response_rdf_xml = job.handleOAIPMHResponse(response);

		assertNotNull(response_rdf_xml);

	}

	public void testListSetsRecordResponse() throws Exception {
		OAIHarvestingJobMock job = new OAIHarvestingJobMock();

		job.setListSetsResponse(TestDataSets.LOC_LISTSETS);

		job.setConfig(dc_config);

		job.initTransformation(OAIHarvestingJob.LIST_SET_XSL);

		Document response = job.sendListSetsRequest();

		Document response_rdf_xml = job.handleOAIPMHResponse(response);

		assertNotNull(response_rdf_xml);

		Model model = job.convertToModel(response_rdf_xml);

		//model.write(System.out, "RDF/XML");

	}

	public void testCutRecords() throws Exception {
		OAIHarvestingJobMock job = new OAIHarvestingJobMock();
		job.setListRecordResponses(TestDataSets.INFOMOTIONS_LISTRECORD_SINGLE);
		job.setConfig(dc_config);

		Document response = job.sendListRecordsRequest(null);

		int no_records = job.countRecords(response);
		assertEquals(200, no_records);

		Document cut_response = job.cutRecords(response, 50);
		no_records = job.countRecords(cut_response);
		assertEquals(50, no_records);

	}

	public void testConvertToModel() throws Exception {
		OAIHarvestingJobMock job = new OAIHarvestingJobMock();
		job.setListRecordResponses(TestDataSets.INFOMOTIONS_LISTRECORD_SINGLE);
		job.setConfig(dc_config);
		job.initTransformation(dc_config.getServer().getOaiServer()
				.getStyleSheet());

		Document response = job.sendListRecordsRequest(null);

		/* count records */
		XPath xpath = XPath.newInstance("//oai_default_ns:record");
		xpath.addNamespace("oai_default_ns", response.getRootElement()
				.getNamespaceURI());

		@SuppressWarnings( { "unchecked" })
		List<Element> record_elements = xpath.selectNodes(response);

		int no_records = record_elements.size();

		/* do transformation */

		Document response_rdf_xml = job.handleOAIPMHResponse(response);
		assertNotNull(response_rdf_xml);

		// printDocument(response_rdf_xml);

		Model model = job.convertToModel(response_rdf_xml);

		assertNotNull(model);

		//model.write(System.out, "RDF/XML");

		/*
		 * check if no of Items in model is equal to the no of items in XML
		 * record
		 */
		int no_items = countItemsInModel(model);

		assertEquals(no_records, no_items);

	}

	public void testHarvestMetadata() throws Exception {

		Model targetModel = ModelFactory.createDefaultModel();

		OAIHarvestingJobMock job = new OAIHarvestingJobMock(dc_config,
				targetModel);
		job.setListRecordResponses(TestDataSets.INFOMOTIONS_LISTRECORD_ALL);

		job.harvestMetadata();

		int no_items = countItemsInModel(targetModel);
		assertEquals(1837, no_items);

	}

	public void testHarvestMetadataPartly() throws Exception {

		Model targetModel = ModelFactory.createDefaultModel();

		dc_config.getServer().getOaiServer().setMaxRecords(10);

		OAIHarvestingJobMock job = new OAIHarvestingJobMock(dc_config,
				targetModel);
		job.setListRecordResponses(TestDataSets.INFOMOTIONS_LISTRECORD_ALL);

		job.harvestMetadata();

		int no_items = countItemsInModel(targetModel);
		assertEquals(10, no_items);

	}

	// private helpers
	private int countItemsInModel(Model model) {

		StmtIterator stmt_iter = model.listStatements(null, RDF.type, OAI.Item);
		int no_items = 0;
		while (stmt_iter.hasNext()) {
			stmt_iter.next();
			no_items++;
		}
		return no_items;
	}

	private void printDocument(Document document) throws Exception {
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		out.output(document, System.out);
	}

}
