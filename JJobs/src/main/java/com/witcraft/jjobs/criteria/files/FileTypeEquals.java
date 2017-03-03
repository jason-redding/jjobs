package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.actions.Matching;
import com.witcraft.jjobs.actions.files.FileMatching;
import com.witcraft.jjobs.criteria.AbstractCriterion;
import java.io.File;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class FileTypeEquals extends AbstractCriterion<File> implements FileCriterion {

	private final FileMatching.FileType fileType;

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public FileTypeEquals(Matching<File> matching, Node node) {
		super(matching, node);
		String c = getContent();
		fileType = (c == null || c.trim().length() == 0 ? FileMatching.FileType.ANY : FileMatching.FileType.valueOf(c.toUpperCase()));
	}

	/**
	 *
	 * @param file
	 *
	 * @return
	 */
	@Override
	protected boolean isMatch(File file) {
		if (FileMatching.FileType.ANY == fileType && (file.isDirectory() || file.isFile())) {
			return true;
		} else if (FileMatching.FileType.DIRECTORY == fileType && file.isDirectory()) {
			return true;
		} else if (FileMatching.FileType.FILE == fileType && file.isFile()) {
			return true;
		}
		return false;
	}

	@Override
	public String expression(File file) {
		boolean isMatched = isMatch(file);
		StringBuilder sb = new StringBuilder();
		sb.append(isMatched ? '+' : '-');
		sb.append('[').append(getFullName()).append("] ");
		sb.append(toString());
//		if (isMatch(file)) {
//			return sb.toString();
//		}
//		sb.append(" is false");
		return sb.toString();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String valueToString() {
		return fileType.name().toLowerCase();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getType());
		sb.append(" = ").append(valueToString());
		return sb.toString();
	}

}
