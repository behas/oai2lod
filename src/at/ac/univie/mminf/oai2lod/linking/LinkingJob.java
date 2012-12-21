package at.ac.univie.mminf.oai2lod.linking;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import at.ac.univie.mminf.oai2lod.config.OAI2LODConfig;
import at.ac.univie.mminf.oai2lod.vocab.OAI;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

public class LinkingJob {

	/* for evaluation purposes only */

	private static boolean EVALUATE_LINKS = true;
	
	private static int NO_LINKS = 100; 

	private LinkTracker tracker = new LinkTracker();

	/* the Jena Model where everything is stored */
	private Model targetModel = null;

	/* the configuration for this harvesting job */
	private OAI2LODConfig config = null;

	// private
	Log log = LogFactory.getLog(LinkingJob.class);

	/*
	 * CONSTRUCTORS
	 */

	public LinkingJob(OAI2LODConfig config, Model targetModel) {

		this.config = config;
		this.targetModel = targetModel;

	}

	protected LinkingJob() {

	}

	public void linkData() {

		// get config data

		OAI2LODConfig.LinkedSparqlEndpoint endpoint = this.config.getServer()
				.getSparqlEndpoint();

		String sparqlService = endpoint.getSparqlService();

		int maxResults = endpoint.getMaxResults();

		Set<OAI2LODConfig.LinkingRule> linkingRules = endpoint
				.getLinkingRules();

		// perform linking for each linking rule *extremely inefficient*

		for (OAI2LODConfig.LinkingRule linkingRule : linkingRules) {

			String targetType = linkingRule.getTargetType();
			String targetProperty = linkingRule.getTargetProperty();
			String sourceType = linkingRule.getSourceType();
			String sourceProperty = linkingRule.getSourceProperty();
			String linkingProperty = linkingRule.getLinkingProperty();
			String similarityMetrics = linkingRule.getSimilarityMetrics();
			float minSimilarity = linkingRule.getMinSimilarity();

			Resource targetTypeRes = ResourceFactory.createResource(targetType);
			Property targetPropertyRes = ResourceFactory
					.createProperty(targetProperty);
			Resource sourceTypeRes = ResourceFactory.createResource(sourceType);
			Property sourcePropertyRes = ResourceFactory
					.createProperty(sourceProperty);

			Property linkingPropertyRes = ResourceFactory
					.createProperty(linkingProperty);

			// get an instance for similarity measurement

			InterfaceStringMetric metric = null;

			try {
				@SuppressWarnings("unchecked")
				Class<InterfaceStringMetric> metricClass = (Class<InterfaceStringMetric>) Class
						.forName(similarityMetrics);

				metric = metricClass.newInstance();

			} catch (Exception ex) {
				log.error("Error when instantating string metrics class: "
						+ similarityMetrics);
				return;
			}

			// do the job linking job

			int offsetCounter = 0;

			boolean moreRemoteData = true;

			while (moreRemoteData) {

				// fetch remote data according to type and property
				// configuration

				log.info("Retrieving remote data from offset position "
						+ offsetCounter);

				Model remoteResult = retrieveRemoteData(sparqlService,
						targetType, targetProperty, maxResults, offsetCounter);

				offsetCounter = offsetCounter + maxResults;

				// iterate trough all instance of type targetType
				ResIterator remoteTypeInst_iter = remoteResult
						.listSubjectsWithProperty(RDF.type, targetTypeRes);

				// if there is no such instance -> no data -> break;
				if (!remoteTypeInst_iter.hasNext()) {

					moreRemoteData = false;

					break;

				}

				while (remoteTypeInst_iter.hasNext()) {

					// iterate through all objects of property targetProperty
					Resource remoteTypeInst = remoteTypeInst_iter
							.nextResource();

					StmtIterator remoteValue_iter = remoteTypeInst
							.listProperties(targetPropertyRes);

					Set<LinkStatement> linkStatements = new HashSet<LinkStatement>();

					while (remoteValue_iter.hasNext()) {

						String remoteValue;

						RDFNode remoteNode = remoteValue_iter.nextStatement()
								.getObject();

						if (remoteNode.isLiteral()) {
							remoteValue = ((Literal) remoteNode)
									.getLexicalForm();
						} else {
							remoteValue = remoteNode.toString();
						}

						// search the local data set for a resource with type
						// sourceType, property sourceProperty and property
						// value
						// 'object'

						// list for storing the identified linking statements

						ResIterator sourceTypeInst_iter = this.targetModel
								.listSubjectsWithProperty(RDF.type,
										sourceTypeRes);

						while (sourceTypeInst_iter.hasNext()) {

							Resource sourceTypeInst = sourceTypeInst_iter
									.nextResource();

							StmtIterator sourceValue_iter = sourceTypeInst
									.listProperties(sourcePropertyRes);

							while (sourceValue_iter.hasNext()) {

								String sourceValue;

								RDFNode sourceNode = sourceValue_iter
										.nextStatement().getObject();

								if (sourceNode.isLiteral()) {
									sourceValue = ((Literal) sourceNode)
											.getLexicalForm();
								} else {
									sourceValue = sourceNode.toString();
								}

								float similarity = metric.getSimilarity(
										sourceValue, remoteValue);

								if (similarity >= minSimilarity) {

									LinkStatement linkStatement = new LinkStatement(
											sourceTypeInst, linkingPropertyRes,
											remoteTypeInst);

									if (!linkStatements.contains(linkStatement)) {

										linkStatements.add(linkStatement);

										log.info("Found link between :"
												+ sourceTypeInst.getURI()
												+ " and "
												+ remoteTypeInst.getURI()
												+ ", " + sourceValue + ", "
												+ remoteValue + ", "
												+ similarity);

										if (LinkingJob.EVALUATE_LINKS) {

											LinkTracker.Link link = new LinkTracker.Link(
													sourceTypeInst,
													linkingPropertyRes,
													remoteTypeInst,
													sourceValue, remoteValue,
													similarity);

											tracker.addLink(link);

										}

									}

								}

							}

						}

						for (LinkStatement stmt : linkStatements) {

							this.targetModel.add(stmt.toJenaStatement());

						}

					}

				}

			}

		}

		if (EVALUATE_LINKS) {

			tracker.saveRandomResults(NO_LINKS);
		}

	}

