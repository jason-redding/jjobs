package com.witcraft.jjobs;

import com.witcraft.jjobs.actions.JobAction;
import com.witcraft.jjobs.actions.db.JdbcAction;
import com.witcraft.jjobs.actions.files.DeleteFilesAction;
import com.witcraft.jjobs.context.CriteriaContext;
import com.witcraft.jjobs.context.FileDateCreatedContext;
import com.witcraft.jjobs.context.FileDateModifiedContext;
import com.witcraft.jjobs.context.FileNameContext;
import com.witcraft.jjobs.context.FileOwnerContext;
import com.witcraft.jjobs.context.FilePathContext;
import com.witcraft.jjobs.context.FileSizeContext;
import com.witcraft.jjobs.criteria.AbstractCriterion;
import com.witcraft.jjobs.criteria.files.DateAfter;
import com.witcraft.jjobs.criteria.files.DateBefore;
import com.witcraft.jjobs.criteria.files.MaxSize;
import com.witcraft.jjobs.criteria.files.MinSize;
import com.witcraft.jjobs.criteria.files.ValueEndsWith;
import com.witcraft.jjobs.criteria.files.ValueEquals;
import com.witcraft.jjobs.criteria.files.ValuePattern;
import com.witcraft.jjobs.criteria.files.ValueRegex;
import com.witcraft.jjobs.criteria.files.ValueStartsWith;
import com.witcraft.jjobs.helpers.JNamespaceContext;
import com.witcraft.jjobs.helpers.JXPathBuilder;
import com.witcraft.jjobs.helpers.JXPathVariableResolver;
import com.witcraft.jjobs.helpers.LogHandler;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Jason Redding
 */
public class Main {

//	private static final Option OPTION_VERBOSE = Option.builder("v").longOpt("verbose").hasArg(true).argName("LEVEL").desc("The minimum logging level to enable. Can be an integer between 0-7 (" + Arrays.toString(LogLevel.values()).replaceAll("(^\\[|\\]$)", "") + ", respectively). 0 turns off logging.").build();
	private static final Option OPTION_JOBS = Option.builder("j").longOpt("jobs").hasArg(true).argName("FILE").desc("The XML file from which to load jobs.").build();
	private static final Option OPTION_HELP = Option.builder("h").longOpt("help").desc("Show usage help.").build();
	private static final Options OPTIONS = new Options().addOption(OPTION_JOBS).addOption(OPTION_HELP);
	private static final List<LogHandler> logHandlers = new ArrayList<>();
	private static final Pattern PATTERN_PROPERTY = Pattern.compile("\\$\\{([a-z]([a-z._-])*)+\\}");
	private static final HashMap<String, Object> globalVariables;

	/**
	 * Constant URI for the JAXP Schema Language
	 */
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	/**
	 * Constant URI for the JAXP Schema Source
	 */
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	static {
//		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
//		System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.xsltc.trax.TransformerFactoryImpl");
//		System.setProperty("javax.xml.xpath.XPathFactory:http://java.sun.com/jaxp/xpath/dom", "org.apache.xpath.jaxp.XPathFactoryImpl");
//		System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");

		globalVariables = new HashMap<>();

		JobAction.register("delete-files", DeleteFilesAction.class);
		JobAction.register("jdbc", JdbcAction.class);

		CriteriaContext.register("name", FileNameContext.class);
		CriteriaContext.register("path", FilePathContext.class);
		CriteriaContext.register("owner", FileOwnerContext.class);
		CriteriaContext.register("date-created", FileDateCreatedContext.class);
		CriteriaContext.register("date-modified", FileDateModifiedContext.class);
		CriteriaContext.register("size", FileSizeContext.class);

		AbstractCriterion.register("name/equals", ValueEquals.class);
		AbstractCriterion.register("name/starts-with", ValueStartsWith.class);
		AbstractCriterion.register("name/ends-with", ValueEndsWith.class);
		AbstractCriterion.register("name/matches/pattern", ValuePattern.class);
		AbstractCriterion.register("name/matches/regex", ValueRegex.class);

		AbstractCriterion.register("path/equals", ValueEquals.class);
		AbstractCriterion.register("path/starts-with", ValueStartsWith.class);
		AbstractCriterion.register("path/ends-with", ValueEndsWith.class);
		AbstractCriterion.register("path/matches/pattern", ValuePattern.class);
		AbstractCriterion.register("path/matches/regex", ValueRegex.class);

		AbstractCriterion.register("owner/equals", ValueEquals.class);
		AbstractCriterion.register("owner/starts-with", ValueStartsWith.class);
		AbstractCriterion.register("owner/ends-with", ValueEndsWith.class);
		AbstractCriterion.register("owner/matches/pattern", ValuePattern.class);
		AbstractCriterion.register("owner/matches/regex", ValueRegex.class);

		AbstractCriterion.register("date-created/before", DateBefore.class);
		AbstractCriterion.register("date-created/after", DateAfter.class);

		AbstractCriterion.register("date-modified/before", DateBefore.class);
		AbstractCriterion.register("date-modified/after", DateAfter.class);

		AbstractCriterion.register("size/min", MinSize.class);
		AbstractCriterion.register("size/max", MaxSize.class);
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		boolean showUsage = true;
		CommandLineParser cParser = new DefaultParser();
		try {
			while (true) {
				CommandLine c = cParser.parse(OPTIONS, args, false);
				if (c.hasOption("help")) {
					break;
				}
//				if (c.hasOption("verbose")) {
//					try {
//						int verboseLevel = Integer.parseInt(c.getOptionValue("verbose", "3"));
//						LogLevel nl = LogLevel.valueOf(verboseLevel);
//						if (nl != null) {
//							Main.log("Setting log level to %s (%s)", verboseLevel, nl.name());
//							LOGGER.setLevel(nl.getLevel());
//						}
//					} catch (Exception ex) {
//					}
//				}
				Main main = new Main(c);
				main.readJobs();
				main.doJobs();
				return;
			}
		} catch (ParseException ex) {
		}
		if (showUsage) {
			HelpFormatter help = new HelpFormatter();
			help.printHelp("java -jar JJobs.jar " + Main.class.getName(), OPTIONS, true);
		}
	}

