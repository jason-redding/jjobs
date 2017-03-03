package com.witcraft.jjobs.nodes;

import org.w3c.dom.Node;

/**
 *
 * @author
 */
public abstract class XmlElementWithContent extends XmlElement {

	private final String content;

	/**
	 *
	 * @param node
	 */
	protected XmlElementWithContent(Node node) {
		super(node);
		this.content = node.getTextContent().trim();
	}

	/**
	 *
	 * @return
	 */
	public String getContent() {
		return content;
	}
}
