package at.ac.univie.mminf.oai2lod.oai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.oclc.oai.harvester2.verb.ListRecords;
import org.oclc.oai.harvester2.verb.ListSets;

import at.ac.univie.mminf.oai2lod.config.OAI2LODConfig;
import at.ac.univie.mminf.oai2lod.vocab.MODS;
import at.ac.univie.mminf.oai2lod.vocab.OAI;
import at.ac.univie.mminf.oai2lod.vocab.OAI2LOD;
import at.ac.univie.mminf.oai2lod.vocab.ONB;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OAIHarvestingJob {

	private Log log = LogFactory.getLog(OAIHarvestingJob.class);

	/* the pattern in the style-sheet to be replaced with the real baseURI */
	private static String BASEURI_PATTERN = "http://baseURI";

	/* the pattern in the style-sheet to be replaced with the origin URI */
	private static String ORIGINURI_PATTERN = "http://sourceURI/";

	/* the stylesheet for transforming OAI ListSet responses to RDF/XML" */
	protected static String LIST_SET_XSL = "xsl/oai_sets2rdf_xml.xsl";

	/* the set of frequently used prefixes; TODO 3rd party prefixes? */
	private static Map<String, String> prefixMap = new HashMap<String, String>();

	static {
		prefixMap.put("dc", DC.getURI());
		prefixMap.put("rdf", RDF.getURI());
		prefixMap.put("rdfs", RDFS.getURI());
		prefixMap.put("oai", OAI.getURI());
		prefixMap.put("oai2lod", OAI2LOD.getURI());
		prefixMap.put("owl", OWL.getURI());
		prefixMap.put("mods", MODS.getURI());
		prefixMap.put("onb", ONB.getURI());
	}

	/* the Jena Model where everything is stored */
	private Model targetModel = null;

	/* the configuration for this harvesting job */
	private OAI2LODConfig config = null;

	/* the XSL transformation engine */
	private Transformer transformer = null;

	/*
	 * CONSTRUCTOR
	 */

	protected OAIHarvestingJob() {

	}

	public OAIHarvestingJob(OAI2LODConfig config, Model targetModel) {

		this.config = config;
		this.targetModel = targetModel;

		this.targetModel.setNsPrefixes(OAIHarvestingJob.prefixMap);

	}

	/**
	 * Main method controlling the Harvesting Job
	 * 
	 * @throws Exception
	 */
	public void harvestMetadata() throws Exception {

		// first retrieve the set information
		String oai_sets2rdf = OAIHarvestingJob.LIST_SET_XSL;
		initTransformation(oai_sets2rdf);

		Document listSetsResponse = sendListSetsRequest();

		Document listSets_rdf_xml = handleOAIPMHResponse(listSetsResponse);

		if (listSets_rdf_xml != null) {

			Model sets_rdf_xml = convertToModel(listSets_rdf_xml);
			targetModel.add(sets_rdf_xml);

		}

		// then harvest the metadata records

		String oai_md2rdf = this.getConfig().getServer().getOaiServer()
				.getStyleSheet();
		initTransformation(oai_md2rdf);

		boolean more_records = true;

		String resumptionToken = null;
		int no_records = 0;

		do {
			log.info("Harvesting records from position " + no_records + "...");
			// send the ListRecords request
			Document response = sendListRecordsRequest(resumptionToken);

			if (config.getServer().getOaiServer().getMaxRecords() != -1) {
				// count how many records still need to be harvested
				int records2harvest = config.getServer().getOaiServer()
						.getMaxRecords()
						- no_records;
				// cut the response if necessary
				cutRecords(response, records2harvest);

			}

			// transform the response into RDF/XML
			Document document_rdf_xml = handleOAIPMHResponse(response);
			if (document_rdf_xml == null) {
				break;
			}

			// create a Jena memory model from the response
			Model response_model = convertToModel(document_rdf_xml);
			// add the model to the job's target model
			targetModel.add(response_model);
			// check if there is a resumptionToken
			resumptionToken = getResumptionToken(response);
			// sum up the number of harvested records
			int harvested_records = countRecords(response);
			no_records = no_records + harvested_records;
			// check if more records need to be harvested
			if (no_records == config.getServer().getOaiServer().getMaxRecords()) {
				more_records = false;
			}
			log.info("Harvested " + harvested_records
					+ " records; continuing with resumption token: "
					+ resumptionToken);

		} while ((resumptionToken != null) && more_records);

	}

	/**
	 * Sends a ListRecord request to the OAI Server's base URL
	 * 
	 * @return the response JDOM XML document
	 * @throws Exception
	 */
	protected Document sendListSetsRequest() throws Exception {

		ListSets listSetsRequest = new ListSets(config.getServer()
				.getOaiServer().getServerURL());

		log.info("Sending OAI-PMH request: " + listSetsRequest.getRequestURL());

		return listSetsRequest.getJDomDocument();

	}

	/**
	 * Sends a ListRecord request to the OAI Server according to the Harvesting
	 * Job's Configuration.
	 * 
	 * @return the response JDOM XML document
	 * @throws Exception
	 */
	protected Document sendListRecordsRequest(String resumptionToken)
			throws Exception {

		ListRecords listRecordsRequest = null;

		if (resumptionToken == null) {
			listRecordsRequest = new ListRecords(config.getServer()
					.getOaiServer().getServerURL(), null, null, null, config
					.getServer().getOaiServer().getMetadataPrefix());
		} else {
			listRecordsRequest = new ListRecords(config.getServer()
					.getOaiServer().getServerURL(), resumptionToken);
		}

		log.info("Sending OAI-PMH request: "
				+ listRecordsRequest.getRequestURL());

		return listRecordsRequest.getJDomDocument();

	}

	/**
	 * replace baseURI pattern in style-sheet; initialize XSLT engine for
	 * transformation;
	 * 
	 * @throws Exception
	 */
	protected void initTransformation(String stylesheet) throws Exception {

		InputStream in = this.getClass().getClassLoader().getResourceAsStream(
				stylesheet);

		if (in == null) {
			throw new Exception("Cannot load stylesheet from classpath: "
					+ stylesheet);
		}

		String stylesheet_xml = OAIHarvestingJob.inputStreamAsString(in);
		in.close();

		stylesheet_xml = stylesheet_xml.replaceAll(
				OAIHarvestingJob.BASEURI_PATTERN, this.getConfig().getServer()
						.getBaseURL());

		stylesheet_xml = stylesheet_xml.replaceAll(
				OAIHarvestingJob.ORIGINURI_PATTERN, this.getConfig()
						.getServer().getOaiServer().getServerURL());

		Reader reader = new StringReader(stylesheet_xml);

		this.transformer = TransformerFactory.newInstance().newTransformer(
				new StreamSource(reader));

		// this.transformer = TransformerFactory.newInstance().newTransformer(
		// new StreamSource(in));
		reader.close();
	}

	/**
	 * Cuts a ListRecord Response to the given number of records. If the no of
	 * returned records is smaller than the given number of records, nothing
	 * happens.
	 * 
	 * @param response
	 * @param records2harvest
	 * @return
	 * @throws Exception
	 */
	protected Document cutRecords(Document response, int records2harvest)
			throws Exception {
		Namespace defaultNS = response.getRootElement().getNamespace();

		/* select record elements */
		Element listRecords = response.getRootElement().getChild("ListRecords",
				defaultNS);
		@SuppressWarnings( { "unchecked" })
		List<Element> record_elements = listRecords.getChildren("record",
				defaultNS);

		while (records2harvest < record_elements.size()) {
			Element last_record = record_elements
					.get(record_elements.size() - 1);
			last_record.detach();
		}

		return response;

	}

	/**
	 * Returns the resumptionToken from a ListRecords request; returns NULL if
	 * there isn't any
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	protected String getResumptionToken(Document response) throws Exception {

		Namespace defaultNS = response.getRootElement().getNamespace();
		Element resumptionToken = response.getRootElement().getChild(
				"ListRecords", defaultNS)
				.getChild("resumptionToken", defaultNS);
		if ((resumptionToken != null)
				&& (resumptionToken.getTextTrim().length() > 0)) {
			return resumptionToken.getTextTrim();
		} else {
			return null;
		}

	}

	/**
	 * Counts the number of record returned in an ListRecords response
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	protected int countRecords(Document response) throws Exception {
		Namespace defaultNS = response.getRootElement().getNamespace();

		/* select record elements using XPath */
		XPath xpath = XPath.newInstance("//oai_default_ns:record");
		xpath.addNamespace("oai_default_ns", defaultNS.getURI());

		@SuppressWarnings( { "unchecked" })
		List<Element> record_elements = xpath.selectNodes(response);

		int no_records = record_elements.size();

		return no_records;

	}

	/**
	 * Converts an RDF/XML Document into a Jena Memory Model
	 * 
	 * @param document_rdf_xml
	 * @return
	 * @throws Exception
	 */
	protected Model convertToModel(Document document_rdf_xml) throws Exception {

		String rdf_xml = print(document_rdf_xml.getRootElement());

		Reader reader = new StringReader(rdf_xml);

		Model mem_model = ModelFactory.createDefaultModel();
		mem_model.read(reader, "");

		return mem_model;

	}

	/**
	 * Parses the OAI List Records Response and applies the defined style-sheet
	 * to transform the response into an RDF/XML document.
	 * 
	 * Returns null if the response is an OAI-PMH Error
	 * 
	 */
	protected Document handleOAIPMHResponse(Document response)
			throws IOException, JDOMException {

		Namespace defaultNS = response.getRootElement().getNamespace();

		/* if OAI-PMH error -> return */
		if (response.getRootElement().getChild("error", defaultNS) != null) {
			Element error_el = response.getRootElement().getChild("error",
					defaultNS);
			log.warn("OAI response resulted in the following error"
					+ print(error_el));
			return null;
		}

		Document result = null;

		try {
			result = transform(response);
			return result;
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;

	}

	/**
	 * Transforms source XML according to previously initialized XSLT settings
	 * 
	 * @param source
	 * @return
	 * @throws TransformerException
	 */
	protected Document transform(Document source) throws TransformerException {

		JDOMSource src = new JDOMSource(source);

		JDOMResult target = new JDOMResult();

		transformer.transform(src, target);

		return target.getDocument();
	}

	/*
	 * PRIVATE STUFF
	 */

	/**
	 * Convert an InputStream to a String
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	private static String inputStreamAsString(InputStream stream)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		return sb.toString();
	}

	/**
	 * pretty print jdom elements
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	private String print(Element element) throws IOException {
		XMLOutputter out = new XMLOutputter();

		StringWriter writer = new StringWriter();
		out.output(element, writer);
		writer.close();

		return writer.toString();

	}

	/* setter/getter */
	public Model getTargetModel() {
		return targetModel;
	}

	public void setTargetModel(Model targetModel) {
		this.targetModel = targetModel;
	}

	public OAI2LODConfig getConfig() {
		return config;
	}

	public void setConfig(OAI2LODConfig config) {
		this.config = config;
	}

}
