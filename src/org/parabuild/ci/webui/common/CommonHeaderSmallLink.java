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
package org.parabuild.ci.webui.common;

import viewtier.ui.Color;
import viewtier.ui.Font;

/**
 */
public final class CommonHeaderSmallLink extends CommonLink {

  private static final long serialVersionUID = -5734728391348277181L;


  public CommonHeaderSmallLink(final String s, final String s1) {
    super(s, s1);
    setForeground(Color.White);
    setFont(new Font(Font.SansSerif, Font.Bold, 10));
  }
}

