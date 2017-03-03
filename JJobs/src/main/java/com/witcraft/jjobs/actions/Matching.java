package com.witcraft.jjobs.actions;

import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.criteria.Criterion;
import com.witcraft.jjobs.nodes.XmlElement;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 * @param <T> type of object each
 * {@link com.witcraft.jjobs.criteria.AbstractCriterion} in this matching group
 * will compare.
 */
public abstract class Matching<T> extends XmlElement {

	private final JobAction jobAction;
	private final List<Criterion<T>> criteria;

	/**
	 *
	 * @param jobAction
	 * @param node
	 */
	public Matching(JobAction jobAction, Node node) {
		super(node);
		this.jobAction = jobAction;
		criteria = new ArrayList<>();
	}

	/**
	 *
	 * @return
	 */
	public boolean hasCriteria() {
		return getCriteriaCount() > 0;
	}

	/**
	 *
	 * @param object
	 *
	 * @return
	 */
	public boolean matches(T object) {
		boolean matchCriterion;
		StringBuilder reasons = new StringBuilder();
		reasons.append("Evaluating \"").append(object).append("\"");
		for (Criterion<T> mc : getCriteria()) {
			matchCriterion = mc.matches(object);
//			if (mc.isNegated()) {
//				matchCriterion = !matchCriterion;
//			}
			if (reasons.length() > 0) {
				reasons.append('\n');
			}
			reasons
			//.append("  ")
			//.append(matchCriterion ? '+' : '-')
			.append(mc.expression(object));
			if (!matchCriterion) {
				Main.err(reasons.toString().replaceAll("(\n|^)(\\+|-)(?=\\[)", "$1  $2 "));
				return false;
			}
		}
		Main.log(reasons.toString().replaceAll("(\n|^)(\\+|-)(?=\\[)", "$1  $2 "));
		return true;
	}

	/**
	 *
	 * @return
	 */
	public int getCriteriaCount() {
		return criteria.size();
	}

	/**
	 *
	 * @return
	 */
	public List<Criterion<T>> getCriteria() {
		return criteria;
	}

	/**
	 *
	 * @param criterion
	 */
	public void addCriterion(Criterion<T> criterion) {
		if (criterion != null) {
			criteria.add(criterion);
		}
	}

	/**
	 * Returns the reference to the {@link com.witcraft.jjobs.actions.JobAction}
	 * that contains this matching group.
	 *
	 * @return the reference to the {@link com.witcraft.jjobs.actions.JobAction}
	 * that contains this matching group.
	 */
	public JobAction getJobAction() {
		return jobAction;
	}

	/**
	 * Clears the list of criteria for this {@link Matching}. The criteria list
	 * will be empty after this method returns.
	 */
	public void clear() {
		criteria.clear();
	}
}
