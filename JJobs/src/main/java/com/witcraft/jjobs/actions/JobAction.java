package com.witcraft.jjobs.actions;

import com.witcraft.jjobs.Job;
import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.nodes.XmlElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import javax.xml.xpath.XPath;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public abstract class JobAction extends XmlElement implements Runnable {

	private final Job job;
	private static final HashMap<String, Class<? extends JobAction>> REGISTERED_JOB_CLASSES = new HashMap<>();

	/**
	 *
	 * @param job
	 * @param node
	 */
	protected JobAction(Job job, Node node) {
		super(node);
		this.job = job;
	}

	/**
	 *
	 * @return
	 */
	public final XPath getXPath() {
		return job.getXPath();
	}

	/**
	 *
	 * @return
	 */
	public Job getJob() {
		return job;
	}

	/**
	 *
	 * @return
	 */
	public boolean isTest() {
		String test = getAttribute("test", null);
		if (test == null) {
			return getJob().isTest();
		}
		return parseBoolean(test);
	}

	/**
	 *
	 * @param type
	 * @param clazz
	 */
	public static final void register(String type, Class<? extends JobAction> clazz) {
		REGISTERED_JOB_CLASSES.put(type, clazz);
	}

	/**
	 *
	 * @param job
	 * @param node
	 *
	 * @return
	 */
	public static final JobAction newFromNode(Job job, Node node) {
		Class<? extends JobAction> clazz = REGISTERED_JOB_CLASSES.get(node.getLocalName());
		if (clazz != null) {
			try {
				Constructor<? extends JobAction> init = clazz.getConstructor(Job.class, Node.class);
				return init.newInstance(job, node);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				Main.err("Failed to construct new JobAction from Node.", ex);
			}
		}
		return null;
	}

	/**
	 *
	 */
	@Override
	public abstract void run();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getType());
		NamedNodeMap attrs = getAttributes();
		if (attrs.getLength() > 0) {
			sb.append(" [")
			.append(attributesToString())
			.append("]");
		}
		return sb.toString();
	}
}
