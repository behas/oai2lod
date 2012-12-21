package at.ac.univie.mminf.oai2lod.oai;

import java.util.Map;

import at.ac.univie.mminf.oai2lod.config.OAI2LODConfig;

import com.hp.hpl.jena.rdf.model.Model;

public class OAIControllerMock extends OAIController {

	private Map<String, String> listRecordResponses;

	private String listSetResponse;

	@Override
	public OAIHarvestingJob createOAIHarvestingJob(
			OAI2LODConfig jobConfig, Model targetModel) {

		OAIHarvestingJobMock mock = new OAIHarvestingJobMock(jobConfig,
				targetModel);

		if (this.listRecordResponses != null) {
			mock.setListRecordResponses(this.listRecordResponses);
		}

		if (this.listSetResponse != null) {
			mock.setListSetsResponse(listSetResponse);
		}

		return mock;

	}

	public void setListRecordResponses(Map<String, String> listRecordResponses) {
		this.listRecordResponses = listRecordResponses;
	}

	public void setListSetResponse(String listSetResponse) {
		this.listSetResponse = listSetResponse;
	}

}
