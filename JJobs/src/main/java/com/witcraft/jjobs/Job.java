package com.witcraft.jjobs;

import com.witcraft.jjobs.actions.JobAction;
import com.witcraft.jjobs.helpers.JXPathBuilder;
import com.witcraft.jjobs.helpers.LogHandler;
import com.witcraft.jjobs.nodes.XmlElement;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jason Redding
 */
public class Job extends XmlElement implements Runnable {

	private static final String ATTRIBUTE_TEST = "test";
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_ASYNC = "async";
	private static final String ATTRIBUTE_DISABLED = "disabled";
	private String name;
	private LinkedHashSet<JobAction> steps;
	private final ExecutorService asyncTasks;
	private final XPath xpath;
	private File loggingDirectory;

	/**
	 *
	 * @param xpath
	 * @param node
	 */
	public Job(XPath xpath, Node node) {
		super(node);
		this.xpath = xpath;
		this.name = getAttribute(node, ATTRIBUTE_NAME);
		steps = new LinkedHashSet<>();
		asyncTasks = Executors.newWorkStealingPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));

		NodeList jobSteps = node.getChildNodes();
		boolean isDisabled;
		int childCount = jobSteps.getLength();
		for (int stepIndex = 0; stepIndex < childCount; stepIndex++) {
			Node jobStepNode = jobSteps.item(stepIndex);
			if (jobStepNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			isDisabled = getBooleanAttribute(jobStepNode, ATTRIBUTE_DISABLED, false);
			if (isDisabled) {
				continue;
			}
			if ("logging".equals(jobStepNode.getLocalName())) {
				JXPathBuilder xp = new JXPathBuilder(getXPath().getNamespaceContext());
				xp.elementLocalName("directory").first();
				try {
					Node dirNode = (Node)getXPath().evaluate(xp.toString(), jobStepNode, XPathConstants.NODE);
					if (dirNode != null) {
						File logDir = new File(dirNode.getTextContent().trim());
						if (logDir.exists()) {
							loggingDirectory = logDir;
						}
					}
				} catch (XPathExpressionException ex) {
					Main.err(ex);
				}
				continue;
			}
			addStep(JobAction.newFromNode(this, jobStepNode));
		}
		if (loggingDirectory == null) {
			loggingDirectory = new File(System.getProperty("user.dir", System.getProperty("java.io.tmpdir", "."))).getAbsoluteFile();
		}
		if (!loggingDirectory.exists()) {
			try {
				loggingDirectory.mkdirs();
			} catch (Exception ex) {
				Main.err(ex);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public final XPath getXPath() {
		return xpath;
	}

	/**
	 *
	 * @param step
	 */
	public void addStep(JobAction step) {
		if (step != null) {
			steps.add(step);
		}
	}

	/**
	 *
	 * @return
	 */
	public LinkedHashSet getSteps() {
		return steps;
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 * @return
	 */
	public File getLoggingDirectory() {
		return loggingDirectory;
	}

	/**
	 *
	 * @return
	 */
	public boolean isTest() {
		String test = getAttribute(ATTRIBUTE_TEST, null);
		if (test == null) {
			test = getAttribute(getNode().getParentNode(), ATTRIBUTE_TEST);
			if (test == null) {
				test = "0";
			}
		}
		return parseBoolean(test);
	}

	@Override
	public void run() {
		LogHandler handler = LogHandler.forJob(this);
		Main.addLogHandler(handler);
		steps.forEach(new Consumer<JobAction>() {
			@Override
			public void accept(JobAction jobStep) {
//				LOGGER.log(Level.INFO, jobStep.toString());
				Main.log(jobStep);
				boolean isAsync = jobStep.getBooleanAttribute(ATTRIBUTE_ASYNC, false);
				if (isAsync) {
					asyncTasks.submit(jobStep);
				} else {
					jobStep.run();
				}
			}
		});
		asyncTasks.shutdown();
		try {
			asyncTasks.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException ex) {
			Main.err(ex);
		}
		Main.removeLogHandler(handler);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getType());
		NamedNodeMap attrs = getAttributes();
		if (attrs.getLength() > 0) {
			sb.append(" [")
			.append(attributesToString())
			.append(']');
		}
		return sb.toString();
	}
}
