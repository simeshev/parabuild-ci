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
package org.parabuild.ci.versioncontrol.mks;

import org.parabuild.ci.object.*;
import org.parabuild.ci.versioncontrol.mks.MKSCommandParameters;

/**
 * Parameters for MKS's rlog command.
 *
 * @see org.parabuild.ci.versioncontrol.mks.MKSCommandParameters
 * @see org.parabuild.ci.versioncontrol.mks.MKSCreatesandboxCommand
 */
public class MKSCreatesandboxCommandParameters extends MKSCommandParameters {

  private int lineTerminator = SourceControlSetting.MKS_LINE_TERMINATOR_NATIVE;
  private String projectRevision = null;

  public int getLineTerminator() {
    return lineTerminator;
  }


  public String getFormattedLineTerminator() {
    if (lineTerminator == SourceControlSetting.MKS_LINE_TERMINATOR_NATIVE) {
      return "native";
    } else if (lineTerminator == SourceControlSetting.MKS_LINE_TERMINATOR_CRLF) {
      return "crlf";
    } else if (lineTerminator == SourceControlSetting.MKS_LINE_TERMINATOR_LF) {
      return "lf";
    } else {
      throw new IllegalStateException("Unknown line terminator code: " + lineTerminator);
    }
  }


  public void setLineTerminator(final int lineTerminator) {
    this.lineTerminator = lineTerminator;
  }


  public String getProjectRevision() {
    return projectRevision;
  }


  public void setProjectRevision(final String projectRevision) {
    this.projectRevision = projectRevision;
  }
}
