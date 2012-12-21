package at.ac.univie.mminf.oai2lod.config;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class holds the configuration for an OAI2LOD instance; it is read from
 * an RDF configuration model,
 * 
 * It resembles the following class hierarchy:
 * 
 * Server |- OAIServer |- LinkedSPARQLEndpoint
 * 
 * @author haslhofer
 * 
 */
public class OAI2LODConfig {

	private Log log = LogFactory.getLog(OAI2LODConfig.class);

	private Server server;

	public OAI2LODConfig() {

	}

	protected Server createServerConfig() {

		Server server = new Server();

		this.server = server;

		return server;

	}

	public Server createServerConfig(String baseURL) {

		Server server = new Server(baseURL);

		this.server = server;

		return server;
	}

	public Server createServerConfig(String baseURL, String serverName, int port) {

		Server server = new Server(baseURL, serverName, port);

		this.server = server;

		return server;
	}

	public Server getServer() {
		return server;
	}

	public boolean isValid() {

		if (server == null || !server.isValid()) {
			log.warn("Server configuration missing");
			return false;
		}

		return true;
	}

	/**
	 * 
	 * Configuration class containing the OAI2LOD Server settings
	 * 
	 * @author haslhofer
	 * 
	 */
	public class Server {

		/* mandatory fields */
		private String baseURL = "http://localhost:2020";

		private String serverName = "OAI2LOD Server";

		private int port = 2020;

		private OAIServer oaiServer = null;

		/* optional fields */
		private LinkedSparqlEndpoint sparqlEndpoint = null;

		protected Server() {

		}

		protected Server(String baseURL) {
			this.baseURL = baseURL;
		}

		protected Server(String baseURL, String serverName, int port) {
			this(baseURL);
			this.serverName = serverName;
			this.port = port;
		}

		protected OAIServer createOAIServerConfig() {

			OAIServer oaiServer = new OAIServer();

			this.oaiServer = oaiServer;

			return oaiServer;
		}

		public OAIServer createOAIServerConfig(String serverURL,
				String metadataPrefix, String styleSheet) {

			return createOAIServerConfig(serverURL, metadataPrefix, styleSheet,
					-1);

		}

		public OAIServer createOAIServerConfig(String serverURL,
				String metadataPrefix, String styleSheet, int maxRecords) {

			OAIServer oaiServer = new OAIServer(serverURL, metadataPrefix,
					styleSheet, maxRecords);

			this.oaiServer = oaiServer;

			return oaiServer;

		}

		public LinkedSparqlEndpoint createLinkedSparqlEndpoint(
				String sparqlService, int maxResults) {

			LinkedSparqlEndpoint sparqlEndpoint = new LinkedSparqlEndpoint(
					sparqlService, maxResults);

			this.sparqlEndpoint = sparqlEndpoint;

			return sparqlEndpoint;

		}

		protected LinkedSparqlEndpoint createLinkedSparqlEndpoint() {

			LinkedSparqlEndpoint sparqlEndpoint = new LinkedSparqlEndpoint();

			this.sparqlEndpoint = sparqlEndpoint;

			return sparqlEndpoint;

		}

		public boolean isValid() {

			if (baseURL == null) {
				log.warn("Config: mandatory field 'baseURL' is missing");
				return false;
			} else if (oaiServer == null || !oaiServer.isValid()) {
				log.warn("OAI2LOD server configuration missing");
				return false;
			} else if (sparqlEndpoint != null && !sparqlEndpoint.isValid()) {
				return false;
			}

			return true;
		}

		/* setter/getter */
		public String getBaseURL() {
			return baseURL;
		}

		public void setBaseURL(String baseURL) {
			this.baseURL = baseURL;
		}

		public String getServerName() {
			return serverName;
		}

		public void setServerName(String serverName) {
			this.serverName = serverName;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public OAIServer getOaiServer() {
			return oaiServer;
		}

		public LinkedSparqlEndpoint getSparqlEndpoint() {
			return sparqlEndpoint;
		}

	}

	/**
	 * 
	 * Configuration class containing the OAI-PMH endpoint settings
	 * 
	 * @author haslhofer
	 * 
	 */
	public class OAIServer {

		/* mandatory fields */
		private String serverURL = null;
		private String metadataPrefix = null;
		private String styleSheet = null;

		/* optional fields */
		private int maxRecords = -1;

		protected OAIServer() {

		}

		public OAIServer(String serverURL, String metadataPrefix,
				String styleSheet) {

			this.serverURL = serverURL;
			this.metadataPrefix = metadataPrefix;
			this.styleSheet = styleSheet;

		}

		public OAIServer(String serverURL, String metadataPrefix,
				String styleSheet, int maxRecords) {
			this(serverURL, metadataPrefix, styleSheet);
			this.maxRecords = maxRecords;

		}

		/* setter / getter */

		public String getServerURL() {
			return serverURL;
		}

		public void setServerURL(String serverURL) {
			this.serverURL = serverURL;
		}

		public String getMetadataPrefix() {
			return metadataPrefix;
		}

		public void setMetadataPrefix(String metadataPrefix) {
			this.metadataPrefix = metadataPrefix;
		}

		public String getStyleSheet() {
			return styleSheet;
		}

		public void setStyleSheet(String styleSheet) {
			this.styleSheet = styleSheet;
		}

		public int getMaxRecords() {
			return maxRecords;
		}

		public void setMaxRecords(int maxRecords) {
			this.maxRecords = maxRecords;
		}

