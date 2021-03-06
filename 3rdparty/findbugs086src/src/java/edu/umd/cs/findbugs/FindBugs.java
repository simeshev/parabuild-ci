/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2003,2004 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.AnalysisException;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.ClassObserver;
import edu.umd.cs.findbugs.ba.InnerClassAccessMap;
import edu.umd.cs.findbugs.visitclass.Constants2;

/**
 * An instance of this class is used to apply the selected set of
 * analyses on some collection of Java classes.  It also implements the
 * comand line interface.
 *
 * @author Bill Pugh
 * @author David Hovemeyer
 */
public class FindBugs implements Constants2, ExitCodes {
	/* ----------------------------------------------------------------------
	 * Helper classes
	 * ---------------------------------------------------------------------- */

	/**
	 * Delegating InputStream wrapper that never closes the
	 * underlying input stream.
	 */
	private static class NoCloseInputStream extends DataInputStream {
		/**
		 * Constructor.
		 * @param in the real InputStream
		 */
		public NoCloseInputStream(InputStream in) {
			super(in);
		}

		public void close() {
		}
	}

	/**
	 * Work list item specifying a file/directory/URL containing
	 * class files to analyze.
	 */
	private static class ArchiveWorkListItem {
		private String fileName;
		private boolean explicit;

		/**
		 * Constructor.
		 *
		 * @param fileName file/directory/URL
		 * @param explicit true if this source of classes appeared explicitly
		 *                 in the project file, false if was found indirectly
		 *                 (e.g., a nested jar file in a .war file)
		 */
		public ArchiveWorkListItem(String fileName, boolean explicit) {
			this.fileName = fileName;
			this.explicit = explicit;
		}

		/**
		 * Get the file/directory/URL.
		 */
		public String getFileName() {
			return fileName;
		}

		/**
		 * Return whether this class source appeared explicitly in
		 * the project file.
		 */
		public boolean isExplicit() {
			return explicit;
		}
	}

	/**
	 * Interface for an object representing a source of class files to analyze.
	 */
	private interface ClassProducer {
		/**
		 * Get the next class to analyze.
		 *
		 * @return the class, or null of there are no more classes for this ClassProducer
		 * @throws IOException          if an IOException occurs
		 * @throws InterruptedException if the thread is interrupted
		 */
		public JavaClass getNextClass() throws IOException, InterruptedException;

		/**
		 * Did this class producer scan any Java source files?
		 */
		public boolean containsSourceFiles();

		/**
		 * Close any internal files or streams.
		 */
		public void close();
	}

	/**
	 * ClassProducer for single class files.
	 */
	private class SingleClassProducer implements ClassProducer {
		private URL url;

		/**
		 * Constructor.
		 *
		 * @param url the single class file to be analyzed
		 */
		public SingleClassProducer(URL url) {
			this.url = url;
		}

		public JavaClass getNextClass() throws IOException, InterruptedException {
			if (url == null)
				return null;
			if (Thread.interrupted())
				throw new InterruptedException();

			URL urlToParse = url;
			url = null; // don't return it next time

			// ClassScreener may veto this class.
			if (!classScreener.matches(urlToParse.toString()))
				return null;

			try {
				return parseClass(urlToParse);
			} catch (ClassFormatException e) {
				throw new ClassFormatException("Invalid class file format for " +
				        url.toString() + ": " + e.getMessage());
			}
		}

		public boolean containsSourceFiles() {
			return false;
		}

		public void close() {
			// Nothing to do here
		}
	}

	/**
	 * ClassProducer for zip/jar archives.
	 */
	private class ZipClassProducer implements ClassProducer {
		private URL url;
		private LinkedList<ArchiveWorkListItem> archiveWorkList;
		private List<String> additionalAuxClasspathEntryList;
		private ZipInputStream zipInputStream;
		private boolean containsSourceFiles;

		public ZipClassProducer(URL url, LinkedList<ArchiveWorkListItem> archiveWorkList,
				List<String> additionalAuxClasspathEntryList)
				throws IOException {
			this.url = url;
			this.archiveWorkList = archiveWorkList;
			this.additionalAuxClasspathEntryList = additionalAuxClasspathEntryList;
			if (DEBUG) System.out.println("Opening jar/zip input stream for " + url.toString());
			this.zipInputStream = new ZipInputStream(url.openStream());
			this.containsSourceFiles = false;
		}

		public JavaClass getNextClass() throws IOException, InterruptedException {
			for (;;) {
				if (Thread.interrupted())
					throw new InterruptedException();

				ZipEntry zipEntry = zipInputStream.getNextEntry();
				if (zipEntry == null)
					return null;

				try {
					String entryName = zipEntry.getName();

					// ClassScreener may veto this class.
					if (!classScreener.matches(entryName)) {
						// Add archive URL to aux classpath
						if (!additionalAuxClasspathEntryList.contains(url.toString())) {
							//System.out.println("Adding additional aux classpath entry: " + url.toString());
							additionalAuxClasspathEntryList.add(url.toString());
						}
						continue;
					}

					String fileExtension = getFileExtension(entryName);
					if (fileExtension != null) {
						if (fileExtension.equals(".class")) {
							return parseClass(url.toString(), new NoCloseInputStream(zipInputStream), entryName);
						} else if (archiveExtensionSet.contains(fileExtension)) {
							// Add nested archive to archive work list
							ArchiveWorkListItem nestedItem =
								new ArchiveWorkListItem("jar:" + url.toString() + "!/" + entryName, false);
							archiveWorkList.addFirst(nestedItem);
						} else if (fileExtension.equals(".java")) {
							containsSourceFiles = true;
						}
					}
				} finally {
					zipInputStream.closeEntry();
				}
			}
		}

		public boolean containsSourceFiles() {
			return containsSourceFiles;
		}

		public void close() {
			if (zipInputStream != null) {
				try {
					zipInputStream.close();
				} catch (IOException ignore) {
					// Ignore
				}
			}
		}
	}

