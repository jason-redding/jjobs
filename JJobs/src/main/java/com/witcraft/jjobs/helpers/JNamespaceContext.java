package com.witcraft.jjobs.helpers;

import com.witcraft.jjobs.Main;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author god
 */
public final class JNamespaceContext implements NamespaceContext {

	/**
	 *
	 */
	public static final String XSI_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";

	/**
	 *
	 */
	public static final String XSL_NS_URI = "http://www.w3.org/1999/XSL/Transform";

	/**
	 *
	 */
	public static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";

	/**
	 *
	 */
	public static final String ATOM_NS_URI = "http://www.w3.org/2005/Atom";

	/**
	 *
	 */
	public static final String XINCLUDE_NS_URI = "http://www.w3.org/2001/XInclude";
	private final HashMap<String, String> mapPrefixes;
	private final HashMap<String, HashSet<String>> mapNamespaces;
	private final Iterator EMPTY_ITERATOR = new Iterator() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Object next() {
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * Constructs a new {@link javax.xml.namespace.NamespaceContext} object with
	 * common namespaces already added.
	 */
	public JNamespaceContext() {
		this(true);
	}

	/**
	 * Constructs a new {@link javax.xml.namespace.NamespaceContext} object
	 * optionally with common namespace prefixes.
	 *
	 * @param addCommon specifies whether or not to add common namespace prefixes.
	 */
	public JNamespaceContext(boolean addCommon) {
		mapPrefixes = new HashMap<>(13);
		mapNamespaces = new HashMap<>(13);

//		addNamespace(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
		addNamespace(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
		addNamespace(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		if (addCommon) {
			addCommonPrefixes();
		}
	}

	/**
	 * Adds common namespace prefixes to this {@link JNamespaceContext}.
	 * <p>
	 * Specifically, adds:
	 * <ul>
	 * <li>{@code xmlns:xhtml="http://www.w3.org/1999/xhtml"}</li>
	 * <li>{@code xmlns:xsl="http://www.w3.org/1999/XSL/Transform"}</li>
	 * <li>{@code xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"}</li>
	 * <li>{@code xmlns:xi="http://www.w3.org/2001/XInclude"}</li>
	 * <li>{@code xmlns:a="http://www.w3.org/2005/Atom"}</li>
	 * </ul>
	 */
	public void addCommonPrefixes() {
		addNamespace("xhtml", XHTML_NS_URI);
		addNamespace("xsl", XSL_NS_URI);
		addNamespace("xsi", XSI_NS_URI);
		addNamespace("xi", XINCLUDE_NS_URI);
		addNamespace("a", ATOM_NS_URI);
	}

	/**
	 *
	 * @param prefix
	 * @param namespaceURI
	 */
	public void addNamespace(String prefix, String namespaceURI) {
		// check for existing prefix-to-namespace mapping, then remove the prefix from the namespace-to-prefixes mapping
		String oldNamespaceURI = mapPrefixes.get(prefix);
		if (oldNamespaceURI != null && !oldNamespaceURI.equals(namespaceURI)) {
			HashSet<String> nsList = mapNamespaces.get(oldNamespaceURI);
			nsList.remove(prefix);
		}

		mapPrefixes.put(prefix, namespaceURI);
		HashSet<String> prefixList;
		if (mapNamespaces.containsKey(namespaceURI)) {
			prefixList = mapNamespaces.get(namespaceURI);
		} else {
			prefixList = new HashSet<>(3);
			mapNamespaces.put(namespaceURI, prefixList);
		}
		prefixList.add(prefix);
	}

	/**
	 *
	 * @param namespaceURI
	 */
	public void removeNamespace(String namespaceURI) {
		HashSet<String> prefixList = mapNamespaces.get(namespaceURI);
		for (String prefix : prefixList) {
			mapPrefixes.remove(prefix);
		}
		mapNamespaces.remove(namespaceURI);
	}

	/**
	 *
	 * @param prefix
	 */
	public void removePrefix(String prefix) {
		String uri = mapPrefixes.get(prefix);
		mapPrefixes.remove(prefix);
		HashSet<String> nsList = mapNamespaces.get(uri);
		nsList.remove(prefix);
	}

	/**
	 *
	 * @param document
	 *
	 * @return
	 */
	public int addNamespaces(Document document) {
		if (document == null) {
			return 0;
		}
		NodeList elementList = document.getElementsByTagName("*");
		int elementCount = elementList.getLength();
		int addCount = 0;
		for (int i = 0; i < elementCount; i++) {
			if (addNamespaces((Element)elementList.item(i))) {
				addCount++;
			}
		}
		return addCount;
	}

	private boolean addNamespaces(Element element) {
		if (element == null) {
			return false;
		}
		return addNamespaces(element.getAttributes());
	}

	private boolean addNamespaces(NamedNodeMap attrs) {
		if (attrs == null) {
			return false;
		}
		int attrCount = attrs.getLength();
		boolean isAddingNS = false;
		for (int i = 0; i < attrCount; i++) {
			if (addNamespaces((Attr)attrs.item(i)) && isAddingNS == false) {
				isAddingNS = true;
			}
		}
		return isAddingNS;
	}

	private boolean addNamespaces(Attr attr) {
		if (attr == null) {
			return false;
		}
		String prefix = attr.getPrefix();
		String localName = attr.getLocalName();
		String value = attr.getValue();
		boolean isAddingNS = false;
		if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
			addNamespace(localName, value);
			isAddingNS = true;
		} else if (prefix == null) {
			if (XMLConstants.XMLNS_ATTRIBUTE.equals(localName)) {
				addNamespace(XMLConstants.DEFAULT_NS_PREFIX, value);
				isAddingNS = true;
			}
		}
		return isAddingNS;
	}

	/**
	 *
	 * @param document
	 */
	public void applyNamespaces(Document document) {
		Element root = document.getDocumentElement();
		// {"http://www.w3.org/2000/xmlns/"=["xmlns"], ""=[], "http://www.w3.org/2005/Atom"=["a"], "http://www.w3.org/1999/xhtml"=["h", "xhtml"], "http://www.w3.org/2001/XInclude"=["xi"], "http://acalog.com/catalog/1.0"=["", "c"], "http://www.w3.org/1999/XSL/Transform"=["xsl"], "http://www.w3.org/XML/1998/namespace"=["xml"]}
		for (Entry<String, HashSet<String>> entry : mapNamespaces.entrySet()) {
			String nsURI = entry.getKey();
			HashSet<String> prefixList = entry.getValue();
			for (String prefix : prefixList) {
				try {
					if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
						root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix, nsURI);
					} else if (prefix == null || prefix.length() == 0) {
						root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, nsURI);
					} else {
						root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + prefix, nsURI);
					}
				} catch (Exception ex) {
					Main.err(ex);
				}
			}
		}

	}

	@Override
	public String getNamespaceURI(String prefix) {
		String uri = mapPrefixes.get(prefix);
		if (uri == null) {
			uri = XMLConstants.NULL_NS_URI;
		}
		return uri;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (mapNamespaces.containsKey(namespaceURI)) {
			HashSet<String> nsList = mapNamespaces.get(namespaceURI);
			return (String)nsList.toArray()[nsList.size() - 1];
		}
		return null;
	}

	@Override
	public Iterator getPrefixes(String namespaceURI) {
		if (mapNamespaces.containsKey(namespaceURI)) {
			HashSet<String> nsList = mapNamespaces.get(namespaceURI);
			return nsList.iterator();
		}
		return EMPTY_ITERATOR;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		StringBuilder psb = new StringBuilder();
		Set<Entry<String, HashSet<String>>> nsList = mapNamespaces.entrySet();
		for (Entry<String, HashSet<String>> entry : nsList) {
			HashSet<String> prefixList = entry.getValue();
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append('"');
			sb.append(entry.getKey());
			sb.append("\"=[");
			psb.delete(0, psb.length());
			for (String prefix : prefixList) {
				if (psb.length() > 0) {
					psb.append(", ");
				}
				psb.append('"');
				psb.append(prefix);
				psb.append('"');
			}
			sb.append(psb);
			sb.append(']');
		}
		sb.insert(0, '{');
		sb.append('}');
		return sb.toString();
	}
}
