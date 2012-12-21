package at.ac.univie.mminf.oai2lod.linking;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

/* Tokenizes the strings to be compared and analyzes their similarity*/
public class NameSimilarity extends AbstractStringMetric {

	public NameSimilarity() {
		super();
	}

	@Override
	public String getLongDescriptionString() {
		return "Name Similarity Measure";
	}

	@Override
	public String getShortDescriptionString() {
		return "Name Similarity Measure";
	}

	@Override
	public float getSimilarity(String string1, String string2) {

		Set<String> string1Tokens = getTokens(string1);

		Set<String> string2Tokens = getTokens(string2);

		float string1Overlap = 0;
		for (String token : string1Tokens) {
			if (string2Tokens.contains(token)) {
				string1Overlap++;
			}
		}
		float string2Overlap = 0;
		for (String token : string2Tokens) {
			if (string1Tokens.contains(token)) {
				string2Overlap++;
			}
		}

		float similarity = (string1Overlap + string2Overlap)
				/ (float) (string1Tokens.size() + string2Tokens.size());

		// calculate overlap

		// TODO Auto-generated method stub
		return similarity;
	}

	private Set<String> getTokens(String string) {

		Set<String> tokens = new HashSet<String>();

		StringTokenizer tokenizer = new StringTokenizer(string);

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();

			token = removeSpecialCharacters(token);

			tokens.add(token);

		}

		return tokens;

	}

	private String removeSpecialCharacters(String string) {

		String[] specChar = new String[] { ".", ",", ";", "'", "#" };

		for (int i = 0; i < specChar.length; i++) {

			if (string.startsWith(specChar[i])) {
				string = string.substring(1);
			}
			if (string.endsWith(specChar[i])) {
				string = string.substring(0, string.length() - 1);
			}

		}

		return string;

	}

	@Override
	public String getSimilarityExplained(String arg0, String arg1) {

		return "no idea what to implement here";
	}

	@Override
	public float getSimilarityTimingEstimated(String arg0, String arg1) {

		return 1f;
	}

	@Override
	public float getUnNormalisedSimilarity(String arg0, String arg1) {
		return -1f;
	}

	public static void main(String[] args) {

		String string1 = "New York (NJ.)";
		String string2 = "New York";

		NameSimilarity sim = new NameSimilarity();

		System.out.println(sim.getSimilarity(string1, string2));

	}

}
