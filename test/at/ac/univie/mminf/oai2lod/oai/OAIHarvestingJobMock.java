package at.ac.univie.mminf.oai2lod.oai;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import at.ac.univie.mminf.oai2lod.TestDataSets;
import at.ac.univie.mminf.oai2lod.config.OAI2LODConfig;

import com.hp.hpl.jena.rdf.model.Model;

public class OAIHarvestingJobMock extends OAIHarvestingJob {

	// default
	private Map<String, String> listRecordResponses = TestDataSets.INFOMOTIONS_LISTRECORD_ALL;

	// default
	private String listSetsResponse = TestDataSets.LOC_LISTSETS;

	public OAIHarvestingJobMock(OAI2LODConfig config, Model targetModel) {

		super(config, targetModel);
		// TODO Auto-generated constructor stub
	}

	public OAIHarvestingJobMock() {
		super();
	}

	public void setListRecordResponses(Map<String, String> listRecordResponses) {
		this.listRecordResponses = listRecordResponses;
	}

	public void setListSetsResponse(String listSetResponse) {
		this.listSetsResponse = listSetResponse;
	}

	@Override
	protected Document sendListRecordsRequest(String resumptionToken)
			throws Exception {

		if (listRecordResponses == null) {
			throw new Exception("No ListRecords response files given");
		}

		String responseFile = null;

		if (resumptionToken == null) {
			responseFile = listRecordResponses.get("NO_TOKEN");
		} else {
			responseFile = listRecordResponses.get(resumptionToken);
		}

		Document xml_file = loadDocument(responseFile);

		return xml_file;

	}

	@Override
	protected Document sendListSetsRequest() throws Exception {

		if (listSetsResponse == null) {
			throw new Exception("No ListSets response files given");
		}

		Document xml_file = loadDocument(listSetsResponse);

		return xml_file;
	}

	private Document loadDocument(String responseFile) {

//		InputStream in = HarvestingJobTest.class.getClassLoader()
//				.getResourceAsStream(responseFile);
		
		InputStream in;
		try {
			in = new FileInputStream(responseFile);
		} catch (FileNotFoundException e1) {
			throw new RuntimeException("Could not load file: "+responseFile, e1);
		}
		

		SAXBuilder builder = new SAXBuilder();
		Document xml_file;
		try {
			xml_file = builder.build(in);
			in.close();
		} catch (JDOMException e) {
			throw new RuntimeException("Error when parsing XML file: "
					+ responseFile);
		} catch (IOException e) {
			throw new RuntimeException("Error when loading XML file: "
					+ responseFile);
		}

		return xml_file;

	}

}
