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
package org.parabuild.ci.webui.admin;

import org.parabuild.ci.object.LogConfig;

/**
 * Panel to configure a GoogleTest XML log.
 * <p/>
 * This panel does not present any additional controls because all we
 * need for GoogleTest is path to a file with with GoogleTest XML logs.
 *
 * @see AbstractLogConfigPanel
 */
public final class GoogleTestLogConfigPanel extends AbstractLogConfigPanel {

  private static final long serialVersionUID = -1731911756101403145L; // NOPMD


  /**
   * Creates message panel without title.
   */
  public GoogleTestLogConfigPanel() {
    super(false); // no content border
    super.setLogType(LogConfig.LOG_TYPE_GOOGLETEST_XML_FILE);
    super.setLogDecription("GoogleTest Log");
  }


  public boolean validateProperties() {
    return getErrors().isEmpty();
  }
}