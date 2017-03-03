package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class ValueStartsWith extends StringCriterion {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public ValueStartsWith(Matching<File> matching, Node node) {
		super(matching, node);
	}

	@Override
	protected boolean isMatch(String contextValue, String criterionValue) {
		return (contextValue.startsWith(criterionValue));
	}
}
