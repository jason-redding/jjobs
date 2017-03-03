package com.witcraft.jjobs.context;

import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import java.math.BigDecimal;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class FileSizeContext extends NumberContext<File> {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public FileSizeContext(Matching<File> matching, Node node) {
		super(matching, node);
	}

	@Override
	public BigDecimal getContextValue(File file) {
//		return BigDecimal.valueOf(file.isDirectory() ? 0 : file.length());
		return (file.isDirectory() ? null : BigDecimal.valueOf(file.length()));
	}
}