	/**
	 * ClassProducer for directories.
	 * The directory is scanned recursively for class files.
	 */
	private class DirectoryClassProducer implements ClassProducer {
		private String dirName;
		private List<String> additionalAuxClasspathEntryList;
		private Iterator<String> rfsIter;
		private boolean containsSourceFiles;

		public DirectoryClassProducer(String dirName,
				List<String> additionalAuxClasspathEntryList) throws InterruptedException {
			this.dirName = dirName;
			this.additionalAuxClasspathEntryList = additionalAuxClasspathEntryList;

			FileFilter filter = new FileFilter() {
				public boolean accept(File file) {
					String fileName = file.getName();
					if (file.isDirectory() || fileName.endsWith(".class"))
						return true;
					if (fileName.endsWith(".java"))
						containsSourceFiles = true;
					return false;
				}
			};

			// This will throw InterruptedException if the thread is
			// interrupted.
			RecursiveFileSearch rfs = new RecursiveFileSearch(dirName, filter).search();
			this.rfsIter = rfs.fileNameIterator();
			this.containsSourceFiles = false;
		}

		public JavaClass getNextClass() throws IOException, InterruptedException {
			String fileName;
			for (;;) {
				if (!rfsIter.hasNext())
					return null;
				fileName = rfsIter.next();
				if (classScreener.matches(fileName)) {
					break;
				} else {
					// Add directory URL to aux classpath
					String dirURL= "file:" + dirName;
					if (!additionalAuxClasspathEntryList.contains(dirURL)) {
						//System.out.println("Adding additional aux classpath entry: " + dirURL);
						additionalAuxClasspathEntryList.add(dirURL);
					}
				}
			}
			try {
				return parseClass(new URL("file:" + fileName));
			} catch (ClassFormatException e) {
				throw new ClassFormatException("Invalid class file format for " +
				        fileName + ": " + e.getMessage());
			}
		}

		public boolean containsSourceFiles() {
			return containsSourceFiles;
		}

		public void close() {
			// Nothing to do here
		}
	}

	/**
	 * A delegating bug reporter which counts reported bug instances,
	 * missing classes, and serious analysis errors.
	 */
	private static class ErrorCountingBugReporter extends DelegatingBugReporter {
		private int bugCount;
		private int missingClassCount;
		private int errorCount;
		private Set<String> missingClassSet = new HashSet<String>();

		public ErrorCountingBugReporter(BugReporter realBugReporter) {
			super(realBugReporter);
			this.bugCount = 0;
			this.missingClassCount = 0;
			this.errorCount = 0;

			// Add an observer to record when bugs make it through
			// all priority and filter criteria, so our bug count is
			// accurate.
			realBugReporter.addObserver(new BugReporterObserver() {
				public void reportBug(BugInstance bugInstance) {
					++bugCount;
				}
			});
		}

		public int getBugCount() {
			return bugCount;
		}

		public int getMissingClassCount() {
			return missingClassCount;
		}

		public int getErrorCount() {
			return errorCount;
		}

		public void logError(String message) {
			++errorCount;
			super.logError(message);
		}

		public void reportMissingClass(ClassNotFoundException ex) {
			String missing = AbstractBugReporter.getMissingClassName(ex);
			if (missingClassSet.add(missing))
				++missingClassCount;
			super.reportMissingClass(ex);
		}
	}

	private static class CategoryFilteringBugReporter extends DelegatingBugReporter {
		private Set<String> categorySet;

		public CategoryFilteringBugReporter(BugReporter realBugReporter, Set<String> categorySet) {
			super(realBugReporter);
			this.categorySet = categorySet;
		}

		public void reportBug(BugInstance bugInstance) {
			BugPattern bugPattern = bugInstance.getBugPattern();
			String category = bugPattern.getCategory();
			if (categorySet.contains(category))
				getRealBugReporter().reportBug(bugInstance);
		}
	}

	/**
	 * Handling callback for choose() method,
	 * used to implement the -chooseVisitors and -choosePlugins options.
	 */
	private interface Chooser {
		/**
		 * Choose a detector, plugin, etc.
		 *
		 * @param enable whether or not the item should be enabled
		 * @param what   the item
		 */
		public void choose(boolean enable, String what);
	}

	private static final int PRINTING_REPORTER = 0;
	private static final int SORTING_REPORTER = 1;
	private static final int XML_REPORTER = 2;
	private static final int EMACS_REPORTER = 3;
	private static final int HTML_REPORTER = 4;
	private static final int XDOCS_REPORTER = 5;

	/**
	 * Helper class to parse the command line and create
	 * the FindBugs engine object.
	 */
	private static class FindBugsCommandLine extends CommandLine {
		private int bugReporterType = PRINTING_REPORTER;
		private boolean xmlWithMessages = false;
		private String stylesheet = null;
		private Project project = new Project();
		private boolean quiet = false;
		private ClassScreener classScreener = new ClassScreener();
		private String filterFile = null;
		private boolean include = false;
		private boolean setExitCode = false;
		private int priorityThreshold = Detector.NORMAL_PRIORITY;
		private PrintStream outputStream = null;
		private Set<String> bugCategorySet = null;

