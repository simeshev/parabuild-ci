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

package edu.umd.cs.findbugs.detect;

import java.util.*;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.visitclass.Constants2;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public class FindDoubleCheck extends BytecodeScanningDetector implements Constants2 {
	static final boolean debug = false;
	int stage = 0;
	int startPC, endPC;
	int count;
	boolean sawMonitorEnter;
	HashSet<FieldAnnotation> fields = new HashSet<FieldAnnotation>();
	HashSet<FieldAnnotation> twice = new HashSet<FieldAnnotation>();
	FieldAnnotation pendingFieldLoad;

	int countSinceGetReference;
	int countSinceGetBoolean;
	private BugReporter bugReporter;

	public FindDoubleCheck(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}

	public void visit(Method obj) {
		if (debug) System.out.println(getFullyQualifiedMethodName());
		super.visit(obj);
		fields.clear();
		twice.clear();
		stage = 0;
		count = 0;
		countSinceGetReference = 1000;
		countSinceGetBoolean = 1000;
		sawMonitorEnter = false;
		pendingFieldLoad = null;
	}

	public void sawOpcode(int seen) {
		if (debug) System.out.println(getPC() + "	" + OPCODE_NAMES[seen] + "	" + stage + "	" + count + "	" + countSinceGetReference);

		if (seen == MONITORENTER) sawMonitorEnter = true;
		if (seen == GETFIELD || seen == GETSTATIC) {
			pendingFieldLoad = FieldAnnotation.fromReferencedField(this);
			if (debug) System.out.println("	" + pendingFieldLoad);
			String sig = getSigConstantOperand();
			if (sig.equals("Z")) {
				countSinceGetBoolean = 0;
				countSinceGetReference++;
			} else if (sig.startsWith("L") || sig.startsWith("[")) {
				countSinceGetBoolean++;
				countSinceGetReference = 0;
			}
		} else {
			countSinceGetReference++;
		}
		switch (stage) {
		case 0:
			if (((seen == IFNULL || seen == IFNONNULL) && countSinceGetReference < 5)
			        || ((seen == IFEQ || seen == IFNE) && countSinceGetBoolean < 5)) {
				int b = getBranchOffset();
				if (debug) {
					System.out.println("branch offset is : " + b);
				}
				if (b > 0
				        && !(seen == IFNULL && b > 9)
				        && !(seen == IFEQ && (b > 9 && b < 34))
				        && !(seen == IFNE && (b > 9 && b < 34))
				        && (!sawMonitorEnter)) {
					fields.add(pendingFieldLoad);
					startPC = getPC();
					stage = 1;
				}
			}
			count = 0;
			break;
		case 1:
			if (seen == MONITORENTER) {
				stage = 2;
				count = 0;
			} else if (((seen == IFNULL || seen == IFNONNULL) && countSinceGetReference < 5)
			        || ((seen == IFEQ || seen == IFNE) && countSinceGetBoolean < 5)) {
				int b = getBranchOffset();
				if (b > 0 && (seen == IFNONNULL || b < 10)) {
					fields.add(pendingFieldLoad);
					startPC = getPC();
					count = 0;
				}
			} else {
				count++;
				if (count > 10) stage = 0;
			}
			break;
		case 2:
			if (((seen == IFNULL || seen == IFNONNULL) && countSinceGetReference < 5)
			        || ((seen == IFEQ || seen == IFNE) && countSinceGetBoolean < 5)) {
				if (getBranchOffset() >= 0 && fields.contains(pendingFieldLoad)) {
					endPC = getPC();
					stage++;
					twice.add(pendingFieldLoad);
					break;
				}
			}
			count++;
			if (count > 10) stage = 0;
			break;
		case 3:
			if (seen == PUTFIELD || seen == PUTSTATIC) {
				FieldAnnotation f = FieldAnnotation.fromReferencedField(this);
				if (debug) System.out.println("	" + f);
				if (twice.contains(f) && !getNameConstantOperand().startsWith("class$")
				        && !getSigConstantOperand().equals("Ljava/lang/String;")) {
					Field declaration = findField(getClassConstantOperand(), getNameConstantOperand());
					/*
					System.out.println(f);
					System.out.println(declaration);
					System.out.println(getSigConstantOperand());
					*/
					if (declaration == null || !declaration.isVolatile())
						bugReporter.reportBug(new BugInstance(this, "DC_DOUBLECHECK", NORMAL_PRIORITY)
						        .addClassAndMethod(this)
						        .addField(f).describe("FIELD_ON")
						        .addSourceLineRange(this, startPC, endPC));
					stage++;
				}
			}
			break;
		default:
		}
	}

	Field findField(String className, String fieldName) {
		try {
			// System.out.println("Looking for " + className);
			JavaClass fieldDefinedIn = getThisClass();
			if (!className.equals(getClassName())) {
				// System.out.println("Using repository to look for " + className);

				fieldDefinedIn = Repository.lookupClass(className);
			}
			Field[] f = fieldDefinedIn.getFields();
			for (int i = 0; i < f.length; i++)
				if (f[i].getName().equals(fieldName)) {
					// System.out.println("Found " + f[i]);
					return f[i];
				}
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

}
