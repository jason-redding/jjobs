package com.witcraft.jjobs.actions.files;

import com.witcraft.jjobs.actions.JobAction;
import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import org.w3c.dom.Node;

/**
 * Represents a matching group containing a list of
 * {@link com.witcraft.jjobs.criteria.Criterion} objects. This matching group
 * will be logically OR'd with other matching groups within the same
 * {@link com.witcraft.jjobs.actions.JobAction}.
 */
public class FileMatching extends Matching<File> {

	private final FileType fileType;

	/**
	 * Constructs a new {@link FileMatching} belonging to the specified
	 * {@code jobAction} and bound to the specified {@code node}.
	 *
	 * @param jobAction the job action to which this matching group will belong.
	 * @param node the node to which this matching group is bound.
	 */
	public FileMatching(JobAction jobAction, Node node) {
		super(jobAction, node);
		fileType = FileType.valueOf(getAttribute("type", FileType.ANY.name()).toUpperCase());
	}

	/**
	 * Returns the filtering file type this matching group will match.
	 *
	 * @return the filtering file type this matching group will match.
	 */
	public FileType getFileType() {
		return fileType;
	}

	@Override
	public boolean matches(File file) {
		while (true) {
			if (FileType.ANY.equals(fileType) && (file.isFile() || file.isDirectory())) {
				break;
			} else if (FileType.FILE.equals(fileType) && !file.isFile()) {
				return false;
			} else if (FileType.DIRECTORY.equals(fileType) && !file.isDirectory()) {
				return false;
			}
			break;
		}
		return super.matches(file);
	}

	/**
	 * Enumeration of the different file type options used by {@link FileMatching}
	 * for filtering.
	 * <p>
	 * A filtering file type can be one of the following:
	 * <ul>
	 * <li>{@link #FILE}<br>Type representing regular files.</li>
	 * <li>{@link #DIRECTORY}<br>Type representing directories.</li>
	 * <li>{@link #ANY}<br>Type representing files of any type.</li>
	 * </ul>
	 *
	 * @author Jason Redding
	 */
	public static enum FileType {
		/**
		 * Type for files.
		 */
		FILE,
		/**
		 * Type for directories.
		 */
		DIRECTORY,
		/**
		 * Special type for both files and directories.
		 */
		ANY
	}

}