		public FindBugsCommandLine() {
			addOption("-home", "home directory", "specify FindBugs home directory");
			addOption("-pluginList", "jar1[" + File.pathSeparator + "jar2...]",
			        "specify list of plugin Jar files to load");
			addSwitch("-showPlugins", "show list of available plugins");
			addSwitch("-quiet", "suppress error messages");
			addSwitch("-experimental", "report all warnings including experimental bug patterns");
			addSwitch("-low", "report all warnings");
			addSwitch("-medium", "report only medium and high priority warnings [default]");
			addSwitch("-high", "report only high priority warnings");
			addSwitch("-sortByClass", "sort warnings by class");
			addSwitchWithOptionalExtraPart("-xml", "withMessages",
				"XML output (optionally with messages)");
			addSwitch("-xdocs", "xdoc XML output to use with Apache Maven");
			addSwitchWithOptionalExtraPart("-html", "stylesheet",
				"Generate HTML output (default stylesheet is default.xsl)");
			addSwitch("-emacs", "Use emacs reporting format");
			addOption("-outputFile", "filename", "Save output in named file");
			addOption("-visitors", "v1[,v2...]", "run only named visitors");
			addOption("-omitVisitors", "v1[,v2...]", "omit named visitors");
			addOption("-chooseVisitors", "+v1,-v2,...", "selectively enable/disable detectors");
			addOption("-choosePlugins", "+p1,-p2,...", "selectively enable/disable plugins");
			addSwitch("-adjustExperimental", "lower the priority of Bug Patterns that are experimental");
			addOption("-adjustPriority", "v1=(raise|lower)[,...]",
					"raise/lower priority of warnings for given visitor(s)");
			addOption("-bugCategories", "cat1[,cat2...]", "only report bugs in given categories");
			addOption("-onlyAnalyze", "classes/packages", "only analyze given classes and packages");
			addOption("-exclude", "filter file", "exclude bugs matching given filter");
			addOption("-include", "filter file", "include only bugs matching given filter");
			addOption("-auxclasspath", "classpath", "set aux classpath for analysis");
			addOption("-sourcepath", "source path", "set source path for analyzed classes");
			addOption("-project", "project", "analyze given project");
			addSwitch("-exitcode", "set exit code of process");
		}

		public Project getProject() {
			return project;
		}

		public boolean setExitCode() {
			return setExitCode;
		}

		public boolean quiet() {
			return quiet;
		}

		protected void handleOption(String option, String optionExtraPart) {
			if (option.equals("-showPlugins")) {
				System.out.println("Available plugins:");
				int count = 0;
				for (Iterator<Plugin> i = DetectorFactoryCollection.instance().pluginIterator(); i.hasNext(); ) {
					Plugin plugin = i.next();
					System.out.println("  " + plugin.getPluginId() + " (default: " +
						(plugin.isEnabled() ? "enabled" : "disabled") + ")");
					if (plugin.getShortDescription() != null)
						System.out.println("    Description: " + plugin.getShortDescription());
					if (plugin.getProvider() != null)
						System.out.println("    Provider: " + plugin.getProvider());
					if (plugin.getWebsite() != null)
						System.out.println("    Website: " + plugin.getWebsite());
					++count;
				}
				if (count == 0) {
					System.out.println("  No plugins are available (FindBugs installed incorrectly?)");
				}
				System.exit(0);
			} else if (option.equals("-experimental"))
				priorityThreshold = Detector.EXP_PRIORITY;
			else if (option.equals("-low"))
				priorityThreshold = Detector.LOW_PRIORITY;
			else if (option.equals("-medium"))
				priorityThreshold = Detector.NORMAL_PRIORITY;
			else if (option.equals("-high"))
				priorityThreshold = Detector.HIGH_PRIORITY;
			else if (option.equals("-adjustExperimental"))
				BugInstance.setAdjustExperimental(true);
			else if (option.equals("-sortByClass"))
				bugReporterType = SORTING_REPORTER;
			else if (option.equals("-xml")) {
				bugReporterType = XML_REPORTER;
				if (!optionExtraPart.equals("")) {
					if (optionExtraPart.equals("withMessages"))
						xmlWithMessages = true;
					else
						throw new IllegalArgumentException("Unknown option: -xml:" + optionExtraPart);
				}
			} else if (option.equals("-emacs"))
				bugReporterType = EMACS_REPORTER;
			else if (option.equals("-html")) {
				bugReporterType = HTML_REPORTER;
				if (!optionExtraPart.equals("")) {
					stylesheet = optionExtraPart;
				} else {
					stylesheet = "default.xsl";
				}
			} else if (option.equals("-xdocs"))
				bugReporterType = XDOCS_REPORTER;
			else if (option.equals("-quiet"))
				quiet = true;
			else if (option.equals("-exitcode"))
				setExitCode = true;
			else
				throw new IllegalStateException();
		}

