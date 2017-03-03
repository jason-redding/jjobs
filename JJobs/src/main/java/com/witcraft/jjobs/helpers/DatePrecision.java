package com.witcraft.jjobs.helpers;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

/**
 *
 * @author Jason Redding
 */
public enum DatePrecision {

	/**
	 *
	 */
	YEAR(ChronoUnit.YEARS, ChronoField.YEAR),
	/**
	 *
	 */
	MONTH(ChronoUnit.MONTHS, ChronoField.MONTH_OF_YEAR),
	/**
	 *
	 */
	DAY(ChronoUnit.DAYS, ChronoField.DAY_OF_MONTH),
	/**
	 *
	 */
	HOUR(ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY),
	/**
	 *
	 */
	MINUTE(ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR),
	/**
	 *
	 */
	SECOND(ChronoUnit.SECONDS, ChronoField.SECOND_OF_MINUTE),
	/**
	 *
	 */
	MILLISECOND(ChronoUnit.MILLIS, ChronoField.MILLI_OF_SECOND),
	/**
	 *
	 */
	NANOSECOND(ChronoUnit.NANOS, ChronoField.NANO_OF_SECOND);

	/**
	 *
	 * @param value
	 *
	 * @return
	 */
	public static DatePrecision from(String value) {
		if (value != null) {
			return valueOf(value.toUpperCase());
		}
		return null;
	}

	private final ChronoUnit unit;
	private final ChronoField field;

	DatePrecision(ChronoUnit unit, ChronoField field) {
		this.unit = unit;
		this.field = field;
	}

	/**
	 *
	 * @return
	 */
	public ChronoUnit getTemporalUnit() {
		return unit;
	}

	/**
	 *
	 * @return
	 */
	public ChronoField getTemporalField() {
		return field;
	}

	/**
	 *
	 * @param temporal
	 *
	 * @return
	 */
	public static final DatePrecision valueOf(Temporal temporal) {
		DatePrecision rv = null;
		if (temporal != null) {
			for (DatePrecision d : DatePrecision.values()) {
				if (temporal.isSupported(d.getTemporalField())) {
					long fv = temporal.getLong(d.getTemporalField());
					if (fv != 0) {
						rv = d;
					}
				}
			}
		}
		return rv;
	}
}
