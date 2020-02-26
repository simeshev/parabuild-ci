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

import org.parabuild.ci.common.VCSAttribute;
import org.parabuild.ci.webui.common.*;

/**
 * Perforce clobber option dropdown.
 */
final class P4ClobberDropDown extends CodeNameDropDown {


  private static final long serialVersionUID = 6574778055454965936L;


  P4ClobberDropDown() {
    addCodeNamePair(VCSAttribute.P4_OPTION_VALUE_NOCLOBBER, "noclobber");
    addCodeNamePair(VCSAttribute.P4_OPTION_VALUE_CLOBBER, "clobber");
    setSelection(0);
  }
}
