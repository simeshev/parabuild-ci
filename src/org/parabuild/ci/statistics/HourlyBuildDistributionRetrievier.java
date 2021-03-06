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
package org.parabuild.ci.statistics;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import java.util.List;

/**
 */
public final class HourlyBuildDistributionRetrievier extends AbstractBuildDistributionRetrievier {

  public static final int DISTRIBUTION_SIZE = 24;


  public HourlyBuildDistributionRetrievier(final int activeBuildID) {
    super(activeBuildID);
  }


  protected List getDistributionFromDB(final Session session) throws HibernateException {
    final Query q = session.createQuery("select hd from HourlyDistribution hd where hd.activeBuildID = ?");
    q.setInteger(0, getActiveBuildID());
    q.setCacheable(true);
    return q.list();
  }


  /**
   * @return distribution size.
   */
  protected int distributionSize() {
    return DISTRIBUTION_SIZE;
  }


  /**
   * Hourly distribution starts with 0 hour.
   *
   * @return 0
   */
  protected int distributionStart() {
    return 0;
  }
}
