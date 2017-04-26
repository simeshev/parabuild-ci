/*
 * Parabuild CI licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parabuild.ci.webui;

import java.text.*;
import java.util.*;

import junit.framework.*;

import org.parabuild.ci.webui.admin.*;

/**
 * Tests home page
 */
public class SATestDateFormatDropdown extends TestCase {

  public SATestDateFormatDropdown(final String s) {
    super(s);
  }


  /**
   * Test that FORMATS list is valid
   */
  public void test_formats() throws Exception {
    final Date date = new Date(System.currentTimeMillis());
    for (int i = 0; i < DateFormatDropdown.getFormats().length; i++) {
      final SimpleDateFormat formatter = new SimpleDateFormat(DateFormatDropdown.getFormats()[i], Locale.US);
      assertTrue(formatter.format(date).length() > 0);
    }
  }


  /**
   * Required by JUnit
   */
  public static TestSuite suite() {
    return new TestSuite(SATestDateFormatDropdown.class);
  }
}