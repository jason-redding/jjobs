package com.witcraft.jjobs.criteria;

import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.context.CriteriaContext;
import com.witcraft.jjobs.helpers.JSONResultSet;

/**
 *
 * @author Jason Redding
 * @param <T>
 */
public interface Criterion<T> {

	/**
	 *
	 * @return
	 */
	public boolean isNegated();

	/**
	 *
	 * @param object
	 *
	 * @return
	 */
	public boolean matches(T object);

	/**
	 *
	 * @return
	 */
	public CriteriaContext getContext();

	/**
	 *
	 * @return
	 */
	public boolean hasResultSet();

	/**
	 *
	 * @return
	 */
	public JSONResultSet getResultSet();

	/**
	 *
	 * @param position
	 *
	 * @return
	 */
	public JSONResultSet getResultSet(int position);

	/**
	 *
	 * @return
	 */
	public String getContent();

	/**
	 *
	 * @return
	 */
	public boolean hasValue();

	/**
	 *
	 * @return
	 */
	public CriteriaValue getValue();

	/**
	 *
	 * @return
	 */
	public Matching getMatching();

	/**
	 *
	 * @return
	 */
	public String getFullName();

	/**
	 *
	 * @return
	 */
	public String valueToString();

	/**
	 *
	 * @param object
	 *
	 * @return
	 */
	public String expression(T object);
}
