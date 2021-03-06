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
package org.parabuild.ci.object;

import java.io.*;

/**
 * User group memebership
 *
 * @hibernate.class table="BUILD_ACCESS" dynamic-update="true"
 * @hibernate.cache usage="read-write"
 */
public final class GroupBuildAccess implements Serializable, ObjectConstants {

  private static final long serialVersionUID = -5237501290837750448L; // NOPMD

  private int ID = UNSAVED_ID;
  private int buildID = BuildConfig.UNSAVED_ID;
  private int groupID = Group.UNSAVED_ID;
  private long timeStamp = 0;


  public GroupBuildAccess() {
  }


  public GroupBuildAccess(final int groupID, final int buildID) {
    this.buildID = buildID;
    this.groupID = groupID;
  }


  /**
   * The getter method for member user ID
   *
   * @return int
   *
   * @hibernate.id generator-class="identity" column="ID"
   * unsaved-value="-1"
   */
  public int getID() {
    return ID;
  }


  public void setID(final int ID) {
    this.ID = ID;
  }


  /**
   * The getter method for member user ID
   *
   * @return int
   *
   * @hibernate.property column = "BUILD_ID" unique="false"
   * null="false"
   */
  public int getBuildID() {
    return buildID;
  }


  public void setBuildID(final int buildID) {
    this.buildID = buildID;
  }


  /**
   * The getter method for a group ID which user is member of
   *
   * @return int
   *
   * @hibernate.property column = "GROUP_ID" unique="false"
   * null="false"
   */
  public int getGroupID() {
    return groupID;
  }


  public void setGroupID(final int groupID) {
    this.groupID = groupID;
  }


  /**
   * Returns timestamp
   *
   * @return long
   *
   * @hibernate.version column="TIMESTAMP"  null="false"
   */
  public long getTimeStamp() {
    return timeStamp;
  }


  public void setTimeStamp(final long timeStamp) {
    this.timeStamp = timeStamp;
  }


  public String toString() {
    return "GroupBuildAccess{" +
      "ID=" + ID +
      ", buildID=" + buildID +
      ", groupID=" + groupID +
      ", timeStamp=" + timeStamp +
      '}';
  }
}
