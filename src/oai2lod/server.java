package oai2lod;

import jena.cmdline.ArgDecl;
import jena.cmdline.CommandLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.ac.univie.mminf.oai2lod.OAI2LODJettyLauncher;
import at.ac.univie.mminf.oai2lod.config.OAI2LODConfigLoader;

import com.hp.hpl.jena.shared.JenaException;

public class server {

	private final static String usage = "usage: oai2lod-server [-p port] [-b serverBaseURI] configFileName";

	private static OAI2LODJettyLauncher server;

	private final static Log log = LogFactory.getLog(server.class);

	public static void main(String[] args) {
		CommandLine cmd = new CommandLine();
		cmd.setUsage(usage);
		ArgDecl portArg = new ArgDecl(true, "p", "port");
		cmd.add(portArg);
		ArgDecl baseURIArg = new ArgDecl(true, "b", "base");
		cmd.add(baseURIArg);
		cmd.process(args);

		if (cmd.numItems() == 0) {
			System.err.println(usage);
			System.exit(1);
		}
		if (cmd.numItems() > 2) {
			System.err.println("too many arguments");
			System.err.println(usage);
			System.exit(1);
		}
		server = new OAI2LODJettyLauncher();
		if (cmd.contains(portArg)) {
			setPort(Integer.parseInt(cmd.getArg(portArg).getValue()));
		}
		if (cmd.contains(baseURIArg)) {
			setServerBaseURI(cmd.getArg(baseURIArg).getValue());
		}
		String mappingFileName = cmd.getItem(0);
		setMappingFileName(mappingFileName);
		startServer();
	}

	public static void setPort(int port) {
		server.overridePort(port);
	}

	public static void setServerBaseURI(String baseURI) {
		server.overrideBaseURI(baseURI);
	}

	public static void setMappingFileName(String mappingFileName) {
		try {
			server.setConfigFile(OAI2LODConfigLoader
					.toAbsoluteURI(mappingFileName));
		} catch (JenaException ex) {
			Throwable t = ex;
			if (ex.getCause() != null) {
				t = ex.getCause();
			}
			System.err.println(mappingFileName + ": " + t.getMessage());
			System.exit(1);
		}
	}

	public static void startServer() {
		server.start();
		log.info("[[[ Server started at " + server.getHomeURI() + " ]]]");
	}

}
