package com.witcraft.jjobs.context;

import com.witcraft.jjobs.actions.Matching;
import java.math.BigDecimal;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 * @param <T>
 */
public abstract class NumberContext<T> extends CriteriaContext<T, BigDecimal> {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public NumberContext(Matching<T> matching, Node node) {
		super(matching, node);
	}

	/**
	 *
	 * @param context
	 *
	 * @return
	 */
	@Override
	public abstract BigDecimal getContextValue(T context);
}
