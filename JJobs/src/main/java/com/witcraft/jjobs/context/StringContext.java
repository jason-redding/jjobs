package com.witcraft.jjobs.context;

import com.witcraft.jjobs.actions.Matching;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 * @param <T>
 */
public abstract class StringContext<T> extends CriteriaContext<T, String> {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public StringContext(Matching<T> matching, Node node) {
		super(matching, node);
	}

	/**
	 *
	 * @param context
	 *
	 * @return
	 */
	@Override
	public abstract String getContextValue(T context);
}