	/**
	 *
	 * @param node
	 * @param name
	 *
	 * @return
	 */
	public final static String getAttribute(Node node, String name) {
		return getAttribute(node.getAttributes(), name);
	}

	/**
	 *
	 * @param node
	 * @param name
	 * @param defaultValue
	 *
	 * @return
	 */
	public final static String getAttribute(Node node, String name, String defaultValue) {
		if (node != null) {
			return getAttribute(node.getAttributes(), name, defaultValue);
		}
		return defaultValue;
	}

	/**
	 *
	 * @param attributes
	 * @param name
	 *
	 * @return
	 */
	public final static String getAttribute(NamedNodeMap attributes, String name) {
		return getAttribute(attributes, name, null);
	}

	/**
	 *
	 * @param attributes
	 * @param name
	 * @param defaultValue
	 *
	 * @return
	 */
	public final static String getAttribute(NamedNodeMap attributes, String name, String defaultValue) {
		Node attr = attributes.getNamedItem(name);
		if (attr != null) {
			return attr.getNodeValue();
		}
		return defaultValue;
	}

	/**
	 *
	 * @param message
	 * @param parameters
	 */
	public final static void log(Object message, Object... parameters) {
		StringBuilder sb = new StringBuilder(String.valueOf(message));
		sb.append('\n');
		System.out.format(sb.toString(), parameters);
		for (LogHandler handler : logHandlers) {
			handler.publish(sb.toString(), parameters);
		}
	}

	/**
	 *
	 * @param ex
	 */
	public final static void err(Exception ex) {
		err(null, ex);
	}

	/**
	 *
	 * @param message
	 * @param parameters
	 */
	public final static void err(Object message, Object... parameters) {
		err(message, null, parameters);
	}

	/**
	 *
	 * @param message
	 * @param ex the value of ex
	 * @param parameters
	 */
	public static final void err(Object message, Exception ex, Object... parameters) {
		StringWriter sw = new StringWriter();
		if (message != null) {
			sw.append(String.valueOf(message));
		}
		if (ex != null) {
			if (sw.getBuffer().length() > 0) {
				sw.append('\n');
			}
			ex.printStackTrace(new PrintWriter(sw));
		}
		sw.append('\n');
		String r = sw.toString();
		System.err.format(r, parameters);
		for (LogHandler handler : logHandlers) {
			handler.publish(r, parameters);
		}
	}

	/**
	 *
	 * @param value
	 *
	 * @return
	 */
	public static String resolveProperties(String value) {
		return resolveProperties(null, value);
	}