		protected void handleOptionWithArgument(String option, String argument) throws IOException {
			if (option.equals("-home")) {
				FindBugs.setHome(argument);
			} else if (option.equals("-pluginList")) {
				String pluginListStr = argument;
				ArrayList<File> pluginList = new ArrayList<File>();
				StringTokenizer tok = new StringTokenizer(pluginListStr, File.pathSeparator);
				while (tok.hasMoreTokens()) {
					pluginList.add(new File(tok.nextToken()));
				}

				DetectorFactoryCollection.setPluginList((File[]) pluginList.toArray(new File[pluginList.size()]));
			} else if (option.equals("-outputFile")) {
				String outputFile = argument;

				try {
					outputStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
				} catch (IOException e) {
					System.err.println("Couldn't open " + outputFile + " for output: " + e.toString());
					System.exit(1);
				}
			} else if (option.equals("-visitors") || option.equals("-omitVisitors")) {
				boolean omit = option.equals("-omitVisitors");

				if (!omit) {
					// Selecting detectors explicitly, so start out by
					// disabling all of them.  The selected ones will
					// be re-enabled.
					DetectorFactoryCollection.instance().disableAll();
				}

				// Explicitly enable or disable the selected detectors.
				StringTokenizer tok = new StringTokenizer(argument, ",");
				while (tok.hasMoreTokens()) {
					String visitorName = tok.nextToken();
					DetectorFactory factory = DetectorFactoryCollection.instance().getFactory(visitorName);
					if (factory == null)
						throw new IllegalArgumentException("Unknown detector: " + visitorName);
					factory.setEnabled(!omit);
				}
			} else if (option.equals("-chooseVisitors")) {
				// This is like -visitors and -omitVisitors, but
				// you can selectively enable and disable detectors,
				// starting from the default set (or whatever set
				// happens to be in effect).
				choose(argument, "Detector choices", new Chooser() {
					public void choose(boolean enabled, String what) {
						DetectorFactory factory = DetectorFactoryCollection.instance()
							.getFactory(what);
						if (factory == null)
							throw new IllegalArgumentException("Unknown detector: " + what);
						factory.setEnabled(enabled);
					}
				});
			} else if (option.equals("-choosePlugins")) {
				// Selectively enable/disable plugins
				choose(argument, "Plugin choices", new Chooser() {
					public void choose(boolean enabled, String what) {
						Plugin plugin = DetectorFactoryCollection.instance().getPluginById(what);
						if (plugin == null)
							throw new IllegalArgumentException("Unknown plugin: " + what);
						plugin.setEnabled(enabled);
					}
				});
			} else if (option.equals("-adjustPriority")) {
				// Selectively raise or lower the priority of warnings
				// produced by specified detectors.
				
				StringTokenizer tok = new StringTokenizer(argument, ",");
				while (tok.hasMoreTokens()) {
					String token = tok.nextToken();
					int eq = token.indexOf('=');
					if (eq < 0)
						throw new IllegalArgumentException("Illegal priority adjustment: " + token);

					String visitorName = token.substring(0, eq);

					DetectorFactory factory = DetectorFactoryCollection.instance()
						.getFactory(visitorName);
					if (factory == null)
						throw new IllegalArgumentException("Unknown detector: " + visitorName);
					
					String adjustment = token.substring(eq + 1);
					if (!(adjustment.equals("raise") || adjustment.equals("lower")))
						throw new IllegalArgumentException("Illegal priority adjustment value: " +
								adjustment);
					
					// Recall that lower values are higher priorities
					factory.setPriorityAdjustment(adjustment.equals("raise") ? -1 : +1);
				}
			} else if (option.equals("-bugCategories")) {
				this.bugCategorySet = handleBugCategories(argument);
			} else if (option.equals("-onlyAnalyze")) {
				// The argument is a comma-separated list of classes and packages
				// to select to analyze.  (If a list item ends with ".*",
				// it specifies a package, otherwise it's a class.)
				StringTokenizer tok = new StringTokenizer(argument, ",");
				while (tok.hasMoreTokens()) {
					String item = tok.nextToken();
					if (item.endsWith(".-"))
						classScreener.addAllowedPrefix(item.substring(0, item.length() - 1));
					else if (item.endsWith(".*"))
						classScreener.addAllowedPackage(item.substring(0, item.length() - 1));
					else
						classScreener.addAllowedClass(item);
				}
			} else if (option.equals("-exclude") || option.equals("-include")) {
				filterFile = argument;
				include = option.equals("-include");
			} else if (option.equals("-auxclasspath")) {
				StringTokenizer tok = new StringTokenizer(argument, File.pathSeparator);
				while (tok.hasMoreTokens())
					project.addAuxClasspathEntry(tok.nextToken());
			} else if (option.equals("-sourcepath")) {
				StringTokenizer tok = new StringTokenizer(argument, File.pathSeparator);
				while (tok.hasMoreTokens())
					project.addSourceDir(new File(tok.nextToken()).getAbsolutePath());
			} else if (option.equals("-project")) {
				String projectFile = argument;

				// Convert project file to be an absolute path
				projectFile = new File(projectFile).getAbsolutePath();

				try {
					project = new Project();
					project.read(projectFile);
				} catch (IOException e) {
					System.err.println("Error opening " + projectFile);
					e.printStackTrace(System.err);
					throw e;
				}
			}
		}

		/**
		 * Common handling code for -chooseVisitors and -choosePlugins options.
		 *
		 * @param argument the list of visitors or plugins to be chosen
		 * @param desc     String describing what is being chosen
		 * @param chooser  callback object to selectively choose list members
		 */
		private void choose(String argument, String desc, Chooser chooser) {
			StringTokenizer tok = new StringTokenizer(argument, ",");
			while (tok.hasMoreTokens()) {
				String what = tok.nextToken();
				if (!what.startsWith("+") && !what.startsWith("-"))
					throw new IllegalArgumentException(desc + " must start with " +
						"\"+\" or \"-\" (saw " + what + ")");
				boolean enabled = what.startsWith("+");
				chooser.choose(enabled, what.substring(1));
			}
		}

		public FindBugs createEngine() throws IOException, FilterException {
			TextUIBugReporter textuiBugReporter = null;
			switch (bugReporterType) {
			case PRINTING_REPORTER:
				textuiBugReporter = new PrintingBugReporter();
				break;
			case SORTING_REPORTER:
				textuiBugReporter = new SortingBugReporter();
				break;
			case XML_REPORTER:
				{
					XMLBugReporter xmlBugReporter = new XMLBugReporter(project);
					xmlBugReporter.setAddMessages(xmlWithMessages);
					textuiBugReporter = xmlBugReporter;
				}
				break;
			case EMACS_REPORTER:
				textuiBugReporter = new EmacsBugReporter();
				break;
			case HTML_REPORTER:
				textuiBugReporter = new HTMLBugReporter(project, stylesheet);
				break;
			case XDOCS_REPORTER:
				textuiBugReporter = new XDocsBugReporter(project);
				break;
			default:
				throw new IllegalStateException();
			}

			if (quiet)
				textuiBugReporter.setErrorVerbosity(BugReporter.SILENT);

			textuiBugReporter.setPriorityThreshold(priorityThreshold);

			if (outputStream != null)
				textuiBugReporter.setOutputStream(outputStream);

			BugReporter bugReporter = textuiBugReporter;

			if (bugCategorySet != null) {
				bugReporter = new CategoryFilteringBugReporter(bugReporter, bugCategorySet);
			}

			FindBugs findBugs = new FindBugs(bugReporter, project);

			if (filterFile != null)
				findBugs.setFilter(filterFile, include);

			findBugs.setClassScreener(classScreener);

			return findBugs;
		}
	}

	/* ----------------------------------------------------------------------
	 * Member variables
	 * ---------------------------------------------------------------------- */

