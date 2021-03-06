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

import org.apache.commons.logging.*;

import org.parabuild.ci.common.*;
import org.parabuild.ci.notification.*;
import org.parabuild.ci.webui.*;
import org.parabuild.ci.webui.common.*;
import viewtier.ui.*;

/**
 * This page is for resetting password.
 */
public final class ResetPasswordPage extends BasePage implements ConversationalTierlet {

  private static final long serialVersionUID = -653967251566354512L; // NOPMD
  /** @noinspection UNUSED_SYMBOL,UnusedDeclaration*/
  private static final Log log = LogFactory.getLog(ResetPasswordPage.class); // NOPMD

  private final ResetPasswordPanel resetPanel = new ResetPasswordPanel();


  /**
   * Reqired default constructor
   */
  public ResetPasswordPage() {
    // appearance
    setTitle(makeTitle("Request for password reset"));
    resetPanel.setWidth(Pages.PAGE_WIDTH);

    // layout
    final Panel cp = baseContentPanel().getUserPanel();
    cp.add(resetPanel);

    // set submit button listener
    resetPanel.setSubmitButtonListener(new ButtonPressedListener() {
      public Result buttonPressed(final ButtonPressedEvent event) {
        try {
          // validate
          if (!resetPanel.validate()) return Result.Continue();
          // reset
          final UserPasswordResetter passwordResetter = new UserPasswordResetter();
          passwordResetter.resetPassword(resetPanel.getName(), resetPanel.getEmail());
          // cleanup the page and tell that message was sent
          final Flow doneMsg = new Flow();
          doneMsg.add(new CommonLabel("Your password has been re-set and the new password was e-mailed. "));
          doneMsg.add(WebuiUtils.clickHereToContinue(Pages.PUBLIC_LOGIN));
          doneMsg.setAlignY(Layout.TOP);
          baseContentPanel().getUserPanel().clear();
          baseContentPanel().add(WebuiUtils.makePanelDivider());
          baseContentPanel().add(doneMsg);
          return Result.Done();
        } catch (FatalConfigurationException e) {
          baseContentPanel().clear();
          baseContentPanel().showErrorMessage("There was an error resetting password: " + StringUtils.toString(e));
          return Result.Done();
        }
      }
    });
  }


  /**
   * Tierlet lifecycle method
   *
   * @param parameters
   *
   * @return
   */
  public Result executePage(final Parameters parameters) {
    resetPanel.clearMessage();
    return Result.Continue();
  }
}
