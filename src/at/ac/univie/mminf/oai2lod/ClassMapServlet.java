package at.ac.univie.mminf.oai2lod;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.ac.univie.mminf.oai2lod.vocab.OAI;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ClassMapServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getPathInfo() == null) {
			new ModelResponse(classMapListModel(), request, response).serve();
			return;
		}
		String classMapName = request.getPathInfo().substring(1);

		OntClass requestedClass = null;

		ExtendedIterator class_iter = OAI.m_model.listClasses();
		while (class_iter.hasNext()) {

			OntClass ontClass = (OntClass) class_iter.next();

			if (classMapName.equals(ontClass.getLocalName())) {
				requestedClass = ontClass;
			}

		}

		if (requestedClass == null) {
			response.sendError(404, "Sorry, class '" + classMapName
					+ "' not available.");
			return;
		}

		// the model to be returned
		Model resourceList = ModelFactory.createDefaultModel();

		OAI2LODServer server = OAI2LODServer
				.fromServletContext(getServletContext());

		// add available instances
		StmtIterator stmt_iter = server.getModel().listStatements(null,
				RDF.type, requestedClass);
		resourceList.add(stmt_iter);

		Resource classMap = resourceList.getResource(server.baseURI() + "all/"
				+ classMapName);
		Resource directory = resourceList.createResource(server.baseURI()
				+ "all");
		classMap.addProperty(RDFS.seeAlso, directory);
		classMap.addProperty(RDFS.label, "List of all instances: "
				+ classMapName);
		directory.addProperty(RDFS.label, "D2R Server contents");
		// server.addDocumentMetadata(resourceList, classMap);
		new ModelResponse(resourceList, request, response).serve();
	}

	private Model classMapListModel() {

		OAI2LODServer server = OAI2LODServer
				.fromServletContext(getServletContext());

		Model result = ModelFactory.createDefaultModel();
		result.setNsPrefixes(server.getModel());

		Resource list = result.createResource(server.baseURI() + "all");
		list.addProperty(RDFS.label, "OAI2LOD Server contents");

		ExtendedIterator class_iter = OAI.m_model.listClasses();
		while (class_iter.hasNext()) {

			OntClass ontClass = (OntClass) class_iter.next();

			String className = ontClass.getLocalName();

			Resource classMap = result.createResource(server.baseURI() + "all/"
					+ className);
			classMap.addProperty(RDFS.label, "List of all instances: "
					+ className);

			list.addProperty(RDFS.seeAlso, classMap);

		}

		// server.addDocumentMetadata(result, list);
		return result;
	}

	private static final long serialVersionUID = 6467361762380096163L;
}
