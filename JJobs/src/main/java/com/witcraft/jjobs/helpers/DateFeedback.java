package com.witcraft.jjobs.helpers;

/**
 *
 * @author Jason Redding
 */
public class DateFeedback {

	private DateType type;
	private DatePart part;
	private DatePrecision leastSignificantField;

	/**
	 * Constructs a default {@link DateFeedback} with
	 */
	public DateFeedback() {
		this.part = DatePart.DATE_TIME;
	}

	/**
	 *
	 * @return
	 */
	public DatePart getPart() {
		return part;
	}

	/**
	 *
	 * @param part
	 */
	public void setPart(DatePart part) {
		this.part = part;
	}

	/**
	 *
	 * @return
	 */
	public DateType getType() {
		return type;
	}

	/**
	 *
	 * @param type
	 */
	public void setType(DateType type) {
		this.type = type;
	}

	/**
	 *
	 * @return
	 */
	public DatePrecision getLeastSignificantField() {
		return leastSignificantField;
	}

	/**
	 *
	 * @param leastSignificantField
	 */
	public void setLeastSignificantField(DatePrecision leastSignificantField) {
		this.leastSignificantField = leastSignificantField;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[")
		.append(type)
		.append("] ")
		.append(part)
		.append(" (specificity: ")
		.append(leastSignificantField)
		.append(")");
		return sb.toString();
	}

	/**
	 * Enumeration of the different ways to represent time, such as a segment
	 * ({@link #INTERVAL}), and a moment ({@link #MOMENT}).
	 */
	public static enum DateType {

		/**
		 * A single moment in time.
		 */
		/**
		 * A single moment in time.
		 */
		MOMENT,
		/**
		 * A segment or duration of time.
		 */
		INTERVAL;
	}

	/**
	 * Enumeration of the different combination of parts of a valid date.
	 */
	public static enum DatePart {

		/**
		 * Date and time (Example: '2005-05-19T17:19:00')
		 */
		DATE_TIME,
		/**
		 * Date (Example: '2005-05-19')
		 */
		DATE,
		/**
		 * Time (Example: '17:19:00')
		 */
		TIME;

		/**
		 *
		 * @param precision
		 *
		 * @return
		 */
		public final boolean hasField(DatePrecision precision) {
			if (this == DatePart.DATE || this == DatePart.DATE_TIME) {
				switch (precision) {
				case YEAR:
				case MONTH:
				case DAY:
					return true;
				default:
				}
			}
			if (this == DatePart.TIME || this == DatePart.DATE_TIME) {
				switch (precision) {
				case HOUR:
				case MINUTE:
				case SECOND:
					return true;
				default:
				}
			}
			return false;
		}

		/**
		 *
		 * @param period
		 *
		 * @return
		 */
		public static final DatePart valueOf(PeriodDuration period) {
			DatePart dPart;
			if (!period.hasDuration()) {
				dPart = DateFeedback.DatePart.DATE;
			} else if (!period.hasPeriod()) {
				dPart = DateFeedback.DatePart.TIME;
			} else {
				dPart = DateFeedback.DatePart.DATE_TIME;
			}
			return dPart;
		}

		/**
		 *
		 * @param precision
		 *
		 * @return
		 */
		public static final DatePart valueOf(DatePrecision precision) {
			switch (precision) {
			case YEAR:
			case MONTH:
			case DAY:
				return DatePart.DATE;
			case HOUR:
			case MINUTE:
			case SECOND:
				return DatePart.DATE_TIME;
			default:
				return null;
			}
		}
	}
}
