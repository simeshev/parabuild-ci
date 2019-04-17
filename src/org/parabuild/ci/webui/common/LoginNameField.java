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

public final class LoginNameField extends CommonField {

  public static final int COLUMN_WIDTH = 175;
  private static final long serialVersionUID = 1909824177317330446L;


  public LoginNameField() {
    this(25);
  }


  public LoginNameField(final int visibleSize) {
    super(60, visibleSize);
  }

  public LoginNameField(final String fieldName, final int visibleSize) {
    super(fieldName, 60, visibleSize);
  }
}
