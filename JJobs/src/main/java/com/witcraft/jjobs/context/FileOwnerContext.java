package com.witcraft.jjobs.context;

import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.actions.files.FileMatching;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class FileOwnerContext extends StringContext<File> {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public FileOwnerContext(Matching<File> matching, Node node) {
		super(matching, node);
	}

	@Override
	public String getContextValue(File file) {
		Path path = file.toPath();
		try {
			return Files.getOwner(path).getName();
		} catch (IOException ex) {
			Logger.getLogger(FileMatching.class.getName()).log(Level.SEVERE, null, ex);
		}
		return "";
	}
}
