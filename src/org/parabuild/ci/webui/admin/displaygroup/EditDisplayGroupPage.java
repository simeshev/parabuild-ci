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
package org.parabuild.ci.webui.admin.displaygroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parabuild.ci.object.DisplayGroup;
import org.parabuild.ci.webui.common.BasePage;
import org.parabuild.ci.webui.common.ButtonSeparator;
import org.parabuild.ci.webui.common.CancelButton;
import org.parabuild.ci.webui.common.Pages;
import org.parabuild.ci.webui.common.ParameterUtils;
import org.parabuild.ci.webui.common.SaveButton;
import org.parabuild.ci.webui.common.WebuiUtils;
import viewtier.ui.ButtonPressedEvent;
import viewtier.ui.ButtonPressedListener;
import viewtier.ui.ConversationalTierlet;
import viewtier.ui.Flow;
import viewtier.ui.Layout;
import viewtier.ui.Parameters;

/**
 * This page is responsible for creating/editting display group
 *
 * @noinspection FieldCanBeLocal
 */
public final class EditDisplayGroupPage extends BasePage implements ConversationalTierlet {

  private static final long serialVersionUID = 124348935234219001L; // NOPMD
  /**
   * @noinspection UNUSED_SYMBOL,UnusedDeclaration
   */
  private static final Log log = LogFactory.getLog(EditDisplayGroupPage.class); // NOPMD

  public static final String PAGE_TITLE_DEFAULT = "Manage Display Group";
  public static final String PAGE_TITLE_ADD_GROUP = "Add New Display Group";
  public static final String ERROR_GROUP_NOT_FOUND = "Requested display group not found";

  private final EditDisplayGroupPanel pnlGroup = new EditDisplayGroupPanel(); // NOPMD
  private final SaveButton btnSave = new SaveButton(); // NOPMD
  private final CancelButton btnCancel = new CancelButton(); // NOPMD
  private final Flow flwSaveCancel = new Flow().add(btnSave).add(new ButtonSeparator()).add(btnCancel);


  /**
   * Constructor.
   */
  public EditDisplayGroupPage() {
    // layout
    setTitle(makeTitle(PAGE_TITLE_DEFAULT)); // default title
    flwSaveCancel.setAlignX(Layout.CENTER);
    flwSaveCancel.setBackground(Pages.COLOR_PANEL_HEADER_BG);
    baseContentPanel().getUserPanel().add(pnlGroup);
    baseContentPanel().getUserPanel().add(WebuiUtils.makePanelDivider());
    baseContentPanel().getUserPanel().add(flwSaveCancel);

    // add cancel button listener
    btnCancel.addListener(new ButtonPressedListener() {
      private static final long serialVersionUID = -3218334269375571954L;


      public Result buttonPressed(final ButtonPressedEvent event) {
        return Result.Done(Pages.ADMIN_DISPLAY_GROUPS);
      }
    });

    // add save button listener
    btnSave.addListener(new ButtonPressedListener() {
      private static final long serialVersionUID = 1802379603602999748L;


      public Result buttonPressed(final ButtonPressedEvent event) {
        if (pnlGroup.save()) {
          return Result.Done(Pages.ADMIN_DISPLAY_GROUPS);
        } else {
          return Result.Continue();
        }
      }
    });
  }


  /**
   * Strategy method derived from BasePage.
   *
   * @param parameters
   */
  public Result executePage(final Parameters parameters) {
    // authenticate
    if (!super.isValidUser()) {
      return WebuiUtils.storeReturnPathAndForward(getTierletContext(),
              Pages.PUBLIC_LOGIN, Pages.ADMIN_EDIT_DISPLAY_GROUP, parameters);
    }

    if (!super.isValidAdminUser()) {
      return WebuiUtils.showNotAuthorized(this);
    }

    if (parameters.isParameterPresent(Pages.PARAM_DISPLAY_GROUP_ID)) {
      // group ID is provided
      final DisplayGroup groupFromParameters = ParameterUtils.getDisplayGroupFromParameters(parameters);
      if (groupFromParameters == null) {
        // show error and exit
        baseContentPanel().getUserPanel().clear();
        baseContentPanel().showErrorMessage(ERROR_GROUP_NOT_FOUND);
        return Result.Done();
      } else {
        // group found, load data
        setTitle(makeTitle("Edit display group \"" + groupFromParameters.getName() + '\"'));
        pnlGroup.setTitle("Edit Display Group");
        pnlGroup.load(groupFromParameters);
        return Result.Continue();
      }
    } else {

      setFocusOnFirstInput(true);
      setTitle(makeTitle(PAGE_TITLE_ADD_GROUP));
      pnlGroup.setTitle("New Display Group");
      return Result.Continue();
    }
  }


  public String toString() {
    return "EditDisplayGroupPage{" +
            "pnlGroup=" + pnlGroup +
            ", btnSave=" + btnSave +
            ", btnCancel=" + btnCancel +
            "} " + super.toString();
  }
}
