package at.ac.univie.mminf.oai2lod.linking;

import at.ac.univie.mminf.oai2lod.config.OAI2LODConfig;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class LinkingJobMock extends LinkingJob {

	private Model dummyRemoteModel;
	
	int no_calls = 0;
	
	
	public LinkingJobMock(OAI2LODConfig config, Model targetModel, Model dummyRemoteModel){
		super(config, targetModel);
		this.dummyRemoteModel = dummyRemoteModel;
	}


	@Override
	protected Model retrieveRemoteData(String sparqlService, String targetType,
			String targetProperty, int limit, int offset) {
		

		if(this.no_calls == 0){
			
			this.no_calls = 1;
			
			return this.dummyRemoteModel;
			
			
		}else{
			
			Model emptyDummmyModel = ModelFactory.createDefaultModel();
			return emptyDummmyModel;
		}
		
		
		
	}
	
	
	
	
	
}
