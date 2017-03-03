package com.witcraft.jjobs.context;

import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class FileDateModifiedContext extends DateContext<File> {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public FileDateModifiedContext(Matching<File> matching, Node node) {
		super(matching, node);
	}

	@Override
	public LocalDateTime getContextValue(File file, ZoneOffset offset) {
		return getFileDateModified(file, offset);
	}
}
