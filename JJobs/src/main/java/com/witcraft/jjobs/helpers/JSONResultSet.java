package com.witcraft.jjobs.helpers;

import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.criteria.Criterion;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Jason Redding
 */
public class JSONResultSet implements ResultSet {

	private static final Pattern PATTERN_PROPERTY = Pattern.compile("\\$\\{((([a-z]([a-z_-])*)+):)?([a-z]([a-z._-])*)+\\}");
	private static final Calendar calendar;
	private int currentRowIndex;
	private List currentRow;
	private HashMap<String, Integer> columnIndexes;
	private List<String> columnNames;
	private List<String> columnClassNames;
	private List<Integer> columnTypes;
	private List<String> columnTypeNames;
	private final List<Map<String, Object>> rows;
	private int fetchDirection;
	private boolean lastColumnWasNull;
	private ResultSetMetaData resultSetMetaData;

	static {
		calendar = Calendar.getInstance();
	}

	public static final JSONResultSet fromJSONObject(JSONObject o) {
		if (o != null && "result-set".equals(o.getString("type"))) {
			return new JSONResultSet(o);
		}
		return null;
	}

	private JSONResultSet(JSONObject o) {
		this.currentRowIndex = -1;
		this.currentRow = null;
		this.rows = new ArrayList<>();
		this.columnIndexes = new HashMap<>();
		this.columnNames = new ArrayList<>();
		this.columnClassNames = new ArrayList<>();
		this.columnTypes = new ArrayList<>();
		this.columnTypeNames = new ArrayList<>();
		this.fetchDirection = ResultSet.FETCH_FORWARD;

		JSONObject meta = o.getJSONObject("meta");
		JSONArray cNames = meta.getJSONArray("column-names");
		JSONArray cClassNames = meta.getJSONArray("column-class-names");
		JSONArray cTypes = meta.getJSONArray("column-types");
		JSONArray cTypeNames = meta.getJSONArray("column-type-names");
		for (int i = 0; i < cNames.length(); i++) {
			String cName = cNames.getString(i).toLowerCase();
			this.columnNames.add(cName);
			this.columnIndexes.put(cName, i);
			this.columnClassNames.add(cClassNames.getString(i));
			this.columnTypes.add(cTypes.getInt(i));
			this.columnTypeNames.add(cTypeNames.getString(i));
		}
		JSONArray importedRows = o.getJSONArray("rows");
		JSONArray importedRow;
		Map<String, Object> columns;
		for (int ri = 0; ri < importedRows.length(); ri++) {
			columns = new HashMap<>();
			importedRow = importedRows.getJSONArray(ri);
			for (int ci = 0; ci < cNames.length(); ci++) {
				columns.put(cNames.getString(ci).toLowerCase(), importedRow.get(ci));
			}
			this.rows.add(columns);
		}

		this.resultSetMetaData = new ResultSetMetaData() {
			@Override
			public int getColumnCount() throws SQLException {
				return columnIndexes.size();
			}

			@Override
			public boolean isAutoIncrement(int column) throws SQLException {
				return false;
			}

			@Override
			public boolean isCaseSensitive(int column) throws SQLException {
				return false;
			}

			@Override
			public boolean isSearchable(int column) throws SQLException {
				return true;
			}

			@Override
			public boolean isCurrency(int column) throws SQLException {
				return false;
			}

			@Override
			public int isNullable(int column) throws SQLException {
				return ResultSetMetaData.columnNullableUnknown;
			}

			@Override
			public boolean isSigned(int column) throws SQLException {
				return false;
			}

			@Override
			public int getColumnDisplaySize(int column) throws SQLException {
				return 255;
			}

			@Override
			public String getColumnLabel(int column) throws SQLException {
				return columnNames.get(column - 1);
			}

			@Override
			public String getColumnName(int column) throws SQLException {
				return columnNames.get(column - 1).toLowerCase();
			}

			@Override
			public String getSchemaName(int column) throws SQLException {
				return null;
			}

			@Override
			public int getPrecision(int column) throws SQLException {
				return 0;
			}

			@Override
			public int getScale(int column) throws SQLException {
				return 0;
			}

			@Override
			public String getTableName(int column) throws SQLException {
				return "";
			}

			@Override
			public String getCatalogName(int column) throws SQLException {
				return "";
			}

			@Override
			public int getColumnType(int column) throws SQLException {
				return columnTypes.get(column - 1);
			}

			@Override
			public String getColumnTypeName(int column) throws SQLException {
				return columnTypeNames.get(column - 1);
			}

			@Override
			public boolean isReadOnly(int column) throws SQLException {
				return true;
			}

			@Override
			public boolean isWritable(int column) throws SQLException {
				return false;
			}

			@Override
			public boolean isDefinitelyWritable(int column) throws SQLException {
				return false;
			}

			@Override
			public String getColumnClassName(int column) throws SQLException {
				return columnClassNames.get(column - 1);
			}

			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(currentRowIndex)
		.append(": {");
		if (currentRow != null) {
			sb.append(currentRow.toString());
		}
		sb.append('}');
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private void refreshCurrentRow() {
		if (currentRowIndex < 0 || currentRowIndex >= rows.size()) {
			currentRow = null;
			return;
		}
		Map<String, Object> columns = rows.get(currentRowIndex);
		int oldColumnCount;
		if (currentRow == null) {
			currentRow = new ArrayList();
			oldColumnCount = 0;
		} else {
			oldColumnCount = currentRow.size();
		}
		String[] keys = columns.keySet().toArray(new String[0]);
		int upperLimit = Math.max(columns.size(), oldColumnCount);
		for (int i = 0; i < upperLimit; i++) {
			if (i < keys.length) {
				String key = keys[i];
				Object v = columns.get(key);
				if (i < oldColumnCount) {
					currentRow.set(i, v);
				} else {
					currentRow.add(v);
				}
			} else if (currentRow.size() > keys.length) {
				currentRow.remove(keys.length);
			}
		}
	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		int ci = (columnIndexes.getOrDefault(columnLabel, -1) + 1);
		if (ci < 1 || ci > columnIndexes.size()) {
			throw new SQLException("Column not found: \"" + columnLabel + "\"", new ArrayIndexOutOfBoundsException(ci - 1));
		}
		return ci;
	}

	/**
	 *
	 * @param criterion
	 *
	 * @return
	 */
	public String useFor(Criterion criterion) {
		return useFor(criterion.getContent());
	}

	/**
	 *
	 * @param value
	 *
	 * @return
	 */
	public String useFor(CharSequence value) {
		Matcher m = PATTERN_PROPERTY.matcher(value);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String pType = m.group(2);
			String pName = m.group(5);
			if (pName != null && pName.length() > 0) {
				Object pValue;
				try {
					pValue = this.getObject(pName);
					if (pValue != null) {
						String nValue = String.valueOf(pValue).replaceAll("(?<!\\\\)\\$(?!\\d)", "\\\\$0");
						if (pType != null) {
							switch (pType) {
							case "file":
								File pFile = new File(nValue);
								nValue = pFile.toPath().toAbsolutePath().toString();
								break;
							}
						}
						m.appendReplacement(sb, nValue);
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
	public boolean next() throws SQLException {
		return relative(1);
	}

	@Override
	public void close() throws SQLException {

	}

	@Override
	public boolean wasNull() throws SQLException {
		return lastColumnWasNull;
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		return (r == null ? null : String.valueOf(r));
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		if (r != null) {
			switch (String.valueOf(r).toLowerCase()) {
			case "true":
			case "yes":
			case "on":
			case "1":
				return true;
			}
		}
		return false;
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		return Byte.parseByte(r.toString());
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		return Short.parseShort(String.valueOf(r));
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		return Integer.parseInt(String.valueOf(r));
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		return Long.parseLong(String.valueOf(r));
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		return Float.parseFloat(String.valueOf(r));
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		return Double.parseDouble(String.valueOf(r));
	}

	@Override
	@SuppressWarnings("deprecation")
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return getBigDecimal(columnIndex);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		return (lastColumnWasNull ? null : String.valueOf(r).getBytes());
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		return getDate(columnIndex, calendar);
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		return getTime(columnIndex, calendar);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return getTimestamp(columnIndex, calendar);
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return getBinaryStream(columnIndex);
	}

	@Override
	@SuppressWarnings("deprecation")
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		return new ByteArrayInputStream((lastColumnWasNull ? new byte[0] : String.valueOf(r).getBytes()));
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return getString(findColumn(columnLabel));
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		return getBoolean(findColumn(columnLabel));
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		return getByte(findColumn(columnLabel));
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		return getShort(findColumn(columnLabel));
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		return getInt(findColumn(columnLabel));
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		return getLong(findColumn(columnLabel));
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return getFloat(findColumn(columnLabel));
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return getDouble(findColumn(columnLabel));
	}

	@Override
	@SuppressWarnings("deprecation")
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		return getBigDecimal(findColumn(columnLabel));
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		return getBytes(findColumn(columnLabel));
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return getDate(findColumn(columnLabel));
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return getTime(findColumn(columnLabel));
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return getTimestamp(findColumn(columnLabel));
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return getAsciiStream(findColumn(columnLabel));
	}

	@Override
	@SuppressWarnings("deprecation")
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return getUnicodeStream(findColumn(columnLabel));
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return getBinaryStream(findColumn(columnLabel));
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {

	}

	@Override
	public String getCursorName() throws SQLException {
		return null;
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return resultSetMetaData;
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		Object r = null;
		int c = (columnIndex - 1);
		try {
			r = currentRow.get(c);
			lastColumnWasNull = (r == null);
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new SQLException("Invalid column index: " + (columnIndex), new ArrayIndexOutOfBoundsException(c));
		}
		return r;
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return getObject(findColumn(columnLabel));
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return new StringReader(getString(columnIndex));
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return getCharacterStream(findColumn(columnLabel));
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		Object r = getObject(columnIndex);
		return (lastColumnWasNull ? null : new BigDecimal(String.valueOf(r)));
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return getBigDecimal(findColumn(columnLabel));
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		if (rows.size() > 0) {
			return (currentRowIndex < 0);
		}
		return false;
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		if (rows.size() > 0) {
			return (currentRowIndex > rows.size());
		}
		return false;
	}

	@Override
	public boolean isFirst() throws SQLException {
		if (rows.size() > 0) {
			return (currentRowIndex == 0);
		}
		return false;
	}

	@Override
	public boolean isLast() throws SQLException {
		if (rows.size() > 0) {
			return (currentRowIndex == (rows.size() - 1));
		}
		return false;
	}

	@Override
	public void beforeFirst() throws SQLException {
		if (rows.size() > 0) {
			currentRowIndex = -1;
		}
	}

	@Override
	public void afterLast() throws SQLException {
		if (rows.size() > 0) {
			currentRowIndex = rows.size();
		}
	}

	@Override
	public boolean first() throws SQLException {
		if (rows.size() > 0) {
			currentRowIndex = 0;
			return true;
		}
		currentRowIndex = -1;
		return false;
	}

	@Override
	public boolean last() throws SQLException {
		if (rows.size() > 0) {
			currentRowIndex = (rows.size() - 1);
			return true;
		}
		currentRowIndex = -1;
		return false;
	}

	@Override
	public int getRow() throws SQLException {
		return (currentRowIndex + 1);
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		if (rows.size() > 0) {
			if (row >= 0) {
				currentRowIndex = (row - 1);
			} else {
				currentRowIndex = (rows.size() + row);
			}
			currentRowIndex = Math.max(-1, Math.min(rows.size(), currentRowIndex));
			refreshCurrentRow();
			return (currentRowIndex >= 0 && currentRowIndex < rows.size());
		}
		refreshCurrentRow();
		return false;
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		int ri = currentRowIndex + rows;
		ri = Math.max(-1, Math.min(this.rows.size(), ri));
		return absolute(ri);
	}

	@Override
	public boolean previous() throws SQLException {
		return relative(-1);
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		this.fetchDirection = direction;
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return this.fetchDirection;
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {

	}

	@Override
	public int getFetchSize() throws SQLException {
		return 0;
	}

	@Override
	public int getType() throws SQLException {
		return ResultSet.TYPE_SCROLL_INSENSITIVE;
	}

	@Override
	public int getConcurrency() throws SQLException {
		return ResultSet.CONCUR_READ_ONLY;
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return false;
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return false;
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return false;
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		updateNull(columnNames.get(columnIndex));
	}

	@Override
	public void updateBoolean(int columnIndex, boolean value) throws SQLException {
		updateBoolean(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateByte(int columnIndex, byte value) throws SQLException {
		updateByte(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateShort(int columnIndex, short value) throws SQLException {
		updateShort(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateInt(int columnIndex, int value) throws SQLException {
		updateInt(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateLong(int columnIndex, long value) throws SQLException {
		updateLong(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateFloat(int columnIndex, float value) throws SQLException {
		updateFloat(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateDouble(int columnIndex, double value) throws SQLException {
		updateDouble(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal value) throws SQLException {
		updateBigDecimal(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateString(int columnIndex, String value) throws SQLException {
		updateString(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateBytes(int columnIndex, byte[] value) throws SQLException {
		updateBytes(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateDate(int columnIndex, Date value) throws SQLException {
		updateDate(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateTime(int columnIndex, Time value) throws SQLException {
		updateTime(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp value) throws SQLException {
		updateTimestamp(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream value, int length) throws SQLException {
		updateAsciiStream(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream value, int length) throws SQLException {
		updateBinaryStream(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader value, int length) throws SQLException {
		updateCharacterStream(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateObject(int columnIndex, Object value, int scaleOrLength) throws SQLException {
		updateObject(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateObject(int columnIndex, Object value) throws SQLException {
		updateObject(columnNames.get(columnIndex), value);
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, null);
	}

	@Override
	public void updateBoolean(String columnLabel, boolean value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateByte(String columnLabel, byte value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateShort(String columnLabel, short value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateInt(String columnLabel, int value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateLong(String columnLabel, long value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateFloat(String columnLabel, float value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateDouble(String columnLabel, double value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateString(String columnLabel, String value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateBytes(String columnLabel, byte[] value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateDate(String columnLabel, Date value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateTime(String columnLabel, Time value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream value, int length) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream value, int length) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader value, int length) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateObject(String columnLabel, Object value, int scaleOrLength) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void updateObject(String columnLabel, Object value) throws SQLException {
		Map<String, Object> columns = rows.get(currentRowIndex);
		columns.put(columnLabel, value);
	}

	@Override
	public void insertRow() throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateRow() throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void deleteRow() throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void refreshRow() throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void moveToCurrentRow() throws SQLException {

	}

	@Override
	public Statement getStatement() throws SQLException {
		return null;
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		return (Ref)getObject(columnIndex);
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		return (Blob)getObject(columnIndex);
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		return (Clob)getObject(columnIndex);
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		return (Array)getObject(columnIndex);
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		return getRef(findColumn(columnLabel));
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		return getBlob(findColumn(columnLabel));
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		return getClob(findColumn(columnLabel));
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		return getArray(findColumn(columnLabel));
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		Object r = getObject(columnIndex);
//		return AbstractCriterion.getDate(String.valueOf(r));
		return (Date)r;
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return getDate(findColumn(columnLabel), cal);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		Object r = getObject(columnIndex);
		return (Time)r;
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return getTime(findColumn(columnLabel), cal);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		Object r = getObject(columnIndex);
		return (Timestamp)r;
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		return getTimestamp(findColumn(columnLabel), cal);
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		URL r = null;
		try {
			r = new URL(getString(columnIndex));
		} catch (MalformedURLException ex) {
			Main.err(ex);
		}
		return r;
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		return getURL(findColumn(columnLabel));
	}

	@Override
	public void updateRef(int columnIndex, Ref value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateRef(String columnLabel, Ref value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateBlob(int columnIndex, Blob value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateBlob(String columnLabel, Blob value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateClob(int columnIndex, Clob value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateClob(String columnLabel, Clob value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateArray(int columnIndex, Array value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateArray(String columnLabel, Array value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateRowId(int columnIndex, RowId value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateRowId(String columnLabel, RowId value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getHoldability() throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream value) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
