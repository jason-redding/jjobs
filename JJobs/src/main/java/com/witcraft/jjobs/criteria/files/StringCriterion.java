package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.context.CriteriaContext;
import com.witcraft.jjobs.criteria.AbstractCriterion;
import com.witcraft.jjobs.helpers.JSONResultSet;
import java.io.File;
import java.sql.SQLException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public abstract class StringCriterion extends AbstractCriterion<File> implements FileCriterion {

	private final boolean ignoreCase;

	/**
	 * Constructs a new RegexCriterion
	 *
	 * @param matching
	 * @param node
	 */
	protected StringCriterion(Matching<File> matching, Node node) {
		super(matching, node);
		boolean defaultIgnoreCase;
		switch (getContext().getName()) {
		default:
			defaultIgnoreCase = false;
			break;
		case "owner":
			defaultIgnoreCase = true;
			break;
		}
		ignoreCase = getBooleanAttribute("ignore-case", defaultIgnoreCase);
	}

	/**
	 *
	 * @return
	 */
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	@Override
	public String valueToString() {
		return getContent();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getFullName());
		NamedNodeMap attrs = getAttributes();
		if (attrs.getLength() > 0) {
			sb.append(" [");
			sb.append(attributesToString());
			sb.append("]");
		}
		sb.append(" \"");
		sb.append(valueToString());
		sb.append("\"");
		return sb.toString();
	}

	/**
	 *
	 * @param file
	 *
	 * @return
	 */
	@Override
	protected final boolean isMatch(File file) {
		CriteriaContext<File, String> c = getContext(String.class);
		String cValue = c.getContextValue(file);
		if (isValueDynamic()) {
			if (hasResultSet()) {
				try (JSONResultSet rs = getResultSet()) {
					boolean r = false;
//					rs.beforeFirst();
					while (rs.next()) {
						String v = rs.useFor(this);
						if (isIgnoreCase()) {
							cValue = cValue.toLowerCase();
							v = v.toLowerCase();
						}
						r = isMatch(cValue, v);
						if (r) {
							break;
						}
					}
					return r;
				} catch (SQLException sex) {
					Main.err(sex);
				}
			}
			return false;
		}
		return isMatch(cValue, getContent());
	}

	/**
	 *
	 * @param contextValue
	 * @param criterionValue
	 *
	 * @return
	 */
	protected abstract boolean isMatch(String contextValue, String criterionValue);

	@Override
	public final String expression(File file) {
		CriteriaContext<File, String> c = getContext(String.class);
		String cValue = c.getContextValue(file);
		String v;
		if (isValueDynamic()) {
			if (hasResultSet()) {
				try (JSONResultSet rs = getResultSet()) {
					StringBuilder reasons = new StringBuilder();
//					rs.beforeFirst();
					while (rs.next()) {
						v = rs.useFor(this);
						String oCValue = cValue;
						String oValue = v;
						if (isIgnoreCase()) {
							cValue = cValue.toLowerCase();
							v = v.toLowerCase();
						}
						if (isMatch(cValue, v)) {
							if (reasons.length() > 0) {
								reasons.append('\n');
							}
							reasons.append(expression(oCValue, oValue));
						}
					}
					return reasons.toString();
				} catch (SQLException sex) {
					Main.err(sex);
				}
			}
		}
		v = getContent();
		if (isIgnoreCase()) {
			cValue = cValue.toLowerCase();
			v = v.toLowerCase();
		}
		return expression(cValue, v);
	}

	/**
	 *
	 * @param contextValue
	 * @param criterionValue
	 *
	 * @return
	 */
	protected final String expression(String contextValue, String criterionValue) {
		boolean isMatched = isMatch(contextValue, criterionValue);
		StringBuilder sb = new StringBuilder();
		sb.append(isMatched ? '+' : '-');
		sb.append("[").append(getFullName()).append("] ")
		.append("\"")
		.append(criterionValue)
		.append("\"");
		if (isIgnoreCase()) {
			sb.append(" (ignore case)");
		}
		return sb.toString();
	}
}
