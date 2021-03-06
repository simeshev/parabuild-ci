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

import java.util.*;

import org.parabuild.ci.object.*;
import org.parabuild.ci.webui.common.*;

/**
 * This abstract class defines a common interface and a strategy
 * for presenting alterable parameter for manual chedule that are
 * entered at build start time.
 * <p/>
 * Such parameters typically include alterable version control
 * parameters. The resulting parameters are used to alter
 * attributes of an actual build configuration that is about to
 * be used to start the build.
 * <p/>
 * If there are no parameters a factory code creating such a
 * panel should set visibility of the panel to false.
 *
 * @see SourceControlSettingVO
 * @see ManualStartParametersPanel
 */
public abstract class ManualScheduleStartParametersPanel extends MessagePanel {

  protected final PropertyToInputMap sourceControlSettingsPropertyToInputMap = new PropertyToInputMap(true, makePropertyHandler()); // strict map
  protected GridIterator gridIterator = null;


  /**
   * Creates message panel with title displayed.
   *
   * @param title panel title.
   */
  protected ManualScheduleStartParametersPanel(final String title) {
    super(title);
    super.setWidth(Pages.PAGE_WIDTH);
    super.showHeaderDivider(true);
    this.gridIterator = new GridIterator(super.getUserPanel(), 2);
  }


  /**
   * Factory method to create SourceControlSetting handler to be
   * used by propertyToInputMap
   *
   * @return implementation of PropertyToInputMap.PropertyHandler
   * @see PropertyToInputMap.PropertyHandler
   */
  private static PropertyToInputMap.PropertyHandler makePropertyHandler() {
    return new PropertyToInputMap.PropertyHandler() {
      public Object makeProperty(final String propertyName) {
        final SourceControlSettingVO setting = new SourceControlSettingVO();
        setting.setName(propertyName);
        return setting;
      }


      public void setPropertyValue(final Object parameter, final String value) {
        ((SourceControlSettingVO) parameter).setValue(value);
      }


      public String getPropertyValue(final Object setting) {
        return ((SourceControlSettingVO) setting).getValue();
      }


      public String getPropertyName(final Object setting) {
        return ((SourceControlSettingVO) setting).getName();
      }
    };
  }


  /**
   * Return modified properties
   *
   * @return returns a list of updated properties <code>SourceControlSettingVO</code>.
   */
  public List getUpdatedSettings() {
    return sourceControlSettingsPropertyToInputMap.getUpdatedProperties();
  }


  /**
   * Sets edit mode.
   *
   * @param mode edit mode to set.
   */
  public abstract void setEditMode(int mode);


  /**
   * Loads the panel content according to the build ID provided in
   * the constructor.
   *
   * @param buildID build ID
   */
  public abstract void load(final int buildID);
}
