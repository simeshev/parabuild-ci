/*
 * FindBugs - Find bugs in Java programs
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

package edu.umd.cs.findbugs.config;

import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.I18N;

import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ProjectFilterSettingsTest extends TestCase {
	ProjectFilterSettings plain;
	ProjectFilterSettings otherPlain;
	ProjectFilterSettings changed;
	ProjectFilterSettings changed2;
	ProjectFilterSettings changed3;
	ProjectFilterSettings changed4;

	protected void setUp() {
		plain = ProjectFilterSettings.createDefault();

		otherPlain = ProjectFilterSettings.createDefault();

		changed = ProjectFilterSettings.createDefault();
		changed.setMinPriority("High");

		changed2 = ProjectFilterSettings.createDefault();
		changed2.removeCategory("MALICIOUS_CODE");

		changed3 = ProjectFilterSettings.createDefault();
		changed3.addCategory("FAKE_CATEGORY");

		changed4 = ProjectFilterSettings.createDefault();
		changed4.setMinPriority("High");
		changed4.removeCategory("MALICIOUS_CODE");
		changed4.addCategory("FAKE_CATEGORY");
		
	}

	public void testPlainPrio() {
		Assert.assertTrue(plain.getMinPriority().equals(ProjectFilterSettings.DEFAULT_PRIORITY));
	}

	public void testPlainCategories() {
		int count = 0;
		Iterator<String> i = I18N.instance().getBugCategories().iterator();
		while (i.hasNext()) {
			String category = i.next();
			Assert.assertTrue(plain.containsCategory(category));
			++count;
		}
		Assert.assertTrue(plain.getActiveCategorySet().size() == count);
	}

	public void testAddCategory() {
		Assert.assertFalse(plain.containsCategory("FAKE_CATEGORY"));
		plain.addCategory("FAKE_CATEGORY");
		Assert.assertTrue(plain.containsCategory("FAKE_CATEGORY"));
	}

	public void testRemoveCategory() {
		Assert.assertTrue(plain.containsCategory("MALICIOUS_CODE"));
		plain.removeCategory("MALICIOUS_CODE");
		Assert.assertFalse(plain.containsCategory("MALICIOUS_CODE"));
	}

	public void testSetMinPriority() {
		plain.setMinPriority("High");
		Assert.assertTrue(plain.getMinPriority().equals("High"));
		Assert.assertTrue(plain.getMinPriorityAsInt() == Detector.HIGH_PRIORITY);
		plain.setMinPriority("Medium");
		Assert.assertTrue(plain.getMinPriority().equals("Medium"));
		Assert.assertTrue(plain.getMinPriorityAsInt() == Detector.NORMAL_PRIORITY);
		plain.setMinPriority("Low");
		Assert.assertTrue(plain.getMinPriority().equals("Low"));
		Assert.assertTrue(plain.getMinPriorityAsInt() == Detector.LOW_PRIORITY);
	}

	public void testEquals() {
		Assert.assertEquals(plain, otherPlain);

		Assert.assertFalse(plain.equals(changed));
		Assert.assertFalse(changed.equals(plain));

		Assert.assertFalse(plain.equals(changed2));
		Assert.assertFalse(changed2.equals(plain));

		Assert.assertFalse(plain.equals(changed3));
		Assert.assertFalse(changed3.equals(plain));

		Assert.assertFalse(plain.equals(changed4));
		Assert.assertFalse(changed4.equals(plain));
	}

	public void testEncodeDecode() {
		ProjectFilterSettings copyOfPlain =
			ProjectFilterSettings.fromEncodedString(plain.toEncodedString());
		Assert.assertEquals(plain, copyOfPlain);

		ProjectFilterSettings copyOfChanged4 =
			ProjectFilterSettings.fromEncodedString(changed4.toEncodedString());
		Assert.assertEquals(changed4, copyOfChanged4);
	}
}

// vim:ts=4