		/* validation */
		public boolean isValid() {

			if (serverURL == null) {
				log
						.warn("Configuration error: mandatory field 'baseURL' is missing");
				return false;
			} else if (metadataPrefix == null) {
				log
						.warn("Configuration error: mandatory field 'metadataPrefix' is missing");
				return false;
			} else if (styleSheet == null) {
				log
						.warn("Configuration error: mandatory field 'stylesheet' is missing");
				return false;
			} else if (maxRecords < -1) {
				log
						.warn("Configuration error: field 'maxRecords' out of range (-1 <= maxRecords <= maxint)");
			}

			return true;
		}

	}

	/**
	 * 
	 * Configuration class containing the settings and linking rules of the
	 * SPARQL endpoint to be linked
	 * 
	 * @author haslhofer
	 * 
	 */
	public class LinkedSparqlEndpoint {

		private String sparqlService = null;

		private int maxResults = 500;

		private Set<LinkingRule> linkingRules = new HashSet<LinkingRule>();

		protected LinkedSparqlEndpoint() {

		}

		public LinkedSparqlEndpoint(String sparqlService, int maxResults) {
			this.sparqlService = sparqlService;
			this.maxResults = maxResults;
		}

		public LinkingRule createLinkingRule(String sourceType,
				String sourceProperty, String targetType,
				String targetProperty, String linkingProperty, String similarityMetrics, float maxDistance) {

			LinkingRule rule = new LinkingRule(sourceType, sourceProperty,
					targetType, targetProperty, linkingProperty, similarityMetrics, maxDistance);

			this.linkingRules.add(rule);

			return rule;

		}

		public LinkingRule createLinkingRule() {

			LinkingRule rule = new LinkingRule();

			this.linkingRules.add(rule);

			return rule;

		}

		public String getSparqlService() {
			return sparqlService;
		}

		public void setSparqlService(String sparqlService) {
			this.sparqlService = sparqlService;
		}

		public int getMaxResults() {
			return this.maxResults;
		}

		public void setMaxResults(int maxResults) {
			this.maxResults = maxResults;
		}

		public Set<LinkingRule> getLinkingRules() {
			return this.linkingRules;
		}

		/* validation */
		public boolean isValid() {

			if (sparqlService == null) {
				log
						.warn("Configuration error: mandatory field 'sparqlService' is missing");
				return false;
			} else if (linkingRules == null) {
				log
						.warn("Configuration error: mandatory field 'linkingRules' is missing");
				return false;
			} else if (maxResults < 0) {
				log
						.warn("Configuration error: field 'maxResult' may not be less than 0");
				return false;
			}

			for (LinkingRule rule : linkingRules) {
				if (!rule.isValid()) {
					return false;
				}
			}

			return true;
		}

	}

	/**
	 * A linking Rule which OAI2LOD resources (having a specific type and
	 * property) should be liked with with resource residing in a remote SPARQL
	 * endpoint (also identified by a specific type and property)
	 * 
	 * @author haslhofer
	 * 
	 */
	public class LinkingRule {

		private String sourceType;

		private String sourceProperty;

		private String targetType;

		private String targetProperty;

		private String linkingProperty;

		private String similarityMetrics = "uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein";

		private float minSimilarity = 1;

		protected LinkingRule() {

		}

		public LinkingRule(String sourceType, String sourceProperty,
				String targetType, String targetProperty,
				String linkingProperty, String similarityMetrics,
				float minSimilarity) {
			super();
			this.sourceType = sourceType;
			this.sourceProperty = sourceProperty;
			this.targetType = targetType;
			this.targetProperty = targetProperty;
			this.linkingProperty = linkingProperty;
			this.similarityMetrics = similarityMetrics;
			this.minSimilarity = minSimilarity;
		}

		public String getSourceType() {
			return sourceType;
		}

		public void setSourceType(String sourceType) {
			this.sourceType = sourceType;
		}

		public String getSourceProperty() {
			return sourceProperty;
		}

		public void setSourceProperty(String sourceProperty) {
			this.sourceProperty = sourceProperty;
		}

		public String getTargetType() {
			return targetType;
		}

		public void setTargetType(String targetType) {
			this.targetType = targetType;
		}

		public String getTargetProperty() {
			return targetProperty;
		}

		public void setTargetProperty(String targetProperty) {
			this.targetProperty = targetProperty;
		}

		public String getLinkingProperty() {
			return linkingProperty;
		}

		public void setLinkingProperty(String linkingProperty) {
			this.linkingProperty = linkingProperty;
		}

		public String getSimilarityMetrics() {
			return similarityMetrics;
		}

		public void setSimilarityMetrics(String similarityMetrics) {
			this.similarityMetrics = similarityMetrics;
		}

		public float getMinSimilarity() {
			return minSimilarity;
		}

		public void setMinSimilarity(float minSimilarity) {
			this.minSimilarity = minSimilarity;
		}

		/* validation */
		public boolean isValid() {

			if (sourceType == null) {
				log
						.warn("Configuration error: mandatory field 'sourceType' is missing");
				return false;
			} else if (sourceProperty == null) {
				log
						.warn("Configuration error: mandatory field 'sourceProperty' is missing");
				return false;
			} else if (targetType == null) {
				log
						.warn("Configuration error: mandatory field 'targetType' is missing");
				return false;
			} else if (targetProperty == null) {
				log
						.warn("Configuration error: mandatory field 'targetProperty' is missing");
			} else if (linkingProperty == null) {
				log
						.warn("Configuration error: mandatory field 'linkingProperty' is missing");
			}

			return true;
		}

	}

}
