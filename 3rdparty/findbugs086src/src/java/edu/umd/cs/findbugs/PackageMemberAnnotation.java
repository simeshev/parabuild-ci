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

/**
 * Abstract base class for BugAnnotations describing constructs
 * which are contained in a Java package.  Specifically,
 * this includes classes, methods, and fields.
 *
 * @author David Hovemeyer
 * @see BugAnnotation
 */
public abstract class PackageMemberAnnotation implements BugAnnotation {
	protected String className;
	protected String description;

	/**
	 * Constructor.
	 *
	 * @param className name of the class
	 */
	public PackageMemberAnnotation(String className, String description) {
		this.className = className;
		this.description = description;
	}

	/**
	 * Get the class name.
	 */
	public final String getClassName() {
		return className;
	}

	/**
	 * Get the package name.
	 */
	public final String getPackageName() {
		int lastDot = className.lastIndexOf('.');
		if (lastDot < 0)
			return "";
		else
			return className.substring(0, lastDot);
	}

	/**
	 * Format the annotation.
	 * Note that this version (defined by PackageMemberAnnotation)
	 * only handles the "class" and "package" keys, and calls
	 * formatPackageMember() for all other keys.
	 *
	 * @param key the key
	 * @return the formatted annotation
	 */
	public final String format(String key) {
		if (key.equals("class"))
			return className;
		else if (key.equals("package"))
			return getPackageName();
		else
			return formatPackageMember(key);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Shorten a type name of remove extraneous components.
	 * Candidates for shortening are classes in same package as this annotation and
	 * classes in the <code>java.lang</code> package.
	 */
	protected static String shorten(String pkgName, String typeName) {
		int index = typeName.lastIndexOf('.');
		if (index >= 0) {
			String otherPkg = typeName.substring(0, index);
			if (otherPkg.equals(pkgName) || otherPkg.equals("java.lang"))
				typeName = typeName.substring(index + 1);
		}
		return typeName;
	}

	/**
	 * Do default and subclass-specific formatting.
	 *
	 * @param key the key specifying how to do the formatting
	 */
	protected abstract String formatPackageMember(String key);

	/**
	 * All PackageMemberAnnotation object share a common toString() implementation.
	 * It uses the annotation description as a pattern for FindBugsMessageFormat,
	 * passing a reference to this object as the single message parameter.
	 */
	public String toString() {
		String pattern = I18N.instance().getAnnotationDescription(description);
		FindBugsMessageFormat format = new FindBugsMessageFormat(pattern);
		return format.format(new BugAnnotation[]{this});
	}
}

// vim:ts=4