	static final boolean DEBUG = Boolean.getBoolean("findbugs.debug");

	/**
	 * FindBugs home directory.
	 */
	private static String home;

	/**
	 * File extensions that indicate an archive (zip, jar, or similar).
	 */
	private static final Set<String> archiveExtensionSet = new HashSet<String>();
	static {
		archiveExtensionSet.add(".jar");
		archiveExtensionSet.add(".zip");
		archiveExtensionSet.add(".war");
		archiveExtensionSet.add(".ear");
		archiveExtensionSet.add(".sar");
	}

	/**
	 * Known URL protocols.
	 * Filename URLs that do not have an explicit protocol are
	 * assumed to be files.
	 */
	private static final Set<String> knownURLProtocolSet = new HashSet<String>();
	static {
		knownURLProtocolSet.add("file");
		knownURLProtocolSet.add("http");
		knownURLProtocolSet.add("https");
		knownURLProtocolSet.add("jar");
	}

	private ErrorCountingBugReporter bugReporter;
	private Project project;
	private List<ClassObserver> classObserverList;
	private Detector detectors [];
	private FindBugsProgress progressCallback;
	private ClassScreener classScreener;
	private AnalysisContext analysisContext;
	private String currentClass;

	/* ----------------------------------------------------------------------
	 * Public methods
	 * ---------------------------------------------------------------------- */

	/**
	 * Constructor.
	 *
	 * @param bugReporter the BugReporter object that will be used to report
	 *                    BugInstance objects, analysis errors, class to source mapping, etc.
	 * @param project     the Project indicating which files to analyze and
	 *                    the auxiliary classpath to use; note that the FindBugs
	 *                    object will create a private copy of the Project object
	 */
	public FindBugs(BugReporter bugReporter, Project project) {
		if (bugReporter == null)
			throw new IllegalArgumentException("null bugReporter");
		if (project == null)
			throw new IllegalArgumentException("null project");

		this.bugReporter = new ErrorCountingBugReporter(bugReporter);
		this.project = project.duplicate();
		this.classObserverList = new LinkedList<ClassObserver>();

		// Create a no-op progress callback.
		this.progressCallback = new FindBugsProgress() {
			public void reportNumberOfArchives(int numArchives) {
			}

			public void finishArchive() {
			}

			public void startAnalysis(int numClasses) {
			}

			public void finishClass() {
			}

			public void finishPerClassAnalysis() {
			}
		};

		// Class screener
		this.classScreener = new ClassScreener();

		addClassObserver(bugReporter);
	}

	/**
	 * Set the progress callback that will be used to keep track
	 * of the progress of the analysis.
	 *
	 * @param progressCallback the progress callback
	 */
	public void setProgressCallback(FindBugsProgress progressCallback) {
		this.progressCallback = progressCallback;
	}

	/**
	 * Set filter of bug instances to include or exclude.
	 *
	 * @param filterFileName the name of the filter file
	 * @param include        true if the filter specifies bug instances to include,
	 *                       false if it specifies bug instances to exclude
	 */
	public void setFilter(String filterFileName, boolean include) throws IOException, FilterException {
		Filter filter = new Filter(filterFileName);
		BugReporter origBugReporter = bugReporter.getRealBugReporter();
		BugReporter filterBugReporter = new FilterBugReporter(origBugReporter, filter, include);
		bugReporter.setRealBugReporter(filterBugReporter);
	}

	/**
	 * Add a ClassObserver.
	 *
	 * @param classObserver the ClassObserver
	 */
	public void addClassObserver(ClassObserver classObserver) {
		classObserverList.add(classObserver);
	}

	/**
	 * Set the ClassScreener.
	 * This object chooses which individual classes to analyze.
	 * By default, all classes are analyzed.
	 *
	 * @param classScreener the ClassScreener to use
	 */
	public void setClassScreener(ClassScreener classScreener) {
		this.classScreener = classScreener;
	}

	/**
	 * Execute FindBugs on the Project.
	 * All bugs found are reported to the BugReporter object which was set
	 * when this object was constructed.
	 *
	 * @throws java.io.IOException  if an I/O exception occurs analyzing one of the files
	 * @throws InterruptedException if the thread is interrupted while conducting the analysis
	 */
	public void execute() throws java.io.IOException, InterruptedException {
		// Configure the analysis context
		analysisContext = new AnalysisContext(bugReporter);
		analysisContext.setSourcePath(project.getSourceDirList());

		// Give the BugReporter a reference to this object,
		// in case it wants to access information such
		// as the AnalysisContext
		bugReporter.setEngine(this);

		// Create detectors
		createDetectors();

		// Clear the repository of classes
		clearRepository();
		
		// Get list of files to analyze.
		// Note that despite the name getJarFileArray(),
		// they can also be zip files, directories,
		// and single class files.
		LinkedList<ArchiveWorkListItem> archiveWorkList = new LinkedList<ArchiveWorkListItem>();
		for (Iterator<String> i = project.getFileList().iterator(); i.hasNext(); ) {
			String fileName = i.next();
			archiveWorkList.add(new ArchiveWorkListItem(fileName, true));
		}

		// Report how many archives/directories/files will be analyzed,
		// for progress dialog in GUI
		progressCallback.reportNumberOfArchives(archiveWorkList.size());

		// Keep track of the names of all classes to be analyzed
		List<String> repositoryClassList = new LinkedList<String>();

		// Record additional entries that should be added to
		// the aux classpath.  These occur when one or more classes
		// in a directory or archive are skipped, to ensure that
		// the skipped classes can still be referenced.
		List<String> additionalAuxClasspathEntryList = new LinkedList<String>();

		// Add all classes in analyzed archives/directories/files
		while (!archiveWorkList.isEmpty()) {
			ArchiveWorkListItem item = archiveWorkList.removeFirst();
			scanArchiveOrDirectory(item, archiveWorkList, repositoryClassList,
				additionalAuxClasspathEntryList);
		}
		
		// Now that we have scanned all specified archives and directories,
		// we can set the repository classpath.
		setRepositoryClassPath(additionalAuxClasspathEntryList);

		// Callback for progress dialog: analysis is starting
		progressCallback.startAnalysis(repositoryClassList.size());

		// Examine all classes for bugs.
		// Don't examine the same class more than once.
		// (The user might specify two jar files that contain
		// the same class.)
		Set<String> examinedClassSet = new HashSet<String>();
		for (Iterator<String> i = repositoryClassList.iterator(); i.hasNext();) {
			String className = i.next();
			if (examinedClassSet.add(className))
				examineClass(className);
		}

		// Callback for progress dialog: analysis finished
		progressCallback.finishPerClassAnalysis();

		// Force any detectors which defer work until all classes have
		// been seen to do that work.
		this.reportFinal();

		// Flush any queued bug reports
		bugReporter.finish();

		// Flush any queued error reports
		bugReporter.reportQueuedErrors();

		// Free up memory for reports
		clearRepository();
	}

