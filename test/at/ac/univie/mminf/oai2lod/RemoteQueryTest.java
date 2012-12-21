package at.ac.univie.mminf.oai2lod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import at.ac.univie.mminf.oai2lod.config.OAI2LODConfigLoader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

public class RemoteQueryTest {

	/*
	 * Note: listener in web.xml must be set to
	 * "at.ac.univie.mminf.oai2lod.TestWebappInitListener"
	 */

	private String queryFile1 = "./testData/mapping/testConstruct1.q";

	private String queryFile2 = "./testData/mapping/testConstruct2.q";

	private String queryFile3 = "./testData/mapping/testConstruct3.q";

	private String sparqlEndpoint = "http://localhost:2020/sparql";

	@Test
	public void testConstructQueries() throws Exception {

		// set up a server instance and load test data

		String configFileName = "./test/test-config.n3";

		OAI2LODJettyLauncher server = new OAI2LODJettyLauncher();

		server.setConfigFile(OAI2LODConfigLoader.toAbsoluteURI(configFileName));

		server.start();

		executeSelectQueryAndPrintResult(sparqlEndpoint, queryFile1);

		executeSelectQueryAndPrintResult(sparqlEndpoint, queryFile2);

		executeSelectQueryAndPrintResult(sparqlEndpoint, queryFile3);

	}

	private void executeSelectQueryAndPrintResult(String sparqlEndpoint,
			String queryFile) throws IOException {

		Query constructQuery = createQueryFromFile(queryFile);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				sparqlEndpoint, constructQuery);

		Model result = qexec.execConstruct();

		qexec.close();

		result.write(System.out, "N3");

	}

	private Query createQueryFromFile(String filePath) throws IOException {

		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();

		return QueryFactory.create(fileData.toString());

	}

}
