package at.ac.univie.mminf.oai2lod;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;

import at.ac.univie.mminf.oai2lod.vocab.OAI;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DirectoryServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getPathInfo() == null) {
			response.sendError(404);
			return;
		}

		String className = request.getPathInfo().substring(1);

		OntClass requestedClass = null;

		ExtendedIterator class_iter = OAI.m_model.listClasses();
		while (class_iter.hasNext()) {

			OntClass ontClass = (OntClass) class_iter.next();

			if (className.equals(ontClass.getLocalName())) {
				requestedClass = ontClass;
			}

		}

		if (requestedClass == null) {
			response.sendError(404, "Sorry, class '" + className
					+ "' not found.");
			return;
		}

		OAI2LODServer server = OAI2LODServer
				.fromServletContext(getServletContext());

		// adding the instances
		Model targetModel = server.getModel();
		StmtIterator stmt_iter = targetModel.listStatements(null, RDF.type,
				requestedClass);

		Map<String, String> resources = new TreeMap<String, String>();

		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.nextStatement();

			Resource subject = (Resource) stmt.getSubject();
			String uri = subject.getURI();

			Statement labelStmt = subject.getProperty(RDFS.label);
			String label = (labelStmt == null) ? subject.getURI() : labelStmt
					.getString();
			resources.put(uri, label);
		}

		Map<String, String> classMapLinks = new TreeMap<String, String>();

		class_iter = OAI.m_model.listClasses();

		while (class_iter.hasNext()) {

			OntClass ontClass = (OntClass) class_iter.next();

			String name = ontClass.getLocalName();

			classMapLinks.put(name, server.baseURI() + "directory/" + name);

		}

		VelocityWrapper velocity = new VelocityWrapper(this, response);
		Context context = velocity.getContext();
		context.put("rdf_link", server.baseURI() + "all/" + className);
		context.put("classmap", className);
		context.put("classmap_links", classMapLinks);
		context.put("resources", resources);
		velocity.mergeTemplateXHTML("directory_page.vm");
	}

	private static final long serialVersionUID = 8398973058486421941L;
}
