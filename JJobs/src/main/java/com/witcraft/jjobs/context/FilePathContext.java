package com.witcraft.jjobs.context;

import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import java.nio.file.Path;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class FilePathContext extends StringContext<File> {

	private final PathOf of;

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public FilePathContext(Matching<File> matching, Node node) {
		super(matching, node);
		this.of = PathOf.valueOf(getAttribute("of", PathOf.FILE.name()).toUpperCase());
	}

	@Override
	public String getContextValue(File file) {
		Path path = file.toPath();
		if (PathOf.DIRECTORY.equals(of)) {
			path = path.getParent();
		}
		return path.normalize().toAbsolutePath().toString();
	}

	/**
	 *
	 * @return
	 */
	public PathOf getOf() {
		return of;
	}

	/**
	 * Enumeration of choices for how much of this path to expose to associated
	 * Criteria.
	 */
	public static enum PathOf {

		/**
		 *
		 */
		FILE,
		/**
		 *
		 */
		DIRECTORY;
	}
}