	/**
	 * Get the analysis context.
	 * It is only valid to call this method after the execute()
	 * method has been called.
	 */
	public AnalysisContext getAnalysisContext() {
		return analysisContext;
	}

	/**
	 * Get the name of the most recent class to be analyzed.
	 * This is useful for diagnosing an unexpected exception.
	 * Returns null if no class has been analyzed.
	 */
	public String getCurrentClass() {
		return currentClass;
	}

	/**
	 * Get the number of bug instances that were reported during analysis.
	 */
	public int getBugCount() {
		return bugReporter.getBugCount();
	}

	/**
	 * Get the number of errors that occurred during analysis.
	 */
	public int getErrorCount() {
		return bugReporter.getErrorCount();
	}

	/**
	 * Get the number of time missing classes were reported during analysis.
	 */
	public int getMissingClassCount() {
		return bugReporter.getMissingClassCount();
	}

	/**
	 * Set the FindBugs home directory.
	 */
	public static void setHome(String home) {
		FindBugs.home = home;
	}

	/**
	 * Get the FindBugs home directory.
	 */
	public static String getHome() {
		if (home == null) {
			home = System.getProperty("findbugs.home");
			if (home == null) {
				System.err.println("Error: The findbugs.home property is not set!");
			}
		}
		return home;
	}

	/* ----------------------------------------------------------------------
	 * Private methods
	 * ---------------------------------------------------------------------- */

	/**
	 * Create Detectors for each DetectorFactory which is enabled.
	 * This will populate the detectors array.
	 */
	private void createDetectors() {
		ArrayList<Detector> result = new ArrayList<Detector>();

		Iterator<DetectorFactory> i = DetectorFactoryCollection.instance().factoryIterator();
		while (i.hasNext()) {
			DetectorFactory factory = i.next();
			if (factory.getPlugin().isEnabled() && factory.isEnabled()) {
				Detector detector = factory.create(bugReporter);
				detector.setAnalysisContext(analysisContext);
				result.add(detector);
			}
		}

		detectors = result.toArray(new Detector[result.size()]);
	}

	/**
	 * Clear the Repository and update it to reflect the classpath
	 * specified by the current project.
	 */
	private void clearRepository() {
		// Purge repository of previous contents
		Repository.clearCache();

		// Clear InnerClassAccessMap cache.
		InnerClassAccessMap.instance().clearCache();

		// Create a URLClassPathRepository based on the current project,
		// and make it current.
		URLClassPathRepository repository = new URLClassPathRepository(); 
		Repository.setRepository(repository);
	}
	
	/**
	 * Based on Project settings, set the classpath to be used
	 * by the Repository when looking up classes.
	 * @throws IOException
	 */
	private void setRepositoryClassPath(List<String> additionalAuxClasspathEntryList)
			throws IOException {

		URLClassPathRepository repository =
			(URLClassPathRepository) Repository.getRepository();

		// Set aux classpath entries
		addCollectionToClasspath(project.getAuxClasspathEntryList(), repository);
		
		// Set implicit classpath entries
		addCollectionToClasspath(project.getImplicitClasspathEntryList(), repository);

		// Add "extra" aux classpath entries needed to ensure that
		// skipped classes can be referenced.
		addCollectionToClasspath(additionalAuxClasspathEntryList, repository);

		// Add system classpath entries
		repository.addSystemClasspathComponents();
	}

	/**
	 * Add all classpath entries in given Collection to the given
	 * URLClassPathRepository.  Missing entries are not fatal:
	 * we'll log them as analysis errors, but the analysis can
	 * continue.
	 * 
	 * @param collection classpath entries to add
	 * @param repository URLClassPathRepository to add the entries to
	 */
	private void addCollectionToClasspath(Collection<String> collection,
			URLClassPathRepository repository) {
		for (Iterator<String> i = collection.iterator(); i.hasNext(); ) {
			String entry = i.next();
			try {
				repository.addURL(entry);
			} catch (IOException e) {
				bugReporter.logError("Warning: could not add URL "  +
					entry + " to classpath: " + e.toString());
			}
		}
	}

