package at.ac.univie.mminf.oai2lod;

import at.ac.univie.mminf.oai2lod.config.OAI2LODConfigLoader;

public class ServerStarter {

	public static void main(String[] args) throws Exception {

		String configFileName = "./doc/sample_config/onb_config_tel_dbpedia.n3";
		
		//String configFileName = "./test/test-mods-config.n3";

		//String configFileName = "./test/test-config1.n3";

		
		OAI2LODJettyLauncher server = new OAI2LODJettyLauncher();

		server.setConfigFile(OAI2LODConfigLoader.toAbsoluteURI(configFileName));

		server.start();

	}
}
