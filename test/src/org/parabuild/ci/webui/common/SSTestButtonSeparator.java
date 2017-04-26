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
package org.parabuild.ci.webui.common;

import org.apache.cactus.*;

import junit.framework.*;

/**
 * Tests ButtonSeparator
 */
public final class SSTestButtonSeparator extends ServletTestCase {

  /** @noinspection UNUSED_SYMBOL,FieldCanBeLocal*/
  private ButtonSeparator buttonSeparator = null;


  public SSTestButtonSeparator(final String s) {
    super(s);
  }


  public void test_create() {
    // do nothing, created in setUp
  }


  /**
   * Required by JUnit
   */
  public static TestSuite suite() {
    return new TestSuite(SSTestButtonSeparator.class);
  }


  protected void setUp() throws Exception {
    super.setUp();
    buttonSeparator = new ButtonSeparator();
  }
}