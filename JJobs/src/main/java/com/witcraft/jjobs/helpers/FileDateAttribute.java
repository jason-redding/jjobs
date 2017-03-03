package com.witcraft.jjobs.helpers;

/**
 *
 * @author Jason Redding
 */
public enum FileDateAttribute {

	/**
	 *
	 */
	DATE_CREATED,
	/**
	 *
	 */
	DATE_MODIFIED;

	/**
	 *
	 * @param value
	 *
	 * @return
	 */
	public static FileDateAttribute from(String value) {
		if (value != null) {
			return valueOf(value.replaceAll("-+", "_").toUpperCase());
		}
		return null;
	}

	@Override
	public String toString() {
		return name().replaceAll("_+", "-");
	}
}
