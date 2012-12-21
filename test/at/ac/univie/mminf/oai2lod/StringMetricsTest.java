package at.ac.univie.mminf.oai2lod;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class StringMetricsTest {

	public void computeSimilarity() {
		
		String string1 = "Speer, Albert";
		String string2 = "Hans Albers";
		
		
		
		//Jaro sim = new Jaro();
		
		Levenshtein sim = new Levenshtein();
		
		//JaroWinkler sim = new JaroWinkler();
		
		// MatchingCoefficient sim = new MatchingCoefficient();
		
		// DiceSimilarity sim = new DiceSimilarity();
		
		//OverlapCoefficient sim = new OverlapCoefficient();
		
		System.out.println(sim.getSimilarity(string1, string2));
		
	}
	
	
	public static void main(String[] args) {
		new StringMetricsTest().computeSimilarity();
	}
	
	
	
}
