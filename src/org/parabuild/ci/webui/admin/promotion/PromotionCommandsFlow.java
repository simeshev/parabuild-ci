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
package org.parabuild.ci.webui.admin.promotion;

import java.util.Properties;

import org.parabuild.ci.webui.common.CommonLink;
import org.parabuild.ci.webui.common.Pages;
import viewtier.ui.Flow;
import viewtier.ui.Label;
import viewtier.ui.Link;

/**
 * List of commands available for a parcticular promotion
 * policy in a policy table.
 */
final class PromotionCommandsFlow extends Flow {

  private static final String CAPTION_EDIT = "Edit";
  private static final String CAPTION_DELETE = "Delete";

  private Link lnkEdit = null;
  private Link lnkDelete = null;
  private static final long serialVersionUID = 1110459605561214200L;


  /**
   * Constructor.
   *
   * @param policyID ID to use to compose command links.
   */
  PromotionCommandsFlow(final int policyID) {
    this();
    setPolicyID(policyID);
  }


  /**
   * Constructor.
   */
  PromotionCommandsFlow() {
    lnkEdit = new CommonLink(CAPTION_EDIT, Pages.PAGE_EDIT_PROMOTION_POLICY);
    lnkDelete = new CommonLink(CAPTION_DELETE, Pages.PAGE_DELETE_PROMOTION_POLICY);
    this.add(lnkEdit);
    this.add(new Label(" | "));
    this.add(lnkDelete);
  }


  /**
   * Sets policy ID
   *
   * @param policyID to set
   */
  public void setPolicyID(final int policyID) {
    final Properties param = PromotionUtils.createProperties(policyID);
    lnkDelete.setParameters(param);
    lnkEdit.setParameters(param);
  }


  public String toString() {
    return "PromotionCommandsFlow{" +
            "lnkEdit=" + lnkEdit +
            ", lnkDelete=" + lnkDelete +
            '}';
  }
}
