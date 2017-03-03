package com.witcraft.jjobs.criteria.dates;

import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.criteria.AbstractCriterion;
import com.witcraft.jjobs.helpers.DateBias;
import com.witcraft.jjobs.helpers.DateFeedback;
import com.witcraft.jjobs.helpers.DatePrecision;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public abstract class DateCriterion extends AbstractCriterion<LocalDateTime> {

	private final LocalDateTime date;
	private final DatePrecision truncateTo;
	private final DateFeedback feedback;

	/**
	 *
	 * @param matching
	 * @param node
	 */
	protected DateCriterion(Matching<LocalDateTime> matching, Node node) {
		super(matching, node);
		truncateTo = DatePrecision.from(getAttribute("truncate-to"));
		DateBias dateBias = DateBias.valueOf(getAttribute("bias", DateBias.START.name()).toUpperCase());
		date = parseDate(getContent(), dateBias, truncateTo, feedback = new DateFeedback());
	}

	/**
	 *
	 * @return
	 */
	public final LocalDateTime getDate() {
		return date;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" (")
		.append(getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm:ss a")))
		.append(")");
		return sb.toString();
	}
}
