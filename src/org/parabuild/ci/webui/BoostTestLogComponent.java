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

import org.parabuild.ci.archive.ArchiveManager;
import org.parabuild.ci.common.IoUtils;
import org.parabuild.ci.object.StepLog;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

/**
 * BoostTestLogComponent is a custom leaf component responsible for
 * displaying build logs presented as unaltered readers.
 *
 * @see org.parabuild.ci.webui.common.BasePage#executePage
 */
public final class BoostTestLogComponent extends AbstractXSLRendererComponent {

  private static final long serialVersionUID = -4256625468445813797L; // NOPMD

  private ArchiveManager archiveManager = null;
  private StepLog stepLog = null;


  /**
   * Constructor
   */
  public BoostTestLogComponent(final ArchiveManager archiveManager, final StepLog stepLog) {
    this.archiveManager = archiveManager;
    this.stepLog = stepLog;
  }


  protected StreamSource xmlSource() throws IOException {
    return new StreamSource(archiveManager.getArchivedLogInputStream(stepLog));
  }


  protected StreamSource xslSource() throws IOException {
    return new StreamSource(new StringReader(IoUtils.getResourceAsString("boost-test-log-report.xsl")));
  }
}
