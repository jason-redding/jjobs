package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.context.CriteriaContext;
import com.witcraft.jjobs.criteria.AbstractCriterion;
import com.witcraft.jjobs.helpers.JSONResultSet;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public abstract class RegexCriterion extends AbstractCriterion<File> implements FileCriterion {

	private String patternText;
	private Pattern pattern = null;
	private final LinkedHashMap<String, Boolean> flags;
	private Matcher namePatternMatcher;

	/**
	 * Constructs a new RegexCriterion
	 *
	 * @param matching
	 * @param node
	 */
	protected RegexCriterion(Matching<File> matching, Node node) {
		super(matching, node);
		flags = new LinkedHashMap<>();
		String inheritedFlags = getAttribute(node.getParentNode(), "flags", "").replaceAll("[^idmsux-]", "");
		int negateIndex = inheritedFlags.indexOf('-');
		for (int i = 0; i < inheritedFlags.length(); i++) {
			if (i == negateIndex) {
				continue;
			}
			setFlag(inheritedFlags.substring(i, i + 1), (negateIndex < 0 || i < negateIndex));
		}
	}

	/**
	 * Sets the specified flag to the specified state in this criterion.
	 *
	 * @param flag
	 * @param state
	 */
	protected final void setFlag(String flag, boolean state) {
		if (flag != null) {
			flags.put(flag, state);
		}
	}

	/**
	 *
	 * @param flag
	 *
	 * @return
	 */
	public final Boolean getFlag(String flag) {
		return flags.getOrDefault(flag, null);
	}

	/**
	 *
	 * @param pattern
	 */
	protected final void setPatternText(String pattern) {
		this.patternText = pattern;
	}

	/**
	 *
	 * @return
	 */
	public final String getPatternText() {
		return this.patternText;
	}

	/**
	 *
	 * @param file
	 *
	 * @return
	 */
	@Override
	protected boolean isMatch(File file) {
		CriteriaContext<File, String> c = getContext(String.class);
		String cValue = c.getContextValue(file);
		if (isValueDynamic()) {
			if (hasResultSet()) {
				try (JSONResultSet rs = getResultSet()) {
					boolean r = false;
//					rs.beforeFirst();
					while (rs.next()) {
						patternText = rs.useFor(this);
						pattern = Pattern.compile(getFlagsText(true) + patternText);
						namePatternMatcher = pattern.matcher(cValue);
						r = namePatternMatcher.find();
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
		if (namePatternMatcher == null) {
			if (pattern == null) {
				pattern = Pattern.compile(getFlagsText(true) + patternText);
			}
			namePatternMatcher = pattern.matcher(cValue);
		} else {
			namePatternMatcher.reset(cValue);
		}
		return namePatternMatcher.find();
	}

	@Override
	public String expression(File file) {
		StringBuilder sb = new StringBuilder();
		boolean isMatched = isMatch(file);
		CriteriaContext<File, String> c = getContext(String.class);
		String cValue = c.getContextValue(file);
		sb.append(isMatched ? '+' : '-');
		sb.append('[').append(getFullName()).append("] ");
		sb.append('"').append(cValue).append('"');
		if (isMatched) {
			sb.append(" matches");
		} else {
			sb.append(" does not match");
		}
		//sb.append(' ');
		//sb.append('"').append(pattern.pattern()).append('"');
		return sb.toString();
	}

	/**
	 *
	 * @return
	 */
	public HashMap<String, Boolean> getFlags() {
		return flags;
	}

	/**
	 *
	 * @return
	 */
	public final boolean hasFlags() {
		return !getFlags().isEmpty();
	}

	/**
	 *
	 * @return
	 */
	public final String getFlagsText() {
		return getFlagsText(false);
	}

	/**
	 *
	 * @param regexStyle
	 *
	 * @return
	 */
	public final String getFlagsText(boolean regexStyle) {
		while (hasFlags()) {
			StringBuilder flagsBuilder = new StringBuilder("-");
			Set<Map.Entry<String, Boolean>> flagSet = flags.entrySet();
			for (Map.Entry<String, Boolean> f : flagSet) {
				String flag = f.getKey();
				if (f.getValue()) {
					flagsBuilder.insert(0, flag);
				} else {
					flagsBuilder.append(flag);
				}
			}
			if (flagsBuilder.length() > 1) {
				if (flagsBuilder.charAt(flagsBuilder.length() - 1) == '-') {
					flagsBuilder.deleteCharAt(flagsBuilder.length() - 1);
				}
				if (regexStyle) {
					flagsBuilder.insert(0, "(?").append(")");
				}
				return flagsBuilder.toString();
			}
			break;
		}
		return "";
	}

	@Override
	public String valueToString() {
		return getPatternText();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getFullName());
		boolean hasFlags = hasFlags();
		boolean isNegated = isNegated();
		if (hasFlags || isNegated) {
			sb.append(" [");
			if (hasFlags) {
				sb.append("flags=\"").append(getFlagsText()).append("\"");
			}
			if (isNegated) {
				if (hasFlags) {
					sb.append(", ");
				}
				sb.append("negated=\"true\"");
			}
			sb.append("]");
		}
		sb.append(" \"");
		sb.append(valueToString());
		sb.append("\"");
		return sb.toString();
	}
}
