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

import java.util.*;

import net.sf.hibernate.*;
import org.parabuild.ci.object.*;

/**
 * Updates per-month statistics.
 */
final class MonthlyTestStatsUpdater extends AbstractTestStatsUpdater {

  protected MonthlyTestStatsUpdater(final byte testCode) {
    super(testCode);
  }


  /**
   * @return truncation lavel to be used by the updater.
   *
   * @see Calendar.DAY_OF_MONTH
   * @see Calendar.MONTH
   * @see Calendar.HOUR_OF_DAY
   * @see Calendar.YEAR
   */
  public int truncateLevel() {
    return Calendar.MONTH;
  }


  /**
   * @param session Hibenrate session
   * @param activeBuildID to get stats for
   * @param sampleDate Date already truncated
   *
   * @return persistant stats corrsponding the given buidl run or
   *         null if doesn't exist.
   *
   * @throws HibernateException
   */
  protected StatisticsSample findPersistedStats(final Session session, final int activeBuildID, final Date sampleDate) throws HibernateException {
    final Query query = session.createQuery("select mts from MonthlyTestStats mts where mts.activeBuildID = ? and mts.testCode = ? and mts.sampleTime = ?");
    query.setInteger(0, activeBuildID);
    query.setByte(1, getTestCode());
    query.setTimestamp(2, sampleDate);
    query.setCacheable(true);
    return (MonthlyTestStats)query.uniqueResult();
  }


  /**
   * Factory method to make an objct that implements
   * PersistentStats.
   *
   * @return new instance of PersistentStats.
   */
  protected StatisticsSample makePersistentStats() {
    final MonthlyTestStats monthlyTestStats = new MonthlyTestStats();
    monthlyTestStats.setTestCode(getTestCode());
    return monthlyTestStats;
  }
}
