package at.ac.univie.mminf.oai2lod.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.ac.univie.mminf.oai2lod.vocab.OAI2LOD;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OAI2LODConfigLoader {

	/**
	 * Accepts an absolute URI, relative file: URI, or plain file name
	 * (including names with spaces, Windows backslashes etc) and returns an
	 * equivalent full absolute URI.
	 */
	public static String toAbsoluteURI(String fileName) {
		// Windows? Convert \ to / in mapping file name
		// because we treat it as a URL, not a file name
		if (System.getProperty("os.name").toLowerCase().indexOf("win") != -1) {
			fileName = fileName.replaceAll("\\\\", "/");
		}
		try {
			if (fileName.matches("[a-zA-Z0-9]+:.*")
					&& new URI(fileName).isAbsolute()) {
				return fileName;
			}
			return new File(fileName).getAbsoluteFile().toURL()
					.toExternalForm();
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Log log = LogFactory.getLog(OAI2LODConfigLoader.class);

	private Model model = null;

	private String configURL;

	public OAI2LODConfigLoader(String configURL) {
		this.configURL = configURL;
	}

	public OAI2LODConfig load() throws JenaException {

		this.model = FileManager.get().loadModel(this.configURL);

		OAI2LODConfig config = new OAI2LODConfig();

		/* loading oai2lod server settings */
		OAI2LODConfig.Server serverConfig = config.createServerConfig();

		Resource server = findResourceOfType(OAI2LOD.Server);
		if (server == null) {
			return null;
		}
		Statement s = server.getProperty(OAI2LOD.baseURI);
		if (s != null) {
			serverConfig.setBaseURL(s.getResource().getURI());
		}
		s = server.getProperty(OAI2LOD.port);
		if (s != null) {
			String value = s.getLiteral().getLexicalForm();
			try {
				serverConfig.setPort(Integer.parseInt(value));

			} catch (NumberFormatException ex) {
				throw new JenaException("Illegal integer value '" + value
						+ "' for d2r:port");
			}
		}
		s = server.getProperty(RDFS.label);
		if (s != null) {
			serverConfig.setServerName(s.getString());
		}

		/* loading oai-endpoint settings */
		// TODO should also support multiple endpoints
		OAI2LODConfig.OAIServer oaiConfig = serverConfig
				.createOAIServerConfig();

		Resource oai_endpoint = findResourceOfType(OAI2LOD.OAIServer);
		if (oai_endpoint == null) {
			log.warn("No OAI Endpoint specified in configuration file");
			return null;
		}
		s = oai_endpoint.getProperty(OAI2LOD.serverURL);
		if (s == null) {
			log.warn("No serverURL specified for the OAI endpoint");
			return null;
		}

		oaiConfig.setServerURL(s.getResource().getURI());

		s = oai_endpoint.getProperty(OAI2LOD.metadataPrefix);
		if (s == null) {
			log.warn("No metadataPrefix specified for the OAI endpoint");
			return null;
		}

		oaiConfig.setMetadataPrefix(s.getLiteral().getLexicalForm());

		s = oai_endpoint.getProperty(OAI2LOD.styleSheet);
		if (s == null) {
			log.warn("No styleSheet file specified for the OAI endpoint");
			return null;
		}

		oaiConfig.setStyleSheet(s.getLiteral().getLexicalForm());

		s = oai_endpoint.getProperty(OAI2LOD.maxRecords);
		int maxRecords = -1;
		if (s != null) {
			String value = s.getLiteral().getLexicalForm();
			try {
				maxRecords = Integer.parseInt(value);
			} catch (NumberFormatException ex) {
				throw new JenaException("Illegal integer value '" + value
						+ "' for oai2lod:maxRecords");
			}
		}

		oaiConfig.setMaxRecords(maxRecords);

		/* loading the linking specification */
		ResIterator res_iter = this.model.listSubjectsWithProperty(RDF.type,
				OAI2LOD.LinkedSPARQLEndpoint);
		while (res_iter.hasNext()) {

			OAI2LODConfig.LinkedSparqlEndpoint endpointConfig = serverConfig
					.createLinkedSparqlEndpoint();

			Resource lsEndpoint = res_iter.nextResource();

			s = lsEndpoint.getProperty(OAI2LOD.sparqlService);

			if (s != null) {
				endpointConfig.setSparqlService(s.getResource().getURI());
			}

			s = lsEndpoint.getProperty(OAI2LOD.maxResults);
			int maxResults = 0;
			if (s != null) {
				String value = s.getLiteral().getLexicalForm();
				try {
					maxResults = Integer.parseInt(value);
				} catch (NumberFormatException ex) {
					throw new JenaException("Illegal integer value '" + value
							+ "' for oai2lod:maxResults");
				}
			}
			endpointConfig.setMaxResults(maxResults);

			/* load the linking rules */

			StmtIterator stmt_iter = lsEndpoint
					.listProperties(OAI2LOD.linkingRule);
			while (stmt_iter.hasNext()) {

				OAI2LODConfig.LinkingRule rule = endpointConfig
						.createLinkingRule();

				Resource lrule = (Resource) stmt_iter.nextStatement()
						.getObject();

				s = lrule.getProperty(OAI2LOD.sourceType);
				if (s != null) {
					rule.setSourceType(s.getResource().getURI());
				}
				s = lrule.getProperty(OAI2LOD.sourceProperty);
				if (s != null) {
					rule.setSourceProperty(s.getResource().getURI());
				}
				s = lrule.getProperty(OAI2LOD.targetType);
				if (s != null) {
					rule.setTargetType(s.getResource().getURI());
				}
				s = lrule.getProperty(OAI2LOD.targetProperty);
				if (s != null) {
					rule.setTargetProperty(s.getResource().getURI());
				}
				s = lrule.getProperty(OAI2LOD.linkingProperty);
				if (s != null) {
					rule.setLinkingProperty(s.getResource().getURI());
				}
				s = lrule.getProperty(OAI2LOD.similarityMetrics);
				if (s != null) {

					String metricsClass = s.getString();

					try {
						Class clazz = Class.forName(metricsClass);

						if (clazz != null) {
							rule.setSimilarityMetrics(metricsClass);

						}
					} catch (ClassNotFoundException e) {
						throw new JenaException(
								"Could not load string metrics class'"
										+ metricsClass);
					}

				}

				s = lrule.getProperty(OAI2LOD.minSimilarity);
				float minSimilarity = 0;
				if (s != null) {
					String value = s.getLiteral().getLexicalForm();
					try {
						minSimilarity = Float.parseFloat(value);
					} catch (NumberFormatException ex) {
						throw new JenaException("Illegal integer value '"
								+ value + "' for oai2lod:maxRecords");
					}
				}
				rule.setMinSimilarity(minSimilarity);

			}

		}

		return config;

	}

	private Resource findResourceOfType(Resource type) {
		ResIterator it = this.model.listSubjectsWithProperty(RDF.type, type);
		if (!it.hasNext()) {
			return null;
		}
		return it.nextResource();
	}
}