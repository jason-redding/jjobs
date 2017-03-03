package com.witcraft.jjobs.actions.db;

import com.witcraft.jjobs.Job;
import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.actions.JobAction;
import com.witcraft.jjobs.criteria.AbstractCriterion;
import com.witcraft.jjobs.helpers.JXPathBuilder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jason Redding
 */
public class JdbcAction extends JobAction {

	private final Connection connection;

	/**
	 *
	 * @param job
	 * @param node
	 *
	 * @throws java.sql.SQLException
	 */
	public JdbcAction(Job job, Node node) throws SQLException {
		super(job, node);
		connection = DriverManager.getConnection(getAttribute("connect"), getAttribute("username"), getAttribute("password"));
	}

	@Override
	public void run() {
		XPath xpath = getXPath();
		JXPathBuilder xp = new JXPathBuilder(xpath.getNamespaceContext());
		xp
		.elementLocalName("script").step().elementLocalName("sql")
		.union()
		.elementLocalName("procedure");
		try {
			NodeList commands = (NodeList)xpath.evaluate(xp.toString(), getNode(), XPathConstants.NODESET);
			for (int i = 0; i < commands.getLength(); i++) {
				Node command = commands.item(i);
				String fullName = AbstractCriterion.getNameUntil(command, getNode());
				Main.log("    %s [%s]", fullName.replaceAll("/+", "-"), attributesToString(command.getAttributes()));
				switch (fullName) {
				case "script/sql":
					String varName = getAttribute(command, "var");
					String sql = command.getTextContent().trim();
					Main.log("      > %s", sql.replaceAll("\n", "\n      > "));
					Statement stmt = connection.createStatement();
					stmt.execute(sql);
					ResultSet rs = stmt.getResultSet();
					if (rs != null) {
						if (varName != null) {
							JSONObject jrs = resultSetToJson(rs);
							jrs.put("var-name", varName);
							Main.setVariable(varName, jrs);
						}
					} else {
						Main.err("No ResultSet was returned.");
					}
					try {
						stmt.close();
					} catch (Exception ex) {
					}
					break;
				case "procedure":
					Main.log("");
					break;
				default:
					break;
				}
			}
		} catch (XPathExpressionException | SQLException ex) {
			Main.err(ex);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			connection.close();
		} catch (Exception ex) {
			Main.err(ex);
		}
		super.finalize();
	}

	/**
	 *
	 * @param rs
	 *
	 * @return
	 */
	public static final JSONObject resultSetToJson(ResultSet rs) {
		if (rs != null) {
			boolean isLogged = false;
			try {
				ResultSetMetaData rsMeta = rs.getMetaData();
				JSONObject r = new JSONObject();
				r.put("type", "result-set");
				JSONObject meta = new JSONObject();
				r.put("meta", meta);
				meta.put("column-count", rsMeta.getColumnCount());
				for (int ci = 1; ci <= rsMeta.getColumnCount(); ci++) {
					meta.append("column-names", rsMeta.getColumnLabel(ci));
					meta.append("column-class-names", rsMeta.getColumnClassName(ci));
					meta.append("column-types", rsMeta.getColumnType(ci));
					meta.append("column-type-names", rsMeta.getColumnTypeName(ci));
				}
				StringBuilder out = new StringBuilder();
				if (isLogged) {
					out.append("      ------------------------------------------------------------------------\n");
				}
				int errorCount = 0;
				long startTime = System.currentTimeMillis();
				while (rs.next()) {
					try {
						JSONArray row = new JSONArray();
						r.append("rows", row);
						for (int ci = 1; ci <= rsMeta.getColumnCount(); ci++) {
							String className = rsMeta.getColumnClassName(ci);
							Object oValue = rs.getObject(ci, Class.forName(className));
							row.put(oValue);
							if (isLogged) {
								String cLabel = rsMeta.getColumnLabel(ci);
								String cValue = String.valueOf(oValue);
								if (ci > 1) {
									out.append('\n');
								}
								out
								.append("      | ")
								.append(cLabel).append("=\"").append(cValue).append("\"");
							}
						}
						if (isLogged) {
							out.append("\n      ------------------------------------------------------------------------");
						}
					} catch (ClassNotFoundException ex) {
						Main.err(ex);
						errorCount++;
						if (errorCount > 20) {
							if ((System.currentTimeMillis() - startTime) <= (500)) {
								break;
							}
							startTime = System.currentTimeMillis();
							errorCount = 0;
						}
					}
				}
				return r;
			} catch (SQLException ex) {
				Main.err(ex);
			}
		}
		return null;
	}

	@Override
	public String attributesToString(String exception, String... exceptions) {
		return super.attributesToString("password", exceptions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getType())
		.append(" [")
		.append(attributesToString())
		.append("]");
		return sb.toString();
	}
}
