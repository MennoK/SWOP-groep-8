package utility;

import java.util.List;

public class Utility {
	/**
	 * Transforms a list of Summarizable elements into a string with on each
	 * line an index and the summary of one of the elements. The index increase
	 * by one on each line and the elements are in the same order as the list.
	 * 
	 * @param elements
	 *            : the elements to be listed
	 * @param startIndex
	 *            : the index of the first element
	 * @return a string representing the list of summaries
	 */
	public static String listSummaries(List<? extends Summarizable> elements,
			int startIndex) {
		String str = "";
		for (int i = 0; i < elements.size(); i++) {
			str += (i + startIndex) + ": " + elements.get(i).toSummary() + "\n";
		}
		return str.trim();
	}
}
