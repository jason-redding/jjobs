package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class ValuePattern extends RegexCriterion {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public ValuePattern(Matching<File> matching, Node node) {
		super(matching, node);
		Boolean attrIgnoreCase = getBooleanAttribute("ignore-case");
		String flagsText = getAttribute("flags", "").replaceAll("[^idmsux-]", "");
		if (attrIgnoreCase != null) {
			setFlag("i", attrIgnoreCase);
		}
		int negateIndex = flagsText.indexOf('-');
		for (int i = 0; i < flagsText.length(); i++) {
			if (flagsText.charAt(i) == '-') {
				continue;
			}
			setFlag(flagsText.substring(i, i + 1), (negateIndex < 0 || i < negateIndex));
		}
		setPatternText(getContent());
	}
}
