package at.ac.univie.mminf.oai2lod;

import info.mediaspaces.mapping.MappingFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joseki.RDFServer;
import org.joseki.Registry;
import org.joseki.Service;
import org.joseki.ServiceRegistry;
import org.joseki.processors.SPARQL;

import at.ac.univie.mminf.oai2lod.config.OAI2LODConfig;
import at.ac.univie.mminf.oai2lod.config.OAI2LODConfigLoader;
import at.ac.univie.mminf.oai2lod.linking.LinkingJob;
import at.ac.univie.mminf.oai2lod.oai.OAIController;
import at.ac.univie.mminf.oai2lod.oai.OAIHarvestingJob;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerFactory;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry;

public class OAI2LODServer {

	private static Log log = LogFactory.getLog(OAI2LODServer.class);

	private final static String SPARQL_SERVICE_NAME = "sparql";
	private final static String RESOURCE_SERVICE_NAME = "resource";
	private final static String DEFAULT_BASE_URI = "http://localhost";
	private final static String DEFAULT_SERVER_NAME = "OAI2LOD Server";
	private final static String SERVER_INSTANCE = "OAI2LODServer.SERVER_INSTANCE";

	/** d2rq mapping file */
	private String configFile;

	/** config file parser and Java representation */
	private OAI2LODConfigLoader config_loader = null;

	private OAI2LODConfig config = null;

	/** server port from command line, overrides port in config file */
	private int overridePort = -1;

	/** base URI from command line */
	private String overrideBaseURI = null;

	/** the dataset containing the harvested data */
	private DataSource dataset;

	private Model targetModel;

	/** the OAI-PMH Harvesting Job controler */
	private OAIController controller = OAIController.INSTANCE;

	public void putIntoServletContext(ServletContext context) {
		context.setAttribute(SERVER_INSTANCE, this);
	}

	public static OAI2LODServer fromServletContext(ServletContext context) {
		return (OAI2LODServer) context.getAttribute(SERVER_INSTANCE);
	}

	public void overridePort(int port) {
		log.info("using port " + port);
		this.overridePort = port;
	}

	public void overrideBaseURI(String baseURI) {

		// This is a hack to allow hash URIs to be used at least in the
		// SPARQL endpoint. It will not work in the Web interface.
		if (!baseURI.endsWith("/") && !baseURI.endsWith("#")) {
			baseURI += "/";
		}
		if (baseURI.contains("#")) {
			log.warn("Base URIs containing '#' may not work correctly!");
		}

		log.info("using custom base URI: " + baseURI);
		this.overrideBaseURI = baseURI;
	}

	public void setConfigFile(String configFileURL) {
		configFile = configFileURL;
	}

	public String baseURI() {
		if (this.overrideBaseURI != null) {
			return this.overrideBaseURI;
		}
		if (this.config.getServer().getBaseURL() != null) {
			return this.config.getServer().getBaseURL();
		}
		if (this.port() == 80) {
			return OAI2LODServer.DEFAULT_BASE_URI + "/";
		}
		return OAI2LODServer.DEFAULT_BASE_URI + ":" + this.port() + "/";
	}

	public int port() {
		if (this.overridePort != -1) {
			return this.overridePort;
		}
		if (this.config.getServer().getPort() != -1) {
			return this.config.getServer().getPort();
		}
		return OAI2LODJettyLauncher.DEFAULT_PORT;
	}

	public String serverName() {
		if (this.config.getServer().getServerName() != null) {
			return this.config.getServer().getServerName();
		}
		return OAI2LODServer.DEFAULT_SERVER_NAME;
	}

	public OAI2LODConfig getConfig() {
		return this.config;
	}
	
	
	public boolean hasTruncatedResults() {
		// return dataset.hasTruncatedResults();
		return false;
	}

	public String resourceBaseURI() {
		// This is a hack to allow hash URIs to be used at least in the
		// SPARQL endpoint. It will not work in the Web interface.
		if (this.baseURI().endsWith("#")) {
			return this.baseURI();
		}
		return this.baseURI() + OAI2LODServer.RESOURCE_SERVICE_NAME + "/";
	}

	public String graphURLDescribingResource(String resourceURI) {
		if (resourceURI.indexOf(":") == -1) {
			resourceURI = resourceBaseURI() + resourceURI;
		}
		String query = "DESCRIBE <" + resourceURI + ">";
		try {
			return this.baseURI() + OAI2LODServer.SPARQL_SERVICE_NAME
					+ "?query=" + URLEncoder.encode(query, "utf-8");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String dataURL(String relativeResourceURI) {
		return this.baseURI() + "data/" + relativeResourceURI;
	}

	public String pageURL(String relativeResourceURI) {
		return this.baseURI() + "page/" + relativeResourceURI;
	}

	public Model getModel() {
		return this.targetModel;
	}

	public void start() {

		MappingFactory.registerPropertyFunctions();

		log.info("using config file: " + configFile);

		this.config_loader = new OAI2LODConfigLoader(configFile);
		this.config = this.config_loader.load();

		if (this.targetModel == null) {
			this.targetModel = ModelFactory.createDefaultModel();
		}

		this.dataset = DatasetFactory.create();
		this.dataset.setDefaultModel(this.targetModel);

		DescribeHandlerRegistry.get().clear();
		DescribeHandlerRegistry.get().add(
				new FindDescribeHandlerFactory(this.dataset.getDefaultModel()));

		Registry.add(RDFServer.ServiceRegistryName,
				createJosekiServiceRegistry());
		
		startHarvestingJob();

		// then link
		if (this.config.getServer().getSparqlEndpoint() != null) {
			startLinkingJob();
		}

		
		
	}

	/* OAI harvesting stuff */

	private void startHarvestingJob() {
		try {
			if (this.targetModel == null) {
				log
						.warn("No targetModel available. Cannot start OAI Harvestiing Job");
				return;
			}

			OAIHarvestingJob job = this.controller.createOAIHarvestingJob(
					config, this.targetModel);

			job.harvestMetadata();
		} catch (Exception e) {
			log.error("Exception during harvesting process", e);
			return;
		}

	}

	/* Linking Job */

	private void startLinkingJob() {

		if (this.targetModel == null) {
			log
					.warn("No targetModel available. Cannot start OAI Harvestiing Job");
			return;
		}
		try {

			LinkingJob job = new LinkingJob(config, this.targetModel);

			job.linkData();
		} catch (Exception e) {
			log.error("Exception during linking process", e);
			return;
		}

	}

	public void setOAIController(OAIController controller) {
		this.controller = controller;
	}

	public void setOAI2LODConfig(OAI2LODConfig config) {
		this.config = config;
	}

	protected void setModel(Model model) {

		this.targetModel = model;

	}

	protected ServiceRegistry createJosekiServiceRegistry() {
		ServiceRegistry services = new ServiceRegistry();
		Service service = new Service(new SPARQL(),
				OAI2LODServer.SPARQL_SERVICE_NAME, new OAI2LODDatasetDesc(
						this.dataset));

		services.add(OAI2LODServer.SPARQL_SERVICE_NAME, service);
		return services;
	}

	private class FindDescribeHandlerFactory implements DescribeHandlerFactory {
		private final Model dataModel;

		FindDescribeHandlerFactory(Model dataModel) {
			this.dataModel = dataModel;
		}

		public DescribeHandler create() {
			return new OAI2LODFindDescribeHandler(dataModel, OAI2LODServer.this);
		}
	}

}
