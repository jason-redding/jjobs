package com.witcraft.jjobs.context;

import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.nodes.XmlElement;
import static com.witcraft.jjobs.nodes.XmlElement.getNameUntil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 * @param <F>
 * @param <T>
 */
public abstract class CriteriaContext<F, T> extends XmlElement {

	private static final HashMap<String, Class<? extends CriteriaContext>> REGISTERED_CONTEXTS = new HashMap<>();

	/**
	 *
	 * @param matching
	 * @param node
	 *
	 * @return
	 */
	public static CriteriaContext newFromNode(Matching matching, Node node) {
		Class<? extends CriteriaContext> clazz = REGISTERED_CONTEXTS.get(getNameUntil(node, matching.getNode()));
		if (clazz != null) {
			try {
				Constructor<? extends CriteriaContext> init = clazz.getConstructor(Matching.class, Node.class);
				return init.newInstance(matching, node);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				Main.err("Failed to instantiate CriteriaContext from XML Node <%s>", ex, node.getNodeName());
			}
		}
		return null;
	}

	/**
	 *
	 * @param name
	 * @param clazz
	 */
	public static final void register(String name, Class<? extends CriteriaContext> clazz) {
		REGISTERED_CONTEXTS.put(name, clazz);
	}

	private final Matching<F> matching;

	/**
	 *
	 * @param matching
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	public CriteriaContext(Matching matching, Node node) {
		super(node);
		this.matching = matching;
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		return getNode().getLocalName();
	}

	/**
	 *
	 * @param context
	 *
	 * @return
	 */
	public abstract T getContextValue(F context);

}
