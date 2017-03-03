package com.witcraft.jjobs.context;

import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.context.StringContext;
import java.io.File;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class FileNameContext extends StringContext<File> {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public FileNameContext(Matching<File> matching, Node node) {
		super(matching, node);
	}

	@Override
	public String getContextValue(File file) {
		return file.getName();
	}
}
