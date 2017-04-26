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

import junit.framework.*;

import org.parabuild.ci.ServersideTestCase;
import org.parabuild.ci.TestHelper;
import org.parabuild.ci.build.*;
import org.parabuild.ci.configuration.*;
import org.parabuild.ci.object.*;
import org.parabuild.ci.webui.admin.*;

/**
 * Tests BuildHeaderPanel
 */
public class SSTestBuildHeaderPanel extends ServersideTestCase {

  private BuildHeaderPanel buildHeaderPanel;
  private ConfigurationManager cm;


  public SSTestBuildHeaderPanel(final String s) {
    super(s);
  }


  /**
   * Bug #513 - editing build configuration caused build to become
   * inactive.
   */
  public void test_bug513() throws Exception {
    // preExecute - set startup status as active
    BuildConfig buildConfig = cm.getBuildConfiguration(TestHelper.TEST_CVS_VALID_BUILD_ID);
    ActiveBuild activeBuild = cm.getActiveBuild(buildConfig.getBuildID());
    activeBuild.setStartupStatus(BuildStatus.ACTIVE_VALUE);
    cm.update(activeBuild);
    // load
    buildHeaderPanel.setBuildConfig(buildConfig);
    // save
    buildHeaderPanel.save();
    // assert config is still active
    buildConfig = cm.getBuildConfiguration(TestHelper.TEST_CVS_VALID_BUILD_ID);
    activeBuild = cm.getActiveBuild(buildConfig.getBuildID());
    assertEquals(BuildStatus.ACTIVE_VALUE, activeBuild.getStartupStatus());
  }


  /**
   * Required by JUnit
   */
  public static TestSuite suite() {
    return new TestSuite(SSTestBuildHeaderPanel.class);
  }


  protected void setUp() throws Exception {
    super.setUp();
    cm = ConfigurationManager.getInstance();
    buildHeaderPanel = new BuildHeaderPanel();
  }
}