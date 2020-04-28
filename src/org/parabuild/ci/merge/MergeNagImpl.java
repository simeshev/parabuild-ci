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
package org.parabuild.ci.merge;

import java.util.List;

/**
 * A reminder to an owner of a pending merge.
 */
final class MergeNagImpl implements MergeNag {

  private final String userName;
  private final List pendingChangeLists;


  /**
   * Constructor.
   *
   * @param name
   * @param pendingChangeLists
   */
  MergeNagImpl(final String name, final List pendingChangeLists) {
    this.userName = name;
    this.pendingChangeLists = pendingChangeLists;
  }


  /**
   * @return version control user name.
   */
  public String getUserName() {
    return userName;
  }


  /**
   * Pending change lists.
   *
   * @return pending change lists.
   */
  public List getPendingChangeLists() {
    return pendingChangeLists;
  }


  public String toString() {
    return "MergeNagImpl{" +
      "userName='" + userName + '\'' +
      ", pendingChangeLists=" + pendingChangeLists +
      '}';
  }
}
