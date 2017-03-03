package com.witcraft.jjobs.helpers;

import java.math.BigDecimal;

/**
 * A size unit describing data. A size can be in one of the following supported units:
 * <ul>
 * <li>{@link #B} &mdash; Byte<br>Unit representing the smallest practical unit of data.</li>
 * <li>{@link #K} &mdash; Kilobyte<br>Unit representing 1,000 bytes.</li>
 * <li>{@link #M} &mdash; Megabyte<br>Unit representing 1,000,000 bytes.</li>
 * <li>{@link #G} &mdash; Gigabyte<br>Unit representing 1,000,000,000 bytes.</li>
 * <li>{@link #T} &mdash; Terabyte<br>Unit representing 1,000,000,000,000 bytes.</li>
 * <li>{@link #P} &mdash; Petabyte<br>Unit representing 1,000,000,000,000,000 bytes.</li>
 * <li>{@link #E} &mdash; Exabyte<br>Unit representing 1,000,000,000,000,000,000 bytes.</li>
 * <li>{@link #Z} &mdash; Zettabyte<br>Unit representing 1,000,000,000,000,000,000,000 bytes.</li>
 * <li>{@link #Y} &mdash; Yottabyte<br>Unit representing 1,000,000,000,000,000,000,000,000 bytes.</li>
 * </ul>
 *
 * @author Jason Redding
 */
public enum SizeUnit {

	/**
	 * Size unit for Bytes.
	 */
	B("Byte"),
	/**
	 * Size unit for Kilobytes (1,000 bytes).
	 */
	K("Kilobyte"),
	/**
	 * Size unit for Megabytes (1,000,000 bytes).
	 */
	M("Megabyte"),
	/**
	 * Size unit for Gigabytes (1,000,000,000 bytes).
	 */
	G("Gigabyte"),
	/**
	 * Size unit for Terabytes (1,000,000,000,000 bytes).
	 */
	T("Terabyte"),
	/**
	 * Size unit for Petabytes (1,000,000,000,000,000 bytes).
	 */
	P("Petabyte"),
	/**
	 * Size unit for Exabytes (1,000,000,000,000,000,000 bytes).
	 */
	E("Exabyte"),
	/**
	 * Size unit for Zettabytes (1,000,000,000,000,000,000,000 bytes).
	 */
	Z("Zettabyte"),
	/**
	 * Size unit for Yottabytes (1,000,000,000,000,000,000,000,000 bytes).
	 */
	Y("Yottabyte");

	private final String name;
	private final BigDecimal factor;

	SizeUnit(String name) {
		this.name = name;
		this.factor = BigDecimal.TEN.pow(this.ordinal() * 3);
	}

	/**
	 * Returns the long name of this unit.
	 *
	 * @return the long name of this unit.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the factor of this unit in terms of bytes.
	 *
	 * @return the factor of this unit.
	 */
	public BigDecimal getFactor() {
		return factor;
	}
}
