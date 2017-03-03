package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class ValueEquals extends StringCriterion {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public ValueEquals(Matching<File> matching, Node node) {
		super(matching, node);
	}

	@Override
	protected boolean isMatch(String contextValue, String criterionValue) {
		boolean r;
		if (isIgnoreCase()) {
			r = criterionValue.equalsIgnoreCase(contextValue);
		} else {
			r = criterionValue.equals(contextValue);
		}
		return r;
	}
}
