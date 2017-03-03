package com.witcraft.jjobs.helpers;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A PeriodDuration encapsulates a {@link java.time.Period} and
 * {@link java.time.Duration} object, effectively merging the two.
 *
 * @author Jason Redding
 */
public class PeriodDuration implements TemporalAmount {

	private final Period period;
	private final Duration duration;
	private static final List<TemporalUnit> UNITS;

	static {
		List<TemporalUnit> units = new ArrayList<>();
		units.add(ChronoUnit.YEARS);
		units.add(ChronoUnit.MONTHS);
		units.add(ChronoUnit.DAYS);
		units.add(ChronoUnit.HOURS);
		units.add(ChronoUnit.MINUTES);
		units.add(ChronoUnit.SECONDS);
		units.add(ChronoUnit.NANOS);
		UNITS = Collections.unmodifiableList(units);
	}

	/**
	 * Constructs a new {@link PeriodDuration} with the specified {@code Period}
	 * and {@code Duration}, allowing the measurement of the combined time
	 * interval.
	 *
	 * @param period the period this object will encapsulate.
	 * @param duration the duration this object will encapsulate.
	 */
	public PeriodDuration(Period period, Duration duration) {
		this.period = period;
		this.duration = duration;
	}

	/**
	 *
	 * @return
	 */
	public Period getPeriod() {
		return period;
	}

	/**
	 *
	 * @return
	 */
	public Duration getDuration() {
		return duration;
	}

	/**
	 *
	 * @return
	 */
	public final boolean hasPeriod() {
		return !period.isZero();
	}

	/**
	 *
	 * @return
	 */
	public final boolean hasDuration() {
		return !duration.isZero();
	}

	/**
	 *
	 * @param input
	 *
	 * @return
	 */
	public static PeriodDuration parse(String input) {
		int periodStart = input.indexOf('P');
		int timeStart = input.indexOf('T');
		Period p;
		Duration d;
		if (periodStart >= 0) {
			boolean isNegative = (input.lastIndexOf('-', periodStart) >= 0);
			if (timeStart > (periodStart + 1)) {
				p = Period.parse(input.substring(periodStart, timeStart));
			} else if (timeStart == -1) {
				p = Period.parse(input.substring(periodStart));
			} else {
				p = Period.ZERO;
			}
			if (timeStart > 0) {
				d = Duration.parse("P" + input.substring(timeStart));
			} else {
				d = Duration.ZERO;
			}
			if (isNegative) {
				p = p.negated();
				d = d.negated();
			}
		} else {
			p = Period.ZERO;
			d = Duration.ZERO;
		}
		return new PeriodDuration(p, d);
	}

	@Override
	public String toString() {
		return period.toString() + duration.toString().substring(1);
	}

	@Override
	public long get(TemporalUnit unit) {
		if (UNITS.contains(unit)) {
			if (period.getUnits().contains(unit)) {
				return period.get(unit);
			} else if (duration.getUnits().contains(unit)) {
				return duration.get(unit);
			}
		}
		throw new UnsupportedTemporalTypeException(unit.toString() + " is not supported by " + PeriodDuration.class.getCanonicalName());
	}

	@Override
	public List<TemporalUnit> getUnits() {
		return UNITS;
	}

	@Override
	public Temporal addTo(Temporal temporal) {
		Temporal t = temporal;
		long amount;
		for (TemporalUnit unit : period.getUnits()) {
			if (t.isSupported(unit)) {
				amount = period.get(unit);
				if (amount != 0) {
					t = t.plus(amount, unit);
				}
			}
		}
		for (TemporalUnit unit : duration.getUnits()) {
			if (t.isSupported(unit)) {
				amount = duration.get(unit);
				if (amount != 0) {
					t = t.plus(amount, unit);
				}
			}
		}
		return t;
	}

	@Override
	public Temporal subtractFrom(Temporal temporal) {
		Temporal t = temporal;
		long amount;
		for (TemporalUnit unit : period.getUnits()) {
			if (t.isSupported(unit)) {
				amount = period.get(unit);
				if (amount != 0) {
					t = t.minus(amount, unit);
				}
			}
		}
		for (TemporalUnit unit : duration.getUnits()) {
			if (t.isSupported(unit)) {
				amount = duration.get(unit);
				if (amount != 0) {
					t = t.minus(amount, unit);
				}
			}
		}
		return t;
	}
}
