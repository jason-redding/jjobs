package com.witcraft.jjobs.actions.files;

import com.witcraft.jjobs.Job;
import com.witcraft.jjobs.Main;
import com.witcraft.jjobs.actions.JobAction;
import com.witcraft.jjobs.criteria.AbstractCriterion;
import com.witcraft.jjobs.criteria.Criterion;
import com.witcraft.jjobs.criteria.files.FileCriterion;
import com.witcraft.jjobs.helpers.JXPathBuilder;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An implementation of {@link com.witcraft.jjobs.actions.JobAction} that
 * deletes files.
 * <p>
 * Here are a few examples:
 * </p>
 * <div class="jtabs" id="jtabs-deletefiles-examples">
 * <ul>
 * <li><a href="#minimal">Minimal</a></li>
 * <li><a href="#example-1">Basic Criteria</a></li>
 * <li><a href="#example-2">Using JDBC</a></li>
 * </ul>
 * <div id="minimal">
 * <p>
 * This XML document defines a {@link com.witcraft.jjobs.Job} that will:
 * </p>
 * <ol>
 * <li>Recursively delete all files (including directories) from within
 * "/tmp".</li>
 * </ol>
 * <p>
 * </p>
 * <pre><code class="jcode-xml">{@literal <?xml version="1.0" encoding="UTF-8"?>
 *<jobs xmlns="http://witcraft.com/xsd/jobs"
 *      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *      xsi:schemaLocation="http://witcraft.com/xsd/jobs http://witcraft.com/xsd/jobs.xsd">
 *  <job name="Sample Job">
 *    <delete-files directory="/tmp" recursive="true"/>
 *  </job>
 *</jobs>}</code></pre>
 * </div>
 * <div id="example-1">
 * <p>
 * This XML document defines a {@link com.witcraft.jjobs.Job} that will:
 * </p>
 * <ol>
 * <li>Recursively delete all files (not including directories) from within
 * "/tmp" whose name starts with "jjobs-".</li>
 * </ol>
 * <p>
 * </p>
 * <pre><code class="jcode-xml">{@literal <?xml version="1.0" encoding="UTF-8"?>
 *<jobs xmlns="http://witcraft.com/xsd/jobs"
 *      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *      xsi:schemaLocation="http://witcraft.com/xsd/jobs http://witcraft.com/xsd/jobs.xsd">
 *  <job name="Sample Job">
 *    <delete-files directory="/tmp" recursive="true">
 *      <matching type="file">
 *        <name>
 *          <starts-with ignore-case="true">
 *            <value>jjobs-</value>
 *          </starts-with>
 *        </name>
 *      </matching>
 *    </delete-files>
 *  </job>
 *</jobs>}</code></pre>
 * </div>
 * <div id="example-2">
 * <p>
 * This XML document defines a {@link com.witcraft.jjobs.Job} that will:
 * </p>
 * <ol>
 * <li>Select a collection of file extensions from the database and store
 * {@link com.witcraft.jjobs.helpers.JSONResultSet} as "extensions".</li>
 * <li>Recursively delete all files (not including directories) whose name ends
 * with one of the values in collection "extensions".</li>
 * </ol>
 * <p>
 * </p>
 * <pre><code class="jcode-xml">{@literal <?xml version="1.0" encoding="UTF-8"?>
 *<jobs xmlns="http://witcraft.com/xsd/jobs"
 *      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *      xsi:schemaLocation="http://witcraft.com/xsd/jobs http://witcraft.com/xsd/jobs.xsd">
 *  <job name="Sample Job">
 *    <jdbc connect="jdbc:mysql://localhost:3306/sid" username="u321" password="a123p">
 *      <script>
 *        <sql var="extensions">
 *          select file_extension from rules;
 *        </sql>
 *      </script>
 *    </jdbc>
 *    <delete-files directory="/tmp" recursive="true">
 *      <matching type="file">
 *        <name>
 *          <ends-with ignore-case="true">
 *            <value-using result-set="extensions">$}&#123;file_extension&#125;&lt;/value-using&gt;
 *         {@literal </ends-with>
 *        </name>
 *      </matching>
 *    </delete-files>
 *  </job>
 *</jobs>}</code></pre>
 * </div>
 * </div>
 *
 * @author Jason Redding
 */
public class DeleteFilesAction extends JobAction implements FileFilter {

	private File directory;
	private boolean recursive;
	private boolean force;
	private boolean async;
	private List<FileMatching> matchings;
	private final FileVisitor<Path> REMOVE_DIRECTORY;

