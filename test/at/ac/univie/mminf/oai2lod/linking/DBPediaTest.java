package at.ac.univie.mminf.oai2lod.linking;

import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Soundex;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DBPediaTest {

	public DBPediaTest() {

	}

	public void testRetrieveRemoteData() throws Exception {

		String sparqlService = "http://DBpedia.org/sparql";

		LinkingJob job = new LinkingJob();

		String DBPediaClass = "http://dbpedia.org/class/yago/Capital108518505";

		String DBPropertery = RDFS.label.toString();

		Model result = job.retrieveRemoteData(sparqlService, DBPediaClass,
				DBPropertery, 5000, 0);

		result.write(System.out);

	}

	public void testLevenstheinDistance() {

		String s1 = "Wien";

		String s2 = "Wien";

		InterfaceStringMetric metric = new Levenshtein();
		
		System.out.println(metric.getSimilarity(s1, s2));

	}

	public static void main(String[] args) throws Exception {
		DBPediaTest test = new DBPediaTest();

		// test.testRetrieveRemoteData();

		test.testLevenstheinDistance();

	}

}
