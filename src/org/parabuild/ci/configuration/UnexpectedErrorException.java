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
package org.parabuild.ci.configuration;

import org.parabuild.ci.util.StringUtils;

public final class UnexpectedErrorException extends RuntimeException {

  private static final long serialVersionUID = -631404622399738707L;


  public UnexpectedErrorException(final Exception e) {
    super(StringUtils.toString(e), e);
  }


  public UnexpectedErrorException(final String message) {
    super(message);
  }
}
