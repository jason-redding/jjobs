package com.witcraft.jjobs.helpers;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

/**
 *
 * @author Jason Redding
 */
public class JXPathVariableResolver implements XPathVariableResolver {

	private final Map<String, Object> vars = new HashMap<>();

	/**
	 *
	 * @param name
	 * @param value
	 */
	public void addVariable(String name, Object value) {
		vars.put(name, value);
	}

	/**
	 *
	 * @param name
	 */
	public void removeVariable(String name) {
		vars.remove(name);
	}

	@Override
	public Object resolveVariable(QName varName) {
		Object r = vars.get(varName.getLocalPart());
		return r;
	}

}
