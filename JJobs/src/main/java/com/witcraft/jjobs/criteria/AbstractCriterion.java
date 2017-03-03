package com.witcraft.jjobs.criteria;

import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.context.CriteriaContext;
import com.witcraft.jjobs.helpers.DateBias;
import com.witcraft.jjobs.helpers.DateFeedback;
import com.witcraft.jjobs.helpers.DateFeedback.DatePart;
import com.witcraft.jjobs.helpers.DateFeedback.DateType;
import com.witcraft.jjobs.helpers.DatePrecision;
import com.witcraft.jjobs.helpers.JSONResultSet;
import com.witcraft.jjobs.helpers.PeriodDuration;
import com.witcraft.jjobs.nodes.XmlElementWithContent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPath;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jason Redding
 * @param <T> the type that will be passed to the {@link #isMatch} method.
 *
 */
public abstract class AbstractCriterion<T> extends XmlElementWithContent implements Criterion<T> {

	private static final Pattern PATTERN_YEAR_MONTH = Pattern.compile("^[+-]?0*(\\d+)-0*(\\d+)");
	private static final HashMap<String, Class<? extends AbstractCriterion>> REGISTERED_CRITERIA = new HashMap<>();

	/**
	 *
	 * @param matching
	 * @param node
	 *
	 * @return
	 */
	public static AbstractCriterion newFromNode(Matching matching, Node node) {
		Class<? extends AbstractCriterion> clazz = REGISTERED_CRITERIA.get(getNameUntil(node, matching.getNode()));
		if (clazz != null) {
			try {
				Constructor<? extends AbstractCriterion> init = clazz.getConstructor(Matching.class, Node.class);
				return init.newInstance(matching, node);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				Main.err("Failed to instantiate Criterion from XML Node <%s>", ex, node.getNodeName());
			}
		}
		return null;
	}

	/**
	 *
	 * @param name
	 * @param clazz
	 */
	public static final void register(String name, Class<? extends AbstractCriterion> clazz) {
		REGISTERED_CRITERIA.put(name, clazz);
	}

	/**
	 *
	 * @param value
	 * @param formatter
	 *
	 * @return
	 */
	public static LocalTime parseLocalTime(String value, DateTimeFormatter formatter) {
		try {
			return LocalTime.parse(value, formatter);
		} catch (DateTimeParseException ex) {
		}
		return null;
	}

	/**
	 *
	 * @param value
	 * @param formatter
	 *
	 * @return
	 */
	public static LocalDate parseLocalDate(String value, DateTimeFormatter formatter) {
		try {
			return LocalDate.parse(value, formatter);
		} catch (DateTimeParseException ex) {
		}
		return null;
	}

	/**
	 *
	 * @param value
	 * @param formatter
	 *
	 * @return
	 */
	public static LocalDateTime parseLocalDateTime(String value, DateTimeFormatter formatter) {
		try {
			return LocalDateTime.parse(value, formatter);
		} catch (DateTimeParseException ex) {
		}
		return null;
	}

	/**
	 *
	 * @param value
	 *
	 * @return
	 */
	public static YearMonth parseYearMonth(String value) {
		try {
			return YearMonth.parse(value);
		} catch (DateTimeParseException ex) {
		}
		return null;
	}

	/**
	 *
	 * @param value
	 *
	 * @return
	 */
	public static LocalDateTime parseDate(String value) {
		return parseDate(null, value, null, null, null);
	}

	/**
	 *
	 * @param value
	 * @param feedback
	 *
	 * @return
	 */
	public static LocalDateTime parseDate(String value, DateFeedback feedback) {
		return parseDate(null, value, null, null, feedback);
	}

	/**
	 *
	 * @param anchor
	 * @param value
	 * @param feedback
	 *
	 * @return
	 */
	public static LocalDateTime parseDate(LocalDateTime anchor, String value, DateFeedback feedback) {
		return parseDate(anchor, value, null, null, feedback);
	}

	/**
	 *
	 * @param value
	 * @param truncatedTo
	 *
	 * @return
	 */
	public static LocalDateTime parseDate(String value, DatePrecision truncatedTo) {
		return parseDate(null, value, null, truncatedTo, null);
	}

	/**
	 *
	 * @param value
	 * @param bias
	 *
	 * @return
	 */
	public static LocalDateTime parseDate(String value, DateBias bias) {
		return parseDate(null, value, bias, null, null);
	}

	/**
	 *
	 * @param value
	 * @param bias
	 * @param feedback
	 *
	 * @return
	 */
	public static LocalDateTime parseDate(String value, DateBias bias, DateFeedback feedback) {
		return parseDate(null, value, bias, null, feedback);
	}

	/**
	 *
	 * @param value
	 * @param bias
	 * @param truncatedTo
	 *
	 * @return the java.time.LocalDateTime
	 */
	public static LocalDateTime parseDate(String value, DateBias bias, DatePrecision truncatedTo) {
		return parseDate(null, value, bias, truncatedTo, null);
	}

	/**
	 *
	 * @param anchor
	 * @param value
	 * @param bias
	 * @param truncatedTo
	 *
	 * @return
	 */
	public static LocalDateTime parseDate(LocalDateTime anchor, String value, DateBias bias, DatePrecision truncatedTo) {
		return parseDate(anchor, value, bias, truncatedTo, null);
	}

	/**
	 *
	 * @param value
	 * @param bias the value of bias
	 * @param truncatedTo
	 * @param feedback
	 *
	 * @return the java.time.LocalDateTime
	 */
	public static LocalDateTime parseDate(String value, DateBias bias, DatePrecision truncatedTo, DateFeedback feedback) {
		return parseDate(null, value, bias, truncatedTo, feedback);
	}

	/**
	 *
	 * @param anchor
	 * @param value
	 * @param truncateTo
	 * @param feedback
	 *
	 * @return
	 */
	public static LocalDateTime parseDate(LocalDateTime anchor, String value, DatePrecision truncateTo, DateFeedback feedback) {
		return parseDate(anchor, value, null, truncateTo, feedback);
	}

	/**
	 *
	 * @param anchor
	 * @param value
	 * @param bias the value of bias
	 * @param truncateTo
	 * @param feedback
	 *
	 * @return the java.time.LocalDateTime
	 */
	public static LocalDateTime parseDate(LocalDateTime anchor, String value, DateBias bias, DatePrecision truncateTo, DateFeedback feedback) {
		LocalDateTime relativeDate = null;
		if (anchor == null) {
			anchor = LocalDateTime.now().withNano(0);
		}
		DateType dType;
		DatePart dPart = DatePart.DATE_TIME;
		DatePrecision leastSignificantField = null;
		if (value == null || value.trim().length() == 0 || value.trim().equals("now")) {
			dType = DateType.MOMENT;
			relativeDate = anchor;
		} else if (value.contains("P")) {
			dType = DateType.INTERVAL;
			PeriodDuration pd = PeriodDuration.parse(value);
			relativeDate = anchor.plus(pd);
			dPart = DatePart.valueOf(pd);
		} else {
			dType = DateType.MOMENT;
			while (true) {
				LocalTime t = parseLocalTime(value, DateTimeFormatter.ISO_OFFSET_TIME);
				if (t == null) {
					t = parseLocalTime(value, DateTimeFormatter.ISO_LOCAL_TIME);
				}
				if (t != null) {
					relativeDate = anchor.withHour(t.getHour()).withMinute(t.getMinute()).withSecond(t.getSecond()).withNano(t.getNano());
					dPart = DatePart.TIME;
					break;
				}
				relativeDate = parseLocalDateTime(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
				if (relativeDate != null) {
					dPart = DatePart.DATE_TIME;
					break;
				}
				relativeDate = parseLocalDateTime(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				if (relativeDate != null) {
					dPart = DatePart.DATE_TIME;
					break;
				}
				LocalDate d = parseLocalDate(value, DateTimeFormatter.ISO_OFFSET_DATE);
				if (d != null) {
					relativeDate = LocalDateTime.of(d, LocalTime.MIN);
					dPart = DatePart.DATE;
					break;
				}
				d = parseLocalDate(value, DateTimeFormatter.ISO_LOCAL_DATE);
				if (d != null) {
					relativeDate = LocalDateTime.of(d, LocalTime.MIN);
					dPart = DatePart.DATE;
					break;
				}
				Matcher m = PATTERN_YEAR_MONTH.matcher(value);
				if (m.matches()) {
					BigInteger year = new BigInteger(m.group(1));
					if (year.compareTo(BigInteger.valueOf(10000)) >= 0 && !value.startsWith("+")) {
						value = "+" + value;
					}
				}
				YearMonth ym = parseYearMonth(value);
				if (ym != null) {
					relativeDate = LocalDateTime.of(ym.atDay(1), LocalTime.MIN);
					dPart = DatePart.DATE;
					leastSignificantField = DatePrecision.MONTH;
				}
				break;
			}
		}
		if (leastSignificantField == null) {
			leastSignificantField = DatePrecision.valueOf(relativeDate);
		}
		if (relativeDate != null) {
			relativeDate = applyDateRules(relativeDate, bias, truncateTo);
		}

		if (feedback != null) {
			feedback.setType(dType);
			feedback.setPart(dPart);
			feedback.setLeastSignificantField(leastSignificantField);
		}
		return relativeDate;
	}

	/**
	 *
	 * @param input
	 * @param bias
	 * @param truncateTo
	 *
	 * @return
	 */
	public static LocalDateTime applyDateRules(LocalDateTime input, DateBias bias, DatePrecision truncateTo) {
		LocalDateTime rv = input;
		DatePrecision dp = DatePrecision.valueOf(input);
		if (truncateTo != null && input.isSupported(truncateTo.getTemporalUnit())) {
			switch (truncateTo) {
			case YEAR:
				switch (bias) {
				case START:
					rv = LocalDateTime.of(rv.with(TemporalAdjusters.firstDayOfYear()).toLocalDate(), LocalTime.MIN);
					break;
				case END:
					rv = LocalDateTime.of(rv.with(TemporalAdjusters.lastDayOfYear()).toLocalDate(), LocalTime.MAX);
					break;
				}
				break;
			case MONTH:
				switch (bias) {
				case START:
					rv = LocalDateTime.of(rv.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate(), LocalTime.MIN);
					break;
				case END:
					rv = LocalDateTime.of(rv.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate(), LocalTime.MAX);
					break;
				}
				break;
			default:
				rv = rv.truncatedTo(truncateTo.getTemporalUnit());
				break;
			}
		}
		if (truncateTo == null || (truncateTo != DatePrecision.YEAR && truncateTo != DatePrecision.MONTH)) {
			switch (bias) {
			case START:
				rv = rv.truncatedTo(dp.getTemporalUnit());
				break;
			case END:
				rv = rv.plus(1, dp.getTemporalUnit()).minusNanos(1);
				break;
			}
		}
		return rv;
	}

	private final Matching<T> matching;
	private final boolean negate;
	private final CriteriaContext context;
	private final CriteriaValue criteriaValue;

	/**
	 *
	 * @param matching
	 * @param node
	 */
	protected AbstractCriterion(Matching<T> matching, Node node) {
		super(node);
		this.matching = matching;
		this.negate = getBooleanAttribute("negate", false);
		Node n = node;
		while (n != null && !matching.getNode().isSameNode(n.getParentNode())) {
			n = n.getParentNode();
		}
		this.context = CriteriaContext.newFromNode(matching, n);

		Node firstChild = null;
		if (node.hasChildNodes()) {
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				n = nodes.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					firstChild = n;
					break;
				}
			}
		}
//		if (CriteriaValue.isCriteriaValue(firstChild)) {
		this.criteriaValue = CriteriaValue.newFromNode(firstChild);
//		} else {
//			this.criteriaValue = null;
//		}
	}

	/**
	 *
	 * @return
	 */
	public boolean isValueStatic() {
		if (hasValue()) {
			return getValue().isValueStatic();
		}
		return true;
	}

	/**
	 *
	 * @return
	 */
	public boolean isValueDynamic() {
		if (hasValue()) {
			return getValue().isValueDynamic();
		}
		return false;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public boolean hasResultSet() {
		if (hasValue()) {
			return getValue().hasResultSet();
		}
		return false;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public final JSONResultSet getResultSet() {
		return getResultSet(0);
	}

	/**
	 *
	 * @param position
	 *
	 * @return
	 */
	@Override
	public JSONResultSet getResultSet(int position) {
		if (hasValue()) {
			JSONResultSet jrs = getValue().getResultSet();
			try {
				jrs.absolute(position);
			} catch (SQLException ex) {
				Main.err(ex);
			}
			return jrs;
		}
		return null;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public final boolean hasValue() {
		return (criteriaValue != null);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public final CriteriaValue getValue() {
		return criteriaValue;
	}

	/**
	 *
	 * @param rs
	 *
	 * @return
	 */
	public final String using(ResultSet rs) {
		return getValue().using(rs);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public final boolean isNegated() {
		return negate;
	}

	/**
	 *
	 * @param object
	 *
	 * @return
	 */
	protected abstract boolean isMatch(T object);

	/**
	 *
	 * @param object
	 *
	 * @return
	 */
	@Override
	public final boolean matches(T object) {
		boolean r = isMatch(object);
		if (negate) {
			r = !r;
		}
		return r;
	}

	/**
	 *
	 *
	 * @param object
	 *
	 * @return the java.lang.String
	 */
	@Override
	public abstract String expression(T object);

	@Override
	public String getContent() {
		if (hasValue()) {
			return getValue().getContent();
		}
		return super.getContent();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public final CriteriaContext getContext() {
		return context;
	}

	/**
	 *
	 * @param <T>
	 * @param <O>
	 * @param clazz
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final <T, O> CriteriaContext<T, O> getContext(Class<O> clazz) {
		return context;
	}

	/**
	 *
	 * @return
	 */
	public final XPath getXPath() {
		return matching.getJobAction().getXPath();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public final Matching getMatching() {
		return matching;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public final String getFullName() {
		return getNameUntil(getNode(), matching.getNode()).replaceAll("/", "-");
	}

	/**
	 *
	 * @return
	 */
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
		sb
		.append(" \"")
		.append(valueToString())
		.append('"');
		return sb.toString();
	}
}
