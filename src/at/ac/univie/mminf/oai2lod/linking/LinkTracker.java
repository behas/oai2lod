package at.ac.univie.mminf.oai2lod.linking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class LinkTracker {

	/* Vocabulary for serialization */

	public static OntModel m_model = ModelFactory.createOntologyModel(
			OntModelSpec.RDFS_MEM, null);

	public static final String NS = "http://www.mediaspaces.info/eval#";

	public static final OntClass Link = m_model.createClass(NS + "Link");

	public static final OntProperty linkNumber = m_model.createOntProperty(NS
			+ "linkNumber");

	public static final OntProperty sourceRes = m_model.createOntProperty(NS
			+ "sourceRes");

	public static final OntProperty sourceVal = m_model.createOntProperty(NS
			+ "sourceVal");

	public static final OntProperty property = m_model.createOntProperty(NS
			+ "property");

	public static final OntProperty targetRes = m_model.createOntProperty(NS
			+ "targetRes");

	public static final OntProperty targetVal = m_model.createOntProperty(NS
			+ "targetVal");

	public static final OntProperty similarity = m_model.createOntProperty(NS
			+ "similarity");

	public static final OntProperty totalLinks = m_model.createOntProperty(NS
			+ "totalLinks");

	/* output file */

	public static String rdfOutputFile = "./generatedLinks.n3";

	public static String htmlOutputFile = "./generatedLinks.html";

	/* Array holding all Links */

	private List<Link> links = new ArrayList<Link>();

	private Set<Integer> used_randoms = new HashSet<Integer>();

	private Random generator = new Random(19580427);

	/* Set of already used random positions */

	public LinkTracker() {

	}

	public void addLink(Link link) {

		this.links.add(link);

	}

	public int generateRandomPosition(int max) {

		int randomIndex = generator.nextInt(max);

		return randomIndex;

	}

	public void toRDF(OutputStream out, int amount) {

		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("eval", NS);

		Resource general = model.createResource();
		general.addLiteral(totalLinks, new Integer(links.size()));

		if (links.size() < amount) {
			amount = links.size();
		}

		int counter = 0;

		while (counter < amount) {

			int randomPos = generateRandomPosition(links.size());

			if (!used_randoms.contains(new Integer(randomPos))) {

				System.out.println("Saving link: " + randomPos);

				Link link = links.get(randomPos);

				Resource link_res = model.createResource("link:" + randomPos);
				link_res.addProperty(RDF.type, Link);
				link_res.addProperty(sourceRes, link.getSource());
				link_res.addProperty(sourceVal, link.getSourceValue());
				link_res.addProperty(targetRes, link.getTarget());
				link_res.addProperty(targetVal, link.getTargetValue());
				link_res.addLiteral(similarity, link.getSimilarity());

				used_randoms.add(new Integer(randomPos));

				counter++;

			}

		}

		model.write(out, "N3");

	}

	public void toHTML(Writer writer, int amount) {

		try {

			System.out.println("Total number of links: " + links.size());

			BufferedWriter out = new BufferedWriter(writer);

			out.write("<HTML>\n<BODY>");
			out.write("<p>Total Number of Links: " + links.size() + "</p>\n");

			out.write("<TABLE>\n");
			out
					.write("<TR><TH>No.</TH><TH>Link Number</TH><TH>Source Resource</TH><TH>Source Value</TH><TH>Target Resource</TH><TH>Target Value</TH><TH>Similarity</TH></TR>\n");

			if (links.size() < amount) {
				amount = links.size();
			}

			int counter = 0;

			while (counter < amount) {

				int randomPos = generateRandomPosition(links.size());

				if (!used_randoms.contains(new Integer(randomPos))) {

					// System.out.println("Saving link: " + randomPos);

					Link link = links.get(randomPos);

					out.write("<TR>");
					out.write("<TD>" + counter + "</TD");
					out.write("<TD>" + randomPos + "</TD");
					out.write("<TD><a href=\"" + link.getSource() + "\"/>"
							+ link.getSource() + "</TD");
					out.write("<TD>" + link.getSourceValue() + "</TD");
					out.write("<TD><a href=\"" + link.getTarget() + "\"/>"
							+ link.getTarget() + "</TD");
					out.write("<TD>" + link.getTargetValue() + "</TD");
					out.write("<TD>" + link.getSimilarity() + "</TD");
					out.write("</TR>\n");

					used_randoms.add(new Integer(randomPos));

					counter++;

				}

			}

			out.write("</TABLE>");

			out.write("</BODY></HTML>");

			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void fromRDF(InputStream in) {

		this.links = new ArrayList<Link>();

		Model model = ModelFactory.createDefaultModel();

		model.read(in, "N3");

		ResIterator res_iter = model.listSubjectsWithProperty(RDF.type, Link);
		while (res_iter.hasNext()) {

			Resource link_res = res_iter.nextResource();

			Resource source = (Resource) link_res.getProperty(
					LinkTracker.sourceRes).getObject().as(Resource.class);

			String sourceVal = link_res.getProperty(LinkTracker.sourceVal)
					.getString();

			Property property = (Property) link_res.getProperty(
					LinkTracker.property).getObject().as(Property.class);

			Resource target = (Resource) link_res.getProperty(
					LinkTracker.targetRes).getObject().as(Resource.class);

			String targetVal = link_res.getProperty(LinkTracker.targetVal)
					.getString();

			float similarity = new Float(link_res.getProperty(
					LinkTracker.sourceVal).getString()).floatValue();

			Link link = new Link(source, property, target, sourceVal,
					targetVal, similarity);

			this.links.add(link);

		}

	}

	public void saveRandomResults(int number) {

		try {
			// toRDF(new FileOutputStream(rdfOutputFile), number);

			toHTML(new FileWriter(htmlOutputFile), number);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/* internal classes */

	public static class Link {

		private Resource source;

		private Property property;

		private Resource target;

		private String sourceValue;

		private String targetValue;

		private float similarity;

		public Link(Resource source, Property property, Resource target,
				String sourceValue, String targetValue, float similiarity) {

			this.source = source;
			this.property = property;
			this.target = target;
			this.sourceValue = sourceValue;
			this.targetValue = targetValue;
			this.similarity = similiarity;

		}

		public Resource getSource() {
			return source;
		}

		public void setSource(Resource source) {
			this.source = source;
		}

		public Property getProperty() {
			return property;
		}

		public void setProperty(Property property) {
			this.property = property;
		}

		public Resource getTarget() {
			return target;
		}

		public void setTarget(Resource target) {
			this.target = target;
		}

		public String getSourceValue() {
			return sourceValue;
		}

		public void setSourceValue(String sourceValue) {
			this.sourceValue = sourceValue;
		}

		public String getTargetValue() {
			return targetValue;
		}

		public void setTargetValue(String targetValue) {
			this.targetValue = targetValue;
		}

		public float getSimilarity() {
			return similarity;
		}

		public void setSimilarity(float similarity) {
			this.similarity = similarity;
		}

	}

	public static void main(String[] args) {

		LinkTracker tracker = new LinkTracker();

		for (int i = 0; i < 100; i++) {

			System.out.println(tracker.generateRandomPosition(100));

		}

	}

}
