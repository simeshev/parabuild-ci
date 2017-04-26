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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parabuild.ci.common.IoUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Upgrades to version 76.
 *
 * @noinspection JDBCResourceOpenedButNotSafelyClosed,JDBCExecuteWithNonConstantString
 */
final class UpgraderToVersion76 implements SingleStepSchemaUpgrader {

  private static final Log LOG = LogFactory.getLog(UpgraderToVersion76.class); // NOPMD


  /**
   * Perform upgrade.
   */
  public void upgrade(final Connection connection, final int upgradeToVersion) throws SQLException {
    final boolean savedAutoCommit = connection.getAutoCommit();
    final Statement st = connection.createStatement();
    try {
      // create statement
      connection.setAutoCommit(true);

      LOG.debug("Altering table");
      PersistanceUtils.executeDDLs(st, new String[]{

              " alter table USERS add column DISABLE_ALL_EMAIL char(1) default 'N' not null ",
              " update USERS set DISABLE_ALL_EMAIL='N' ",
              " alter table USERS alter column DISABLE_ALL_EMAIL drop default ",
      });

      LOG.debug("Updating version");
      st.executeUpdate("update SYSTEM_PROPERTY set VALUE = '" + upgraderVersion() + "' where NAME = 'parabuild.schema.version' ");

      // finish
      connection.commit();

    } finally {
      IoUtils.closeHard(st);
      connection.setAutoCommit(savedAutoCommit);
    }
  }


  public int upgraderVersion() {
    return 76;
  }
}