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
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class RootServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		OAI2LODServer server = OAI2LODServer
				.fromServletContext(getServletContext());

		Map<String, String> classes = new TreeMap<String, String>();

		ExtendedIterator class_iter = OAI.m_model.listClasses();

		while (class_iter.hasNext()) {

			OntClass ontClass = (OntClass) class_iter.next();

			String name = ontClass.getLocalName();

			classes.put(name, server.baseURI() + "directory/" + name);

		}

		VelocityWrapper velocity = new VelocityWrapper(this, response);
		Context context = velocity.getContext();
		context.put("oai_repo", server.getConfig().getServer().getOaiServer().getServerURL());
		context.put("rdf_link", server.baseURI() + "all");
		context.put("classmap_links", classes);
		velocity.mergeTemplateXHTML("root_page.vm");
	}

	private static final long serialVersionUID = 8398973058486421941L;
}
