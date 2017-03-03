package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.context.CriteriaContext;
import com.witcraft.jjobs.criteria.AbstractCriterion;
import com.witcraft.jjobs.helpers.JSONResultSet;
import com.witcraft.jjobs.helpers.SizeUnit;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public abstract class FileSizeCriterion extends AbstractCriterion<File> implements FileCriterion {

	private BigDecimal fileSize;
	private BigDecimal fileSizeInBytes;
	private final SizeUnit fileSizeUnit;

	/**
	 *
	 * @param matching
	 * @param node
	 */
	protected FileSizeCriterion(Matching<File> matching, Node node) {
		super(matching, node);
		fileSizeUnit = SizeUnit.valueOf(getAttribute("unit", SizeUnit.B.name()).toUpperCase());
		try {
			setFileSize(new BigDecimal(getContent()));
		} catch (NumberFormatException nfex) {
//			Main.err(nfex);
		}
	}

	/**
	 *
	 * @return
	 */
	public BigDecimal getFileSize() {
		return fileSize;
	}

	private void setFileSize(BigDecimal size) {
		fileSize = size;
		fileSizeInBytes = fileSizeUnit.getFactor().multiply(fileSize);
	}

	/**
	 *
	 * @return
	 */
	public BigDecimal getFileSizeInBytes() {
		return fileSizeInBytes;
	}

	/**
	 *
	 * @return
	 */
	public SizeUnit getFileSizeUnit() {
		return fileSizeUnit;
	}

	/**
	 *
	 * @param file
	 *
	 * @return
	 */
	@Override
	protected final boolean isMatch(File file) {
		CriteriaContext<File, BigDecimal> c = getContext(BigDecimal.class);
		BigDecimal cValue = c.getContextValue(file);
		if (isValueDynamic()) {
			if (hasResultSet()) {
				try (JSONResultSet rs = getResultSet()) {
					boolean r = false;
//					rs.beforeFirst();
					while (rs.next()) {
						String v = rs.useFor(this);
						try {
							setFileSize(new BigDecimal(v));
							r = isMatch(cValue);
							if (r) {
								break;
							}
						} catch (NumberFormatException nfex) {
							Main.err("Failed to create BigDecimal from \"%s\"", v);
						}
					}
					return r;
				} catch (SQLException sex) {
					Main.err(sex);
				}
			}
			return false;
		}
		return isMatch(cValue);
//		return (file.isDirectory() || isMatch(BigDecimal.valueOf(file.length())));
	}

	@Override
	public final String expression(File file) {
		CriteriaContext<File, BigDecimal> c = getContext(BigDecimal.class);
		return expression(c.getContextValue(file));
	}

	/**
	 *
	 * @param size
	 *
	 * @return
	 */
	public abstract String expression(BigDecimal size);

	/**
	 *
	 * @param size
	 *
	 * @return
	 */
	protected abstract boolean isMatch(BigDecimal size);

	/**
	 *
	 * @param size
	 *
	 * @return
	 */
	public final boolean matches(BigDecimal size) {
		boolean r = isMatch(size);
		if (isNegated()) {
			r = !r;
		}
		return r;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String valueToString() {
		NumberFormat df = DecimalFormat.getInstance();
		SizeUnit unit = getFileSizeUnit();
		BigDecimal size = getFileSize();
		BigDecimal sizeInBytes = getFileSizeInBytes();
		StringBuilder sb = new StringBuilder();
		if (size != null && sizeInBytes != null) {
			sb
			.append(df.format(size))
			.append(" ")
			.append(unit.getName());
			if (BigDecimal.ONE.compareTo(size) != 0) {
				sb.append("s");
			}
			sb
			.append(" (")
			.append(df.format(sizeInBytes))
			.append(" ")
			.append(SizeUnit.B.getName().toLowerCase());
			if (BigDecimal.ONE.compareTo(sizeInBytes) != 0) {
				sb.append("s");
			}
			sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getFullName());
		sb.append(" = ").append(valueToString());
		return sb.toString();
	}
}
