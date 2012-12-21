package at.ac.univie.mminf.oai2lod;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import at.ac.univie.mminf.oai2lod.config.OAI2LODConfigLoader;
import at.ac.univie.mminf.oai2lod.oai.OAIControllerMock;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

/**
 * Initialize D2R server on startup of an appserver such as Tomcat. This
 * listener should be included in the web.xml. This is compatible with Servlet
 * 2.3 spec compliant appservers.
 * 
 * @version $Id: WebappInitListener.java,v 1.5 2007/11/14 17:05:52 cyganiak Exp
 *          $
 * @author Inigo Surguy
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class OAIMockWebappInitListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		OAI2LODServer server = new OAI2LODServer();
		String configFile = context.getInitParameter("overrideConfigFile");
		if (configFile == null) {
			if (context.getInitParameter("configFile") == null) {
				throw new RuntimeException(
						"No configFile configured in web.xml");
			}
			configFile = absolutize(context.getInitParameter("configFile"),
					context);
		}
		if (context.getInitParameter("port") != null) {
			server.overridePort(Integer.parseInt(context
					.getInitParameter("port")));
		}
		if (context.getInitParameter("baseURI") != null) {
			server.overrideBaseURI(context.getInitParameter("baseURI"));
		}

		server.setConfigFile(configFile);

		/* start - for testing purposes only */

		OAIControllerMock mock1 = new OAIControllerMock();

		mock1.setListRecordResponses(TestDataSets.ONB_DEMO_ALL);

		mock1.setListSetResponse(TestDataSets.ONB_LISTSETS);

		server.setOAIController(mock1);

		/* end - for testing purposes only */

		server.start();

		server.putIntoServletContext(context);

		VelocityWrapper.initEngine(server, context);
	}

	public void contextDestroyed(ServletContextEvent event) {
		// Do nothing
	}

	private String absolutize(String fileName, ServletContext context) {
		if (!fileName.matches("[a-zA-Z0-9]+:.*")) {
			fileName = context.getRealPath("WEB-INF/" + fileName);
		}
		return OAI2LODConfigLoader.toAbsoluteURI(fileName);
	}

	/* loads sample data from a file */
	protected Model loadInstanceData(String dataFile) {

		FileManager fileManager = new FileManager();
		fileManager.addLocatorClassLoader(this.getClass().getClassLoader());
		fileManager.addLocatorFile();

		Model inputModel = fileManager.loadModel(dataFile);

		return inputModel;

	}

}
