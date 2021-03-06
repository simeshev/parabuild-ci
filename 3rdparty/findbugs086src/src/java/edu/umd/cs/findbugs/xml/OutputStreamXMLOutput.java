/*
 * XML input/output support for FindBugs
 * Copyright (C) 2004, University of Maryland
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

package edu.umd.cs.findbugs.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.nio.charset.Charset;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Write XML to an output stream.
 * 
 * @author David Hovemeyer
 */
public class OutputStreamXMLOutput implements XMLOutput {
	private static final String OPENING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	private static final MetaCharacterMap textMetaCharacterMap = new MetaCharacterMap();
	static {
		textMetaCharacterMap.addMeta('<', "&lt;");
		textMetaCharacterMap.addMeta('>', "&gt;");
		textMetaCharacterMap.addMeta('&', "&amp;");
	}

	private class WriterQuoteMetaCharacters extends QuoteMetaCharacters {
		public WriterQuoteMetaCharacters(String text) {
			super(text, textMetaCharacterMap);
		}

		public void emitLiteral(String s) throws IOException {
			out.write(s);
			newLine = s.endsWith("\n");
		}
	}

	private Writer out;
	private int nestingLevel;
	private boolean newLine;

	/**
	 * Constructor.
	 * @param os OutputStream to write XML output to
	 */
	public OutputStreamXMLOutput(OutputStream os) {
		this.out = new OutputStreamWriter(os, Charset.forName("UTF-8"));
		this.nestingLevel = 0;
		this.newLine = true;
	}

	public void beginDocument() throws IOException {
		out.write(OPENING);
		out.write("\n");
		newLine = true;
	}

	public void openTag(String tagName) throws IOException {
		emitTag(tagName, false);
	}

	public void openTag(String tagName, XMLAttributeList attributeList) throws IOException {
		emitTag(tagName, attributeList.toString(), false);
	}

	public void openCloseTag(String tagName) throws IOException {
		emitTag(tagName, true);
	}

	public void openCloseTag(String tagName, XMLAttributeList attributeList) throws IOException {
		emitTag(tagName, attributeList.toString(), true);
	}

	private void emitTag(String tagName, boolean close) throws IOException {
		indent();
		++nestingLevel;
		out.write("<" + tagName);
		if (close) {
			out.write("/>\n");
			--nestingLevel;
			newLine = true;
		} else {
			out.write(">");
			newLine = false;
		}
	}

	private void emitTag(String tagName, String attributes, boolean close) throws IOException {
		indent();
		++nestingLevel;
		out.write("<" + tagName);
		attributes = attributes.trim();
		if (attributes.length() > 0) {
			out.write(" ");
			out.write(attributes);
		}
		if (close) {
			out.write("/>\n");
			--nestingLevel;
			newLine = true;
		} else {
			out.write(">");
			newLine = false;
		}
	}

	public void closeTag(String tagName) throws IOException {
		--nestingLevel;
		if (newLine)
			indent();
		out.write("</" + tagName + ">\n");
		newLine = true;
	}

	public void writeText(String text) throws IOException {
		new WriterQuoteMetaCharacters(text).process();
	}

	public void writeCDATA(String cdata) throws IOException {
		// We just trust fate that the characters being written
		// don't contain the string "]]>"
		out.write("<![CDATA[");
		out.write(cdata);
		out.write("]]>");
		newLine = false;
	}

	public void finish() throws IOException {
		out.close();
	}

	private void indent() throws IOException {
		if (!newLine)
			out.write("\n");
		for (int i = 0; i < nestingLevel; ++i) {
			out.write("  ");
		}
	}
}

// vim:ts=4
