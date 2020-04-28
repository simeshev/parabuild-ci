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
package org.parabuild.ci.webui.admin.builder;

import org.parabuild.ci.webui.CommonCommandLinkWithImage;
import org.parabuild.ci.webui.admin.system.NavigatableSystemConfigurationPage;
import org.parabuild.ci.webui.common.BreakLabel;
import org.parabuild.ci.webui.common.CommonLabel;
import org.parabuild.ci.webui.common.GridIterator;
import org.parabuild.ci.webui.common.MessagePanel;
import org.parabuild.ci.webui.common.Pages;
import org.parabuild.ci.webui.common.WebuiUtils;
import viewtier.ui.Parameters;
import viewtier.ui.StatelessTierlet;

/**
 * This page shows list List of clusters.
 */
public final class BuilderListPage extends NavigatableSystemConfigurationPage implements StatelessTierlet {

  private static final long serialVersionUID = -2472052514871569348L;  // NOPMD
  private static final String CAPTION_BUILDER_LIST = "Build Farm List";
  private static final String CAPTION_ADD_NEW_BUILDER = "Add Build Farm";


  public BuilderListPage() {
    setTitle(makeTitle(CAPTION_BUILDER_LIST));
  }


  protected Result executeSystemConfigurationPage(final Parameters params) {
    final GridIterator gi = new GridIterator(getRightPanel(), 1);

    final MessagePanel pnlHeader = new MessagePanel(CAPTION_BUILDER_LIST);
    pnlHeader.setWidth("100%");
    pnlHeader.getUserPanel().add(new BreakLabel());
    pnlHeader.getUserPanel().add(new CommonLabel("A Build farm is a cluster of one or more build agents that a build configuration uses to run builds. If there are multiple agents attached to the build farm, it will balance build load evenly across the agents and fail over to another agent if a current agent goes out of commission."));
    pnlHeader.getUserPanel().add(new BreakLabel());

    // Add builder list table
    gi.add(pnlHeader);
    gi.add(WebuiUtils.makePanelDivider());
    gi.add(new BuilderListTable(super.isValidAdminUser()));

    // add new cluster link - bottom
    gi.add(WebuiUtils.makeHorizontalDivider(5));
    gi.add(new CommonCommandLinkWithImage(CAPTION_ADD_NEW_BUILDER, Pages.ADMIN_EDIT_BUILDER));
    return Result.Done();
  }
}
