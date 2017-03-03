package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.context.CriteriaContext;
import com.witcraft.jjobs.context.DateContext;
import com.witcraft.jjobs.criteria.AbstractCriterion;
import com.witcraft.jjobs.helpers.DateBias;
import com.witcraft.jjobs.helpers.DateFeedback;
import com.witcraft.jjobs.helpers.DatePrecision;
import com.witcraft.jjobs.helpers.FileDateAttribute;
import com.witcraft.jjobs.helpers.JSONResultSet;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public abstract class FileDateCriterion extends AbstractCriterion<File> implements FileCriterion {

	private LocalDateTime date;
	private final DatePrecision truncateTo;
	private final DateBias bias;
	private final FileDateAttribute relativeTo;
	private DateFeedback feedback;

	/**
	 *
	 * @param matching
	 * @param node
	 */
	protected FileDateCriterion(Matching<File> matching, Node node) {
		super(matching, node);
		truncateTo = DatePrecision.from(getAttribute("truncate-to"));
		bias = DateBias.valueOf(getAttribute("bias", DateBias.START.name()).toUpperCase());
		relativeTo = FileDateAttribute.from(getAttribute("relative-to"));
		date = parseDate(getContent(), bias, truncateTo, feedback = new DateFeedback());
	}

	/**
	 *
	 * @return
	 */
	public FileDateAttribute getRelativeTo() {
		return relativeTo;
	}

	/**
	 *
	 * @return
	 */
	public final LocalDateTime getDate() {
		return getDate((File)null);
	}

	/**
	 *
	 * @param file
	 *
	 * @return
	 */
	public final LocalDateTime getDate(File file) {
		if (relativeTo != null && file != null) {
			if (feedback.getType() == DateFeedback.DateType.INTERVAL) {
				switch (relativeTo) {
				case DATE_CREATED:
					return parseDate(DateContext.getFileDateCreated(file), getContent(), bias, truncateTo);
				case DATE_MODIFIED:
					return parseDate(DateContext.getFileDateModified(file), getContent(), bias, truncateTo);
				}
			} else {
				Main.err("Cannot use \"relative-to\" when date type is \"%s\".", feedback.getType());
			}
		}
		return date;
	}

	@Override
	protected boolean isMatch(File file) {
		CriteriaContext<File, LocalDateTime> c = getContext(LocalDateTime.class);
		LocalDateTime cValue = c.getContextValue(file);
		if (isValueDynamic()) {
			if (hasResultSet()) {
				try (JSONResultSet rs = getResultSet()) {
					boolean r = false;
//					rs.beforeFirst();
					while (rs.next()) {
						String v = rs.useFor(this);
						date = parseDate(v, bias, truncateTo, feedback);
						if (date == null) {
							Main.err("Failed to create LocalDateTime from \"%s\"", v);
							continue;
						}
						r = isMatch(file, cValue);
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
//			date = getDate(valueOf, bias, truncateTo, feedback);
		}
		return isMatch(file, cValue);
	}

	@Override
	public String expression(File file) {
		CriteriaContext<File, LocalDateTime> c = getContext(LocalDateTime.class);
		return expression(file, c.getContextValue(file));
	}

	/**
	 *
	 * @param file
	 * @param date
	 *
	 * @return
	 */
	protected abstract boolean isMatch(File file, LocalDateTime date);

	/**
	 *
	 * @param file
	 * @param date
	 *
	 * @return
	 */
	public boolean matches(File file, LocalDateTime date) {
		boolean r = isMatch(file, date);
		if (isNegated()) {
			r = !r;
		}
		return r;
	}

	/**
	 *
	 * @param file
	 * @param date
	 *
	 * @return
	 */
	public abstract String expression(File file, LocalDateTime date);

	@Override
	public String toString() {
		if (relativeTo != null) {
			return super.toString();
		}
		LocalDateTime d = getDate();
		StringBuilder sb = new StringBuilder(super.toString());
		if (d != null) {
			sb.append(" (");
			sb.append(d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm:ss a")));
			sb.append(")");
		}
		return sb.toString();
	}
}
