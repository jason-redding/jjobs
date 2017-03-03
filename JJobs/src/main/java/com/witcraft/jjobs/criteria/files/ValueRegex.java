package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jason Redding
 */
public class ValueRegex extends RegexCriterion {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public ValueRegex(Matching<File> matching, Node node) {
		super(matching, node);
		NodeList children = getNode().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			String nodeName = child.getLocalName();
			switch (nodeName) {
			case "flags":
				NodeList flagNodes = child.getChildNodes();
				for (int fi = 0; fi < flagNodes.getLength(); fi++) {
					Node flagNode = flagNodes.item(fi);
					if (flagNode.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					if (!"flag".equals(flagNode.getLocalName())) {
						continue;
					}
					boolean flagState = getBooleanAttribute(flagNode, "state", true);
					String flag = getAttribute(flagNode, "name");
					setFlag(flag, flagState);
				}
				break;
			case "pattern":
				setPatternText(child.getTextContent().trim());
				break;
			default:
				break;
			}
		}
	}
}