	/**
	 *
	 * @param properties
	 * @param value
	 *
	 * @return
	 */
	public static String resolveProperties(JSONObject properties, String value) {
		Matcher m = PATTERN_PROPERTY.matcher(value);
		StringBuffer sb = new StringBuffer();
		boolean doneReplace;
		while (m.find()) {
			doneReplace = false;
			if (properties != null) {
				String pName = m.group(1);
				if (pName != null && pName.length() > 0) {
					m.appendReplacement(sb, getPropertyString(properties, pName, "").replaceAll("(?<!\\\\)\\$(?!\\d)", "\\\\$0"));
					doneReplace = true;
				}
			}
			if (!doneReplace) {
				m.appendReplacement(sb, "");
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private final CommandLine cmdLine;
	private final JNamespaceContext namespaceContext;
	private final JXPathVariableResolver xpathVariableResolver;
	private final DocumentBuilderFactory docBuilderFactory;
	private final XPathFactory xPathFactory;

	private final XPath xpath;
	private Document jobsDoc;
	private final List<Job> jobs;
	private final ExecutorService asyncTasks;

	private Main(CommandLine cmdLine) {
		this.cmdLine = cmdLine;
		namespaceContext = new JNamespaceContext();
		xpathVariableResolver = new JXPathVariableResolver();
		docBuilderFactory = DocumentBuilderFactory.newInstance();
//		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		docBuilderFactory.setNamespaceAware(true);
//    docBuilderFactory.setXIncludeAware(true);

		xPathFactory = XPathFactory.newInstance();
		xPathFactory.setXPathVariableResolver(xpathVariableResolver);
		xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(namespaceContext);
		jobs = new ArrayList<>();
		asyncTasks = Executors.newWorkStealingPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
	}

	/**
	 *
	 * @param handler
	 */
	public static final void addLogHandler(LogHandler handler) {
		if (handler != null) {
			logHandlers.add(handler);
		}
	}

	/**
	 *
	 * @param handler
	 */
	public static final void removeLogHandler(LogHandler handler) {
		if (handler != null) {
			if (logHandlers.contains(handler)) {
				handler.close();
				logHandlers.remove(handler);
			}
		}
	}

	/**
	 *
	 * @param name
	 *
	 * @return
	 */
	public static final Object getVariable(String name) {
		return globalVariables.get(name);
	}

	/**
	 *
	 * @param <T>
	 * @param name
	 * @param clazz
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T getVariable(String name, Class<T> clazz) {
		Object r = getVariable(name);
		if (r != null) {
			if (clazz.isInstance(r)) {
				return (T)r;
			}
		}
		return null;
	}

	/**
	 *
	 * @param properties
	 * @param name
	 *
	 * @return
	 */
	public static final JSONObject getProperty(JSONObject properties, String name) {
		JSONObject r = null;
		if (name != null) {
			String[] path = name.split("\\.");
			if (path.length > 0) {
				try {
					if (path.length > 1) {
						JSONObject o = properties;
						for (int pi = 0; pi < path.length; pi++) {
							if (pi < (path.length - 1)) {
								o = o.getJSONObject(path[pi]);
							} else {
								r = o.getJSONObject(path[pi]);
							}
						}
					} else {
						r = properties.getJSONObject(path[0]);
					}
				} catch (JSONException ex) {
					Main.err(ex);
				}
			}
		}
		return r;
	}

	/**
	 *
	 * @param properties
	 * @param name
	 *
	 * @return
	 */
	public static final String getPropertyString(JSONObject properties, String name) {
		return getPropertyString(properties, name, null);
	}

	/**
	 *
	 * @param properties
	 * @param name
	 * @param defaultValue
	 *
	 * @return
	 */
	public static final String getPropertyString(JSONObject properties, String name, String defaultValue) {
		String r = defaultValue;
		if (properties != null && name != null) {
			String[] path = name.split("\\.");
			if (path.length > 0) {
				if (path.length > 1) {
					JSONObject o;
					StringBuilder nPath = new StringBuilder();
					Arrays.stream(path, 0, path.length - 1).forEachOrdered(new Consumer<String>() {
						@Override
						public void accept(String t) {
							if (nPath.length() > 0) {
								nPath.append('.');
							}
							nPath.append(t);
						}
					});
					o = getProperty(properties, nPath.toString());
					r = getPropertyString(o, path[path.length - 1], defaultValue);
				} else {
					Object o = properties.opt(path[0]);
					if (o != null) {
						r = String.valueOf(o);
					}
				}
			}
		}
		return r;
	}

	/**
	 *
	 * @param properties
	 * @param name
	 * @param object
	 */
	public static void setProperty(JSONObject properties, String name, Object object) {
		String[] path = name.split("\\.");
		if (path.length > 0) {
			if (path.length > 1) {
				JSONObject o = properties;
				for (int pi = 0; pi < path.length; pi++) {
					try {
						if (pi < (path.length - 1)) {
							o = o.getJSONObject(path[pi]);
						} else {
							o.put(path[pi], object);
						}
					} catch (JSONException ex) {
						break;
					}
				}
			} else {
				globalVariables.put(path[0], object);
			}
		}
	}

	/**
	 *
	 * @param name
	 * @param value
	 */
	public static void setVariable(String name, Object value) {
		globalVariables.put(name, value);
	}

	/**
	 * Run all enabled jobs.
	 */
	public void doJobs() {
		if (!hasJobs()) {
			Main.err("There are no enabled jobs in \"%s\"", new File(cmdLine.getOptionValue("jobs")).getAbsolutePath());
			return;
		}
		for (Job job : jobs) {
			Main.log(job);
			boolean isAsync = job.getBooleanAttribute("async", false);
			if (isAsync) {
				asyncTasks.submit(job);
			} else {
				job.run();
			}
		}
		asyncTasks.shutdown();
		try {
			asyncTasks.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException ex) {
			Main.err(ex);
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean hasJobs() {
		return (!jobs.isEmpty());
	}

	/**
	 * Replaces the list of jobs with those read from the XML file specified on
	 * the command-line.
	 */
	public void readJobs() {
		File jobsFile = new File(cmdLine.getOptionValue("jobs", "jobs.xml"));
		jobs.clear();
		jobsDoc = loadDocument(jobsFile);
		if (jobsDoc != null) {
			try {
				JXPathBuilder pb = new JXPathBuilder(namespaceContext);
				pb.fromRoot().elementLocalName("jobs").step().elementLocalName("job");
				NodeList jobNodes = (NodeList)xpath.evaluate(pb.toString(), jobsDoc, XPathConstants.NODESET);
				for (int i = 0; i < jobNodes.getLength(); i++) {
					Node jobNode = jobNodes.item(i);
					boolean isDisabled = Job.getBooleanAttribute(jobNode, "disabled", false);
					if (isDisabled) {
						continue;
					}
					this.jobs.add(new Job(xpath, jobNode));
				}
			} catch (XPathExpressionException ex) {
				Main.err(ex);
			}
		}
	}

	private Document loadDocument(File inputFile) {
		return loadDocument(inputFile, true);
	}

	private Document loadDocument(File inputFile, boolean validate) {
		Document document = null;
		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			document = docBuilder.parse(inputFile);
			namespaceContext.addNamespaces(document);
			namespaceContext.applyNamespaces(document);
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Main.err("Failed to load XML document \"%s\"", ex, inputFile.getAbsolutePath());
		}
		if (document != null && validate) {
			Object schemaSource = null;
			try {
				JXPathBuilder xpb = new JXPathBuilder(namespaceContext);
				xpb.fromRoot().anyElement().step().attributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
				String schemaLocation = (String)xpath.evaluate(xpb.toString(), document, XPathConstants.STRING);
				String[] schemaLocationParts = schemaLocation.split("\\s+", 2);
				schemaSource = schemaLocationParts[schemaLocationParts.length - 1];
			} catch (Exception ex) {
				Main.err("Error occured while attempting to find the schemaLocation.", ex);
			}
			if (schemaSource != null) {
//				LOGGER.log(Level.FINE, "Validation entity referenced at \"%s\"", schemaSource);
				Main.log("Validation entity referenced at \"%s\"", schemaSource);
				try {
					SAXParserFactory spf = SAXParserFactory.newInstance();
					spf.setNamespaceAware(true);
					spf.setValidating(true);
					SAXParser sp = spf.newSAXParser();
					sp.setProperty(JAXP_SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
					sp.setProperty(JAXP_SCHEMA_SOURCE, schemaSource);
					XMLReader xr = sp.getXMLReader();
					xr.setErrorHandler(new ErrorHandler() {
						@Override
						public void warning(SAXParseException ex) throws SAXException {
							Main.err(ex);
						}

						@Override
						public void error(SAXParseException ex) throws SAXException {
							throw ex;
						}

						@Override
						public void fatalError(SAXParseException ex) throws SAXException {
							throw ex;
						}
					});
					xr.parse(new InputSource(inputFile.toURI().toString()));
//					LOGGER.log(Level.FINE, "Validation succeeded.");
					Main.log("Validation succeeded.");
					return document;
				} catch (SAXParseException ex) {
					Main.err("Validation of document failed.\nDocument: \"%s\"\nSchema Definition: \"%s\"", ex, inputFile.getName(), schemaSource);
				} catch (ParserConfigurationException | SAXException | IOException ex) {
					Main.err("Error ocurred during validation.", ex);
				}
			}
			return null;
		}
		return document;
	}

	private enum LogLevel {
		OFF(Level.OFF),
		SEVERE(Level.SEVERE),
		WARNING(Level.WARNING),
		INFO(Level.INFO),
		CONFIG(Level.CONFIG),
		FINE(Level.FINE),
		FINER(Level.FINER),
		FINEST(Level.FINEST);

		private final Level level;

		LogLevel(Level level) {
			this.level = level;
		}

		public Level getLevel() {
			return level;
		}

		public static LogLevel valueOf(int level) {
			if (level < 0) {
				return null;
			}
			LogLevel[] levels = LogLevel.values();
			if (level > (levels.length - 1)) {
				return null;
			}
			return levels[level];
		}
	}
}
