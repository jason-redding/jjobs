package com.witcraft.jjobs.criteria;

import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.helpers.JSONResultSet;
import com.witcraft.jjobs.nodes.XmlElementWithContent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class CriteriaValue extends XmlElementWithContent {

	private static final Pattern PATTERN_PROPERTY = Pattern.compile("\\$\\{([a-z]([a-z._-])*)+\\}");

	/**
	 *
	 * @param node
	 *
	 * @return
	 */
	public static CriteriaValue newFromNode(Node node) {
		if (isCriteriaValue(node)) {
			return new CriteriaValue(node);
		}
		return null;
	}

	/**
	 *
	 * @param node
	 *
	 * @return
	 */
	public static boolean isCriteriaValue(Node node) {
		return (node != null && ("value".equals(node.getLocalName()) || ("value-using".equals(node.getLocalName()) && hasAttribute(node, "result-set"))));
	}

	private final String resultSetName;
	private JSONResultSet resultSet;

	private CriteriaValue(Node node) {
		super(node);
		switch (node.getLocalName()) {
		case "value-using":
			this.resultSetName = getAttribute("result-set");
			break;
		default:
			this.resultSetName = null;
			break;
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean isValueStatic() {
		return !isValueDynamic();
	}

	/**
	 *
	 * @return
	 */
	public boolean isValueDynamic() {
		return (resultSetName != null);
	}

	/**
	 *
	 * @return
	 */
	public boolean hasResultSet() {
		if (resultSet != null) {
			return true;
		}
		if (resultSetName != null) {
			JSONObject o = Main.getVariable(resultSetName, JSONObject.class);
			if (o != null && "result-set".equals(o.getString("type"))) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @return
	 */
	public JSONResultSet getResultSet() {
		if (resultSet != null) {
			return resultSet;
		}
		if (resultSetName != null) {
			return (resultSet = JSONResultSet.fromJSONObject(Main.getVariable(resultSetName, JSONObject.class)));
		}
		return null;
	}

	/**
	 *
	 * @return
	 */
	public String getResultSetName() {
		return resultSetName;
	}

	/**
	 *
	 * @param rs
	 *
	 * @return
	 */
	public final String using(JSONResultSet rs) {
		return rs.useFor(getContent());
	}

	/**
	 *
	 * @param rs
	 *
	 * @return
	 */
	public final String using(ResultSet rs) {
		Matcher m = PATTERN_PROPERTY.matcher(getContent());
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String pName = m.group(1);
			if (pName != null && pName.length() > 0) {
				Object pValue;
				try {
					pValue = rs.getObject(pName);
					if (pValue != null) {
						m.appendReplacement(sb, String.valueOf(pValue).replaceAll("(?<!\\\\)\\$(?!\\d)", "\\\\$0"));
						continue;
					}
				} catch (SQLException sex) {
					Main.err(sex);
				}
			}
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	@Override
	public String toString() {
		return getContent();
	}
}