	/**
	 * Add all classes contained in given file or directory to the BCEL Repository.
	 *
	 * @param item                work list item representing the file, which may be a jar/zip
	 *                            archive, a single class file, or a directory to be recursively
	 *                            searched for class files
	 * @param archiveWorkList     work list of archives to analyze: this method
	 *                            may add to the work list if it finds nested archives
	 * @param repositoryClassList a List to which all classes found in
	 *                            the archive or directory are added, so we later know
	 *                            which files to analyze
	 */
	private void scanArchiveOrDirectory(ArchiveWorkListItem item,
			LinkedList<ArchiveWorkListItem> archiveWorkList, List<String> repositoryClassList,
			List<String> additionalAuxClasspathEntryList)
	        throws IOException, InterruptedException {

		String fileName = item.getFileName();
		ClassProducer classProducer = null;

		try {
			// Create a URL for the filename.
			// The protocol defaults to "file" if not explicitly
			// specified in the filename.
			String protocol = getURLProtocol(fileName);
			if (protocol == null) {
				protocol = "file";
				fileName = "file:" + fileName;
			}
			URL url = new URL(fileName);

			// Figure out the file extension
			String fileExtension = null;
			int lastDot = fileName.lastIndexOf('.');
			if (lastDot >= 0) {
				fileExtension = fileName.substring(lastDot);
			}

			// Create the ClassProducer
			if (fileExtension != null && isArchiveExtension(fileExtension))
				classProducer = new ZipClassProducer(url, archiveWorkList, additionalAuxClasspathEntryList);
			else if (fileExtension != null && fileExtension.equals(".class"))
				classProducer = new SingleClassProducer(url);
			else if (protocol.equals("file")) {
				// Assume it's a directory
				fileName = fileName.substring("file:".length());
				File dir = new File(fileName);
				if (!dir.isDirectory())
					throw new IOException("Path " + fileName + " is not an archive, class file, or directory");
				classProducer = new DirectoryClassProducer(fileName, additionalAuxClasspathEntryList);
			} else
				throw new IOException("URL " + fileName + " is not an archive, class file, or directory");

			// Load all referenced classes into the Repository
			for (; ;) {
				if (Thread.interrupted())
					throw new InterruptedException();
				try {
					JavaClass jclass = classProducer.getNextClass();
					if (jclass == null)
						break;
					if (DEBUG) System.out.println("Scanned " + jclass.getClassName());
					Repository.addClass(jclass);
					repositoryClassList.add(jclass.getClassName());
				} catch (ClassFormatException e) {
					e.printStackTrace();
					bugReporter.logError(e.getMessage());
				}
			}

			if (item.isExplicit())
				progressCallback.finishArchive();

			// If the archive or directory scanned contained source files,
			// add it to the end of the source path.
			if (classProducer.containsSourceFiles())
				project.addSourceDir(fileName);

		} catch (IOException e) {
			// You'd think that the message for a FileNotFoundException would include
			// the filename, but you'd be wrong.  So, we'll add it explicitly.
			throw new IOException("Could not analyze " + fileName + ": " + e.getMessage());
		} finally {
			if (classProducer != null) {
				classProducer.close();
			}
		}
	}

	/**
	 * Examine a single class by invoking all of the Detectors on it.
	 *
	 * @param className the fully qualified name of the class to examine
	 */
	private void examineClass(String className) throws InterruptedException {
		if (DEBUG) System.out.println("Examining class " + className);

		this.currentClass = className;

		try {
			JavaClass javaClass = Repository.lookupClass(className);

			// Notify ClassObservers
			for (Iterator<ClassObserver> i = classObserverList.iterator(); i.hasNext();) {
				i.next().observeClass(javaClass);
			}

			// Create a ClassContext for the class
			ClassContext classContext = analysisContext.getClassContext(javaClass);

			// Run the Detectors
			for (int i = 0; i < detectors.length; ++i) {
				if (Thread.interrupted())
					throw new InterruptedException();
				Detector detector = detectors[i];
				try {
					if (DEBUG) System.out.println("  running " + detector.getClass().getName());
					detector.visitClassContext(classContext);
				} catch (AnalysisException e) {
					reportRecoverableDetectorException(className, detector, e);
				} catch (ArrayIndexOutOfBoundsException e) {
					reportRecoverableDetectorException(className, detector, e);
				} catch (ClassCastException e) {
					reportRecoverableDetectorException(className, detector, e);
				}
			}
		} catch (ClassNotFoundException e) {
			// This should never happen unless there are bugs in BCEL.
			bugReporter.reportMissingClass(e);
			reportRecoverableException(className, e);
		} catch (ClassFormatException e) {
			reportRecoverableException(className, e);
		}

		progressCallback.finishClass();
	}

	private void reportRecoverableException(String className, Exception e) {
		if (DEBUG) {
			e.printStackTrace();
		}
		bugReporter.logError("Exception analyzing " + className + ": " + e.toString());
	}

	private void reportRecoverableDetectorException(String className, Detector detector, Exception e) {
		if (DEBUG) {
			e.printStackTrace();
		}
		bugReporter.logError("Exception analyzing " + className +
			" using detector " + detector.getClass().getName() +
			": " + e.toString());
	}

	/**
	 * Call report() on all detectors, to give them a chance to
	 * report any accumulated bug reports.
	 */
	private void reportFinal() throws InterruptedException {
		for (int i = 0; i < detectors.length; ++i) {
			if (Thread.interrupted())
				throw new InterruptedException();
			detectors[i].report();
		}
	}

	/**
	 * Get the file extension of given fileName.
	 * @return the file extension, or null if there is no file extension
	 */
	static String getFileExtension(String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		return (lastDot >= 0)
			? fileName.substring(lastDot)
			: null;
	}

	/**
	 * Get the URL protocol of given URL string.
	 * @param urlString the URL string
	 * @return the protocol name ("http", "file", etc.), or null if there is no protocol
	 */
	static String getURLProtocol(String urlString) {
		String protocol = null;
		int firstColon = urlString.indexOf(':');
		if (firstColon >= 0) {
			String specifiedProtocol = urlString.substring(0, firstColon);
			if (knownURLProtocolSet.contains(specifiedProtocol))
				protocol = specifiedProtocol;
		}
		return protocol;
	}

	/**
	 * Determine if given file extension indicates an archive file.
	 * 
	 * @param fileExtension the file extension (e.g., ".jar")
	 * @return true if the file extension indicates an archive,
	 *   false otherwise
	 */
	static boolean isArchiveExtension(String fileExtension) {
		return archiveExtensionSet.contains(fileExtension);
	}

