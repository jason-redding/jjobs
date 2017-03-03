package com.witcraft.jjobs.nodes;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public abstract class XmlElement {

	private Node node;

	/**
	 *
	 * @param node
	 */
	protected XmlElement(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException("Node \"" + node.getNodeName() + "\" should be of type ELEMENT");
		}
		this.node = node;
	}

	/**
	 *
	 * @return
	 */
	public final String getType() {
		return node.getLocalName();
	}

	/**
	 *
	 * @return
	 */
	public final Node getNode() {
		return node;
	}

	/**
	 *
	 * @param exceptions
	 *
	 * @return
	 */
	public final String attributesToString(Set<String> exceptions) {
		return attributesToString(getAttributes(), exceptions);
	}

	/**
	 *
	 * @param exceptions
	 *
	 * @return
	 */
	public final String attributesToString(String... exceptions) {
		return attributesToString((String)null, exceptions);
	}

	/**
	 *
	 * @param exception
	 * @param exceptions
	 *
	 * @return
	 */
	public String attributesToString(String exception, String... exceptions) {
		return attributesToString(getAttributes(), exception, exceptions);
	}

	/**
	 *
	 * @param attributes
	 *
	 * @return
	 */
	protected static final String attributesToString(NamedNodeMap attributes) {
		return attributesToString(attributes, null, (String)null);
	}

	/**
	 *
	 * @param attributes
	 * @param exceptions
	 *
	 * @return
	 */
	protected static final String attributesToString(NamedNodeMap attributes, Set<String> exceptions) {
		return attributesToString(attributes, exceptions.toArray(new String[0]));
	}

	/**
	 *
	 * @param attributes
	 * @param exception
	 *
	 * @return
	 */
	protected static final String attributesToString(NamedNodeMap attributes, String exception) {
		return attributesToString(attributes, exception, (String[])null);
	}

	/**
	 *
	 * @param attributes
	 * @param exceptions
	 *
	 * @return
	 */
	protected static final String attributesToString(NamedNodeMap attributes, String[] exceptions) {
		return attributesToString(attributes, null, exceptions);
	}

	/**
	 *
	 * @param attributes
	 * @param exception
	 * @param exceptions
	 *
	 * @return
	 */
	protected static final String attributesToString(NamedNodeMap attributes, String exception, String... exceptions) {
		StringBuilder sb = new StringBuilder();
		if (attributes != null) {
			if (exceptions != null && exceptions.length > 1) {
				Arrays.parallelSort(exceptions);
			}
			int usedCount = 0;
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attr = attributes.item(i);
				String attrName = attr.getNodeName();
				if (exception != null && exception.equals(attrName)) {
					continue;
				}
				if (exceptions != null && exceptions.length > 0) {
					boolean containsAttributeName = Arrays.stream(exceptions).anyMatch(new Predicate<String>() {
						@Override
						public boolean test(String value) {
							return attrName.equals(value);
						}
					});
					if (containsAttributeName) {
						continue;
					}
				}
				if (usedCount > 0) {
					sb.append(", ");
				}
				sb.append(attrName)
				.append("=\"")
				.append(attr.getNodeValue())
				.append("\"");
				usedCount++;
			}
		}
		return sb.toString();
	}

	/**
	 *
	 * @param node
	 *
	 * @return
	 */
	public static final String getFullName(Node node) {
		return getNameUntil(node, (Node)null);
	}

	/**
	 *
	 * @param node
	 * @param until
	 *
	 * @return
	 */
	public static final String getNameUntil(Node node, Node until) {
		StringBuilder sb = new StringBuilder();
		Node n = node;
		String name;
		while (n != null && !n.equals(until)) {
			name = n.getLocalName();
			if (sb.length() > 0) {
				sb.insert(0, "/");
			}
			sb.insert(0, name);
			n = n.getParentNode();
		}
		return sb.toString();
	}

	/**
	 *
	 * @param node
	 * @param until
	 *
	 * @return
	 */
	public static final String getNameUntil(Node node, String until) {
		StringBuilder sb = new StringBuilder();
		Node n = node;
		String name;
		while (n != null) {
			name = n.getLocalName();
			if (name.equals(until)) {
				break;
			}
			if (sb.length() > 0) {
				sb.insert(0, "/");
			}
			sb.insert(0, name);
			n = n.getParentNode();
		}
		return sb.toString();
	}

	/**
	 *
	 * @return
	 */
	public final NamedNodeMap getAttributes() {
		return node.getAttributes();
	}

	/**
	 * Returns the value for the specified attribute name, null if the attribute
	 * doesn't exist.
	 *
	 * @param name
	 *
	 * @return
	 */
	public final String getAttribute(String name) {
		return getAttribute(node, name);
	}

	/**
	 *
	 * @param name
	 * @param defaultValue
	 *
	 * @return
	 */
	public final String getAttribute(String name, String defaultValue) {
		return getAttribute(node, name, defaultValue);
	}

	/**
	 *
	 * @param name
	 *
	 * @return
	 */
	public final boolean hasAttribute(String name) {
		return (hasAttribute(node, name));
	}

	/**
	 *
	 * @param node
	 * @param name
	 *
	 * @return
	 */
	public static final String getAttribute(Node node, String name) {
		return getAttribute(node, name, null);
	}

	/**
	 *
	 * @param node
	 * @param name
	 *
	 * @return
	 */
	public static final boolean hasAttribute(Node node, String name) {
		return (node.getAttributes().getNamedItem(name) != null);
	}

	/**
	 *
	 * @param node
	 * @param name
	 * @param defaultValue
	 *
	 * @return
	 */
	public static final String getAttribute(Node node, String name, String defaultValue) {
		if (node != null) {
			Node attr = node.getAttributes().getNamedItem(name);
			if (attr != null) {
				return attr.getNodeValue();
			}
		}
		return defaultValue;
	}

	/**
	 *
	 * @param name
	 *
	 * @return
	 */
	public final Boolean getBooleanAttribute(String name) {
		return getBooleanAttribute(node, name);
	}

	/**
	 *
	 * @param name
	 * @param defaultValue
	 *
	 * @return
	 */
	public final Boolean getBooleanAttribute(String name, boolean defaultValue) {
		return getBooleanAttribute(node, name, defaultValue);
	}

	/**
	 *
	 * @param node
	 * @param name
	 *
	 * @return
	 */
	public static final Boolean getBooleanAttribute(Node node, String name) {
		return parseBoolean(getAttribute(node, name));
	}

	/**
	 *
	 * @param node
	 * @param name
	 * @param defaultValue
	 *
	 * @return
	 */
	public static final Boolean getBooleanAttribute(Node node, String name, boolean defaultValue) {
		return parseBoolean(getAttribute(node, name, Boolean.toString(defaultValue)));
	}

	/**
	 * Returns a Boolean object from the given value.
	 * <p>
	 * Returns TRUE only if the given value is non-null and
	 * <code>value.toLowerCase()</code> is equal to any of: "true", "yes", "on",
	 * or "1". For any non-null value that doesn't match above, returns FALSE.
	 * Otherwise, returns null.
	 *
	 * @param value
	 *
	 * @return
	 */
	public static final Boolean parseBoolean(String value) {
		if (value != null) {
			switch (value.toLowerCase()) {
			case "1":
			case "true":
			case "yes":
			case "on":
				return Boolean.TRUE;
			default:
				return Boolean.FALSE;
			}
		}
		return null;
	}
}
