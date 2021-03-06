/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2004, Garvin LeClaire <garvin.leclaire@insightbb.com>
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


import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import org.apache.bcel.classfile.JavaClass;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * BugReporter to output warnings in xdocs format for Maven.
 *
 * @author Garvin LeClaire
 */
public class XDocsBugReporter extends TextUIBugReporter {
	private SortedBugCollection bugCollection = new SortedBugCollection();
	private Project project;
	private Document document;
	private Element root;

	private static final String ROOT_ELEMENT_NAME = "BugCollection";
	private static final String PROJECT_ELEMENT_NAME = "Project";
	private static final String ERRORS_ELEMENT_NAME = "Errors";
	private static final String ANALYSIS_ERROR_ELEMENT_NAME = "AnalysisError";
	private static final String MISSING_CLASS_ELEMENT_NAME = "MissingClass";
	private static final String SUMMARY_HTML_ELEMENT_NAME = "SummaryHTML";

	private static final String ELEMENT_NAME = "BugInstance";
	private static final String FILE_ELEMENT_NAME = "file";


	public XDocsBugReporter(Project project) {
		this.project = project;

		this.document = DocumentHelper.createDocument();
		this.root = document.addElement(ROOT_ELEMENT_NAME);


	}

	public void observeClass(JavaClass javaClass) {
	}

	public void logError(String message) {
		bugCollection.addError(message);
		super.logError(message);
	}

	public void reportMissingClass(ClassNotFoundException ex) {
		bugCollection.addMissingClass(getMissingClassName(ex));
		super.reportMissingClass(ex);
	}

	public void doReportBug(BugInstance bugInstance) {
		if (bugCollection.add(bugInstance)) {
			printBug(bugInstance);
			notifyObservers(bugInstance);
		}
	}

	protected void printBug(BugInstance bugInstance) {
		try {

			toElement(bugInstance);


		} catch (Exception e) {
			logError("Couldn't add Element: " + e.toString());
		}
	}

	public void finish() {

		try {
			writeXML(outputStream, project);
		} catch (Exception e) {
			logError("Couldn't write XML output: " + e.toString());
		}
		outputStream.close();
	}

	private void writeXML(OutputStream out, Project project) throws IOException {
		Document document = endDocument(project);

		XMLWriter writer = new XMLWriter(out, OutputFormat.createPrettyPrint());
		writer.write(document);
	}

	private Document endDocument(Project project) {

		// Save the error information
		Element errorsElement = root.addElement(ERRORS_ELEMENT_NAME);
		for (Iterator<String> i = bugCollection.errorIterator(); i.hasNext();) {
			errorsElement.addElement(ANALYSIS_ERROR_ELEMENT_NAME).setText(i.next());
		}
		for (Iterator<String> i = bugCollection.missingClassIterator(); i.hasNext();) {
			errorsElement.addElement(MISSING_CLASS_ELEMENT_NAME).setText(i.next());
		}

		return document;
	}

	public void toElement(BugInstance bugInstance) {

		String className = bugInstance.getPrimaryClass().getClassName();
		Element element = (Element) root.selectSingleNode(FILE_ELEMENT_NAME + "[@classname='" + className + "']");

		if (element == null) {
			element = root.addElement(FILE_ELEMENT_NAME);
			element.addAttribute("classname", className);
		}

		element = element.addElement(ELEMENT_NAME);


		element.addAttribute("type", bugInstance.getType());

		switch (bugInstance.getPriority()) {
		case Detector.EXP_PRIORITY:
			element.addAttribute("priority", "Experimental");
			break;
		case Detector.LOW_PRIORITY:
			element.addAttribute("priority", "Low");
			break;
		case Detector.NORMAL_PRIORITY:
			element.addAttribute("priority", "Normal");
			break;
		case Detector.HIGH_PRIORITY:
			element.addAttribute("priority", "High");
			break;
		}

		element.addAttribute("message", bugInstance.getMessage());

		SourceLineAnnotation line =
		        bugInstance.getPrimarySourceLineAnnotation();
		if (line == null) {
			element.addAttribute("line", "0");
		} else {
			element.addAttribute("line", Integer.toString(line.getStartLine()));
		}


		return;
	}

}

// vim:ts=3