	protected Model retrieveRemoteData(String sparqlService, String targetType,
			String targetProperty, int limit, int offset) {

		String sparqlQuery = "PREFIX rdf: <" + RDF.getURI() + ">" +

		"PREFIX oai: <" + OAI.getURI() + ">" +

		"PREFIX dc: <" + DC.getURI() + ">" +

		"CONSTRUCT {" +

		"?res rdf:type <" + targetType + "> ." +

		"?res <" + targetProperty + "> ?value" +

		"}" +

		"WHERE {" +

		"?res rdf:type <" + targetType + "> ." +

		"?res <" + targetProperty + "> ?value" +

		"} " + "LIMIT " + new Integer(limit).toString() + "OFFSET"
				+ new Integer(offset).toString();

		Model result = null;

		try {
			Query query = QueryFactory.create(sparqlQuery);

			log.debug("Executing query:\n" + query.toString());

			QueryExecution qe = QueryExecutionFactory.sparqlService(
					sparqlService, query);

			result = qe.execDescribe();

			qe.close();

		} catch (Exception ex) {
			log.warn("Could not link data - error when sending " + sparqlQuery
					+ " to " + sparqlService);
			// if there is an error, return empty model
			return ModelFactory.createDefaultModel();
		}

		return result;

	}

	protected Model getTargetModel() {

		return this.targetModel;

	}

	/**
	 * Required because equality / contains check on Jena Statement does not
	 * work
	 * 
	 * @author haslhofer
	 * 
	 */
	static class LinkStatement {

		private Resource source;

		private Property linkingProperty;

		private Resource target;

		public LinkStatement(Resource source, Property linkingProperty,
				Resource target) {

			this.source = source;

			this.linkingProperty = linkingProperty;

			this.target = target;

		}

		public Statement toJenaStatement() {

			Statement statement = ResourceFactory.createStatement(this.source,
					this.linkingProperty, this.target);

			return statement;

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((linkingProperty == null) ? 0 : linkingProperty.getURI()
							.hashCode());
			result = prime * result
					+ ((source == null) ? 0 : source.getURI().hashCode());
			result = prime * result
					+ ((target == null) ? 0 : target.getURI().hashCode());

			return result;
		}

		/* equals */

		@Override
		public boolean equals(Object obj) {

			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LinkStatement other = (LinkStatement) obj;
			if (linkingProperty == null) {
				if (other.linkingProperty != null)
					return false;
			} else if (!linkingProperty.getURI().equals(
					other.linkingProperty.getURI()))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.getURI().equals(other.source.getURI()))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.getURI().equals(other.target.getURI()))
				return false;
			return true;
		}

	}

}