	/**
	 * Parse the data for a class to create a JavaClass object.
	 */
	private static JavaClass parseClass(String archiveName, InputStream in, String fileName)
	        throws IOException {
		if (DEBUG) System.out.println("About to parse " + fileName + " in " + archiveName);
		return parseFromStream(in, fileName);
	}

	/**
	 * Parse the data for a class to create a JavaClass object.
	 */
	private static JavaClass parseClass(URL url) throws IOException {
		if (DEBUG) System.out.println("About to parse " + url.toString());
		InputStream in = url.openStream();
		return parseFromStream(in, url.toString());
	}

	/**
	 * Parse an input stream to produce a JavaClass object.
	 * Makes sure that the input stream is closed no
	 * matter what.
	 */
	private static JavaClass parseFromStream(InputStream in, String fileName) throws IOException {
		boolean parsed = false;
		try {
			JavaClass jclass = new ClassParser(in, fileName).parse();
			parsed = true;
			return jclass;
		} finally {
			if (!parsed) {
				// BCEL does not close the input stream unless
				// parsing was successful.
				try {
					in.close();
				} catch (IOException ignore) {
					// Ignore
				}
			}
		}
	}

	/**
	 * Process -bugCategories option.
	 * @param categories comma-separated list of bug categories
	 * @return Set of categories to be used
	 */
	private static Set<String> handleBugCategories(String categories) {
		// Parse list of bug categories
		HashSet<String> categorySet = new HashSet<String>();
		StringTokenizer tok = new StringTokenizer(categories, ",");
		while (tok.hasMoreTokens()) {
			categorySet.add(tok.nextToken());
		}

		// Enable only those detectors that can emit those categories
		// (and the ones that produce unknown bug patterns, just to be safe).
		// Skip disabled detectors, though.
		for (Iterator<DetectorFactory> i = DetectorFactoryCollection.instance().factoryIterator(); i.hasNext();) {
			DetectorFactory factory = i.next();
			if (!factory.isEnabled())
				continue;
			Collection<BugPattern> reported = factory.getReportedBugPatterns();
			boolean enable = false;
			if (reported.isEmpty()) {
				// Don't know what bug patterns are produced by this detector
				if (DEBUG) System.out.println("Unknown bug patterns for " + factory.getShortName());
				enable = true;
			} else {
				for (Iterator<BugPattern> j = reported.iterator(); j.hasNext();) {
					BugPattern bugPattern = j.next();
					if (categorySet.contains(bugPattern.getCategory())) {
						if (DEBUG)
							System.out.println("MATCH ==> " + categorySet +
							        " -- " + bugPattern.getCategory());
						enable = true;
						break;
					}
				}
			}
			if (DEBUG && enable) {
				System.out.println("Enabling " + factory.getShortName());
			}
			factory.setEnabled(enable);
		}

		return categorySet;
	}

	/* ----------------------------------------------------------------------
	 * main() method
	 * ---------------------------------------------------------------------- */

	public static void main(String[] argv) {
		try {
			FindBugsCommandLine commandLine = new FindBugsCommandLine();
			FindBugs findBugs = createEngine(commandLine, argv);

			try {
				runMain(findBugs, commandLine);
			} catch (RuntimeException e) {
				System.err.println("Fatal exception: " + e.toString());
				String currentClass = findBugs.getCurrentClass();
				if (currentClass != null) {
					System.err.println("\tWhile analyzing " + currentClass);
				}
				e.printStackTrace();
				System.err.println("Please report the failure to " + Version.SUPPORT_EMAIL);
				System.exit(1);
			}

		} catch (java.io.IOException e) {
			// Probably a missing file
			if (DEBUG) {
				e.printStackTrace();
			}
			System.err.println("IO Error: " + e.getMessage());
			System.exit(1);
		} catch (FilterException e) {
			System.err.println("Filter exception: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			// Probably an illegal command line argument
			System.err.println("Illegal argument: " + e.getMessage());
			System.exit(1);
		}
	}

	private static FindBugs createEngine(FindBugsCommandLine commandLine, String[] argv)
	        throws java.io.IOException, FilterException {
		
		// Expand option files in command line.
		// An argument beginning with "@" is treated as specifying
		// the name of an option file.
		// Each line of option files are treated as a single argument.
		// Blank lines and comment lines (beginning with "#")
		// are ignored.
		argv = CommandLine.expandOptionFiles(argv, true, true);
		
		int argCount = commandLine.parse(argv);

		Project project = commandLine.getProject();
		for (int i = argCount; i < argv.length; ++i)
			project.addFile(argv[i]);

		if (project.getFileCount() == 0) {
			System.out.println("FindBugs version " + Version.RELEASE + ", " + Version.WEBSITE);
			System.out.println("Usage: findbugs -textui [options...] [jar/zip/class files, directories...]");
			System.out.println("Options:");
			commandLine.printUsage(System.out);
			System.exit(0);
		}

		return commandLine.createEngine();
	}

	private static void runMain(FindBugs findBugs, FindBugsCommandLine commandLine)
	        throws java.io.IOException, RuntimeException, FilterException {
		try {
			findBugs.execute();
		} catch (InterruptedException e) {
			// Not possible when running from the command line
		}

		int bugCount = findBugs.getBugCount();
		int missingClassCount = findBugs.getMissingClassCount();
		int errorCount = findBugs.getErrorCount();

		if (!commandLine.quiet() || commandLine.setExitCode()) {
			if (bugCount > 0)
				System.err.println("Warnings generated: " + bugCount);
			if (missingClassCount > 0)
				System.err.println("Missing classes: " + missingClassCount);
			if (errorCount > 0)
				System.err.println("Analysis errors: " + errorCount);
		}

		if (commandLine.setExitCode()) {
			int exitCode = 0;
			if (errorCount > 0)
				exitCode |= ERROR_FLAG;
			if (missingClassCount > 0)
				exitCode |= MISSING_CLASS_FLAG;
			if (bugCount > 0)
				exitCode |= BUGS_FOUND_FLAG;

			System.exit(exitCode);
		}
	}
}

// vim:ts=4
