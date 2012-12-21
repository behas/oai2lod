package at.ac.univie.mminf.oai2lod.oai;

import at.ac.univie.mminf.oai2lod.config.OAI2LODConfig;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Singleton OAI Controler for steering OAI Harvesting Jobs
 * 
 * @author haslhofer
 * 
 */
public class OAIController {

	public static OAIController INSTANCE = new OAIController();

	protected OAIController() {

	}

	protected static OAIController getInstance() {
		return new OAIController();
	}

	public OAIHarvestingJob createOAIHarvestingJob(
			OAI2LODConfig jobConfig, Model targetModel) {

		OAIHarvestingJob job = new OAIHarvestingJob(jobConfig, targetModel);

		return job;

	}

}
