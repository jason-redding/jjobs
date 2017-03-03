package com.witcraft.jjobs.helpers;

import javax.xml.namespace.NamespaceContext;

/**
 *
 * @author Jason Redding
 */
public class JXPathBuilder {

	private NamespaceContext namespaceContext;
	private StringBuilder path;

	/**
	 *
	 * @param namespaceContext
	 */
	public JXPathBuilder(NamespaceContext namespaceContext) {
		this(namespaceContext, null);
	}

	/**
	 *
	 * @param namespaceContext
	 * @param path
	 */
	public JXPathBuilder(NamespaceContext namespaceContext, String path) {
		this.namespaceContext = namespaceContext;
		if (path != null) {
			this.path = new StringBuilder(path);
		} else {
			this.path = new StringBuilder();
		}
	}

	/**
	 *
	 * @param name
	 *
	 * @return
	 */
	public JXPathBuilder elementLocalName(String name) {
		path.append('*').append("[");
		localName(name);
		path.append("]");
		return this;
	}

	/**
	 *
	 * @param name
	 *
	 * @return
	 */
	public JXPathBuilder localName(String name) {
		path.append("local-name()='").append(name).append("'");
		return this;
	}

	/**
	 *
	 * @param namespaceURI
	 * @param name
	 *
	 * @return
	 */
	public JXPathBuilder nodeName(String namespaceURI, String name) {
		String prefix = namespaceContext.getPrefix(namespaceURI);
		if (prefix == null) {
			throw new NullPointerException("No prefix found for namespace \"" + namespaceURI + "\"");
		}
		path.append(prefix).append(':').append(name);
		return this;
	}

	/**
	 *
	 * @return
	 */
	public JXPathBuilder groupBegin() {
		path.append('(');
		return this;
	}

	/**
	 *
	 * @return
	 */
	public JXPathBuilder groupEnd() {
		path.append(')');
		return this;
	}

	/**
	 *
	 * @param name
	 *
	 * @return
	 */
	public JXPathBuilder attribute(String name) {
		path.append('@').append(name);
		return this;
	}

	/**
	 *
	 * @param namespaceURI
	 * @param name
	 *
	 * @return
	 */
	public JXPathBuilder attributeNS(String namespaceURI, String name) {
		String prefix = namespaceContext.getPrefix(namespaceURI);
		if (prefix == null) {
			throw new NullPointerException("No prefix found for namespace \"" + namespaceURI + "\"");
		}
		path.append('@').append(prefix).append(':').append(name);
		return this;
	}

	/**
	 *
	 * @return
	 */
	public JXPathBuilder fromRoot() {
		if (path.length() == 0 || path.charAt(0) != '/') {
			path.insert(0, "/");
		}
		return this;
	}

	/**
	 *
	 * @return
	 */
	public JXPathBuilder anyElement() {
		path.append('*');
		return this;
	}

	/**
	 *
	 * @return
	 */
	public JXPathBuilder step() {
		if (path.charAt(path.length() - 1) != '/') {
			path.append('/');
		}
		return this;
	}

	/**
	 *
	 * @return
	 */
	public JXPathBuilder first() {
		String pattern = "[1]";
		if (!pattern.equals(path.substring(path.length() - pattern.length()))) {
			path.append(pattern);
		}
		return this;
	}

	/**
	 *
	 * @return
	 */
	public JXPathBuilder last() {
		String pattern = "[length()]";
		if (!pattern.equals(path.substring(path.length() - pattern.length()))) {
			path.append(pattern);
		}
		return this;
	}

	/**
	 *
	 * @return
	 */
	public JXPathBuilder predicateBegin() {
		path.append('[');
		return this;
	}

	/**
	 *
	 * @return
	 */
	public JXPathBuilder predicateEnd() {
		path.append(']');
		return this;
	}

	public JXPathBuilder or() {
		int pLength = path.length();
		if (pLength >= 3 && !"or".equals(path.substring(pLength - 3).trim())) {
			path.append(" or ");
		}
		return this;
	}

	/**
	 *
	 * @return
	 */
	public JXPathBuilder union() {
		if (path.charAt(path.length() - 1) != '|') {
			path.append('|');
		}
		return this;
	}

	/**
	 *
	 * @param xpath
	 *
	 * @return
	 */
	public JXPathBuilder union(JXPathBuilder xpath) {
		union();
		path.append(xpath.toString());
		return this;
	}

	@Override
	public String toString() {
		return path.toString();
	}
}