	/**
	 *
	 * @param job
	 * @param node
	 */
	public DeleteFilesAction(Job job, Node node) {
		super(job, node);
		this.REMOVE_DIRECTORY = new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (!DeleteFilesAction.this.isTest()) {
				} else {
					return FileVisitResult.CONTINUE;
				}
				try {
					Files.delete(file);
				} catch (Exception ex) {
					Main.err("Failed to delete file \"%s\"", ex, file.normalize().toAbsolutePath());
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException ex) throws IOException {
				Main.err("Failed to reference file \"%s\"", ex, file.normalize().toAbsolutePath());
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException vex) throws IOException {
				if (vex != null) {
					Main.err("Failure ocurred within directory \"%s\"", vex, dir.normalize().toAbsolutePath());
				}
				if (DeleteFilesAction.this.isTest()) {
					return FileVisitResult.CONTINUE;
				}
				try {
					Files.delete(dir);
				} catch (Exception ex) {
					Main.err("Failed to delete directory \"%s\"", ex, dir.normalize().toAbsolutePath());
				}
				return FileVisitResult.CONTINUE;
			}
		};
		String directoryAttribute = getAttribute("directory", "");
		if (directoryAttribute.trim().length() == 0) {
			throw new NullPointerException("<" + getType() + "> is missing required attribute \"directory\".");
		}
		recursive = getBooleanAttribute("recursive", false);
		force = getBooleanAttribute("force", false);
		async = getBooleanAttribute("async", false);
		directory = new File(directoryAttribute);
		NodeList nodes = node.getChildNodes();
		for (int ni = 0; ni < nodes.getLength(); ni++) {
			Node n = nodes.item(ni);
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			boolean isDisabled = getBooleanAttribute(n, "disabled", false);
			if (isDisabled) {
				continue;
			}
			if ("matching".equals(n.getLocalName())) {
				FileMatching m = this.newMatchingFromNode(n);
				try {
					JXPathBuilder xpath = new JXPathBuilder(getXPath().getNamespaceContext());
//					xpath
//					.elementLocalName("name").step().elementLocalName("equals")
//					.union()
//					.elementLocalName("name").step().elementLocalName("starts-with")
//					.union()
//					.elementLocalName("name").step().elementLocalName("ends-with")
//					.union()
//					.elementLocalName("name").step().elementLocalName("matches").step().elementLocalName("pattern")
//					.union()
//					.elementLocalName("name").step().elementLocalName("matches").step().elementLocalName("regex")
//					.union()
//					.elementLocalName("path").step().elementLocalName("equals")
//					.union()
//					.elementLocalName("path").step().elementLocalName("starts-with")
//					.union()
//					.elementLocalName("path").step().elementLocalName("ends-with")
//					.union()
//					.elementLocalName("path").step().elementLocalName("matches").step().elementLocalName("pattern")
//					.union()
//					.elementLocalName("path").step().elementLocalName("matches").step().elementLocalName("regex")
//					.union()
//					.elementLocalName("owner").step().elementLocalName("equals")
//					.union()
//					.elementLocalName("owner").step().elementLocalName("starts-with")
//					.union()
//					.elementLocalName("owner").step().elementLocalName("ends-with")
//					.union()
//					.elementLocalName("owner").step().elementLocalName("matches").step().elementLocalName("pattern")
//					.union()
//					.elementLocalName("owner").step().elementLocalName("matches").step().elementLocalName("regex")
//					.union()
//					.elementLocalName("date-modified").step().elementLocalName("before")
//					.union()
//					.elementLocalName("date-modified").step().elementLocalName("after")
//					.union()
//					.elementLocalName("date-created").step().elementLocalName("before")
//					.union()
//					.elementLocalName("date-created").step().elementLocalName("after")
//					.union()
//					.elementLocalName("size").step().elementLocalName("min")
//					.union()
//					.elementLocalName("size").step().elementLocalName("max");

					xpath
					.anyElement().predicateBegin().localName("name").or().localName("path").or().localName("owner").predicateEnd()
					.step()
					.anyElement().predicateBegin().localName("equals").or().localName("starts-with").or().localName("ends-with").predicateEnd()
					.union()
					.anyElement().predicateBegin().localName("name").or().localName("path").or().localName("owner").predicateEnd()
					.step()
					.elementLocalName("matches")
					.step()
					.anyElement().predicateBegin().localName("pattern").or().localName("regex").predicateEnd()
					.union()
					.anyElement().predicateBegin().localName("date-created").or().localName("date-modified").predicateEnd()
					.step()
					.anyElement().predicateBegin().localName("after").or().localName("before").predicateEnd()
					.union()
					.elementLocalName("size")
					.step()
					.anyElement().predicateBegin().localName("min").or().localName("max").predicateEnd();

					NodeList matcherNodes = (NodeList)getXPath().evaluate(xpath.toString(), n, XPathConstants.NODESET);
					for (int mi = 0; mi < matcherNodes.getLength(); mi++) {
						Node cNode = matcherNodes.item(mi);
						if (cNode.getNodeType() != Node.ELEMENT_NODE) {
							continue;
						}
						AbstractCriterion c = AbstractCriterion.newFromNode(m, cNode);
						if (c instanceof FileCriterion) {
							m.addCriterion((FileCriterion)c);
						}
					}
					getMatchings().add(m);
				} catch (XPathExpressionException ex) {
					Main.err(ex);
				}
			}
		}
	}

	/**
	 *
	 * @return Returns true if this action will <strong>recursively</strong>
	 * delete files within
	 * {@link com.witcraft.jjobs.actions.files.DeleteFilesAction#directory}.
	 */
	public boolean isRecursive() {
		return recursive;
	}

	/**
	 *
	 * @return
	 */
	public boolean isForce() {
		return force;
	}

	/**
	 *
	 * @return
	 */
	public boolean isAsync() {
		return async;
	}

	/**
	 *
	 * @return
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 *
	 * @return
	 */
	public final List<FileMatching> getMatchings() {
		if (matchings == null) {
			matchings = new ArrayList<>();
		}
		return matchings;
	}

	/**
	 *
	 * @return
	 */
	public boolean hasCriteria() {
		return !matchings.isEmpty();
	}

	@Override
	public boolean accept(File file) {
		for (FileMatching matching : getMatchings()) {
			if (matching.matches(file)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 */
	@Override
	public void run() {
		File baseDirectory = getDirectory();
		if (!baseDirectory.exists()) {
//			LOGGER.log(Level.WARNING, "Directory \"{0}\" does not exist.", new Object[] {baseDirectory.getAbsolutePath()});
			Main.err("Directory \"%s\" does not exist.", baseDirectory.getAbsolutePath());
			return;
		}

		try {
			//<editor-fold defaultstate="collapsed" desc="Process FileTree">
			Files.walkFileTree(baseDirectory.toPath(), new FileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					boolean isRecursive = isRecursive();
					if (isRecursive || Files.isSameFile(dir, baseDirectory.toPath())) {
						return FileVisitResult.CONTINUE;
					}
					if (!isRecursive) {
						boolean processFile = accept(dir.toFile());
						if (processFile) {
							deleteFile(dir, force);
						}
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					boolean processFile = accept(file.toFile());
					if (processFile) {
						deleteFile(file, force);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException ex) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
					if (isRecursive() && !Files.isSameFile(dir, baseDirectory.toPath())) {
						boolean processFile = accept(dir.toFile());
						if (processFile) {
							deleteFile(dir, force);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
			//</editor-fold>
		} catch (IOException ex) {
			Main.err(ex);
		}
	}

	private void deleteFile(Path file, boolean force) {
		boolean isDir = Files.isDirectory(file);
		try {
			if (force && isDir) {
//				LOGGER.log(Level.INFO, "Force-deleting directory \"{0}\"", file.normalize().toAbsolutePath());
				Main.log("  = Force-deleting directory \"%s\"", file.normalize().toAbsolutePath());
				Files.walkFileTree(file, REMOVE_DIRECTORY);
			} else {
//				LOGGER.log(Level.INFO, "Deleting {1} \"{0}\"", new Object[] {file.normalize().toAbsolutePath(), (isDir ? "directory" : "file")});
				Main.log("  = Deleting %s \"%s\"", (isDir ? "directory" : "file"), file.normalize().toAbsolutePath());
				if (DeleteFilesAction.this.isTest()) {
					return;
				}
				Files.delete(file);
			}
		} catch (Exception ex) {
			Main.err("  = Failed to delete %s \"%s\"", ex, (isDir ? "directory" : "file"), file.normalize().toAbsolutePath());
		}
	}

	/**
	 *
	 * @param node
	 *
	 * @return
	 */
	public final FileMatching newMatchingFromNode(Node node) {
		return new FileMatching(this, node);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		List<FileMatching> allMatchings = getMatchings();
		int validMatchingCount = 0;
		for (int mi = 0; mi < allMatchings.size(); mi++) {
			FileMatching matching = allMatchings.get(mi);
			FileMatching.FileType fType = matching.getFileType();
			sb.append("\n    ");
			if (validMatchingCount > 0) {
				sb.append("OR\n    ");
			}
			sb.append("where [type=\"").append(fType.name().toLowerCase()).append("\"]");
			for (Criterion<File> c : matching.getCriteria()) {
				sb.append("\n        ").append(c.toString());
			}
			validMatchingCount++;
		}
		return sb.toString();
	}
}
