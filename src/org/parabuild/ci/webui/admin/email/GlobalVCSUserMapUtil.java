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
package org.parabuild.ci.webui.admin.email;

import org.parabuild.ci.object.GlobalVCSUserMap;
import org.parabuild.ci.webui.common.Pages;
import org.parabuild.ci.webui.common.ParameterUtils;
import org.parabuild.ci.configuration.GlobalVCSUserMapManager;
import viewtier.ui.Parameters;


/**
 * GlobalVersionControlUserMapPageUtil
 * <p/>
 *
 * @author Slava Imeshev
 * @since Dec 27, 2008 4:37:40 PM
 */
final class GlobalVCSUserMapUtil {

  /**
   * Utility class constructor.
   */
  private GlobalVCSUserMapUtil() {
  }


  public static GlobalVCSUserMap getMappingFromParameters(final Parameters params) {
    final Integer id = ParameterUtils.getIntegerParameter(params, Pages.PARAM_VCS_MAPPING_ID, null);
    if (id == null) {
      return null;
    }
    return GlobalVCSUserMapManager.getInstance().getMapping(id);
  }
}
