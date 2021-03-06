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

import java.sql.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.parabuild.ci.common.IoUtils;

/**
 * Upgrades to version 47.
 */
final class UpgraderToVersion47 implements SingleStepSchemaUpgrader {

  private static final Log log = LogFactory.getLog(UpgraderToVersion47.class);


  /**
   * Perform upgrade.
   */
  public void upgrade(final Connection conn, final int upgradeToVersion) throws SQLException {
    final boolean savedAutoCommit = conn.getAutoCommit();
    Statement st = null; // NOPMD
    try {
      // create statement
      conn.setAutoCommit(true);
      st = conn.createStatement();

      log.debug("Altering table");
      PersistanceUtils.executeDDLs(st, new String[]{
        //
        " alter table MERGE add column BRANCH_VIEW_SOURCE tinyint default 0 not null ",
        " update MERGE set BRANCH_VIEW_SOURCE=0 ",
        " alter table MERGE alter column BRANCH_VIEW_SOURCE drop default ",
        //
        " alter table MERGE add column BRANCH_VIEW varchar(32000) default '' not null ",
        " update MERGE set BRANCH_VIEW='' ",
        " alter table MERGE alter column BRANCH_VIEW drop default ",
        //
        " alter table MERGE add column BRANCH_VIEW_NAME varchar(512) default '' not null ",
        " update MERGE set BRANCH_VIEW_NAME='' ",
        " alter table MERGE alter column BRANCH_VIEW_NAME drop default ",
      });

      log.debug("Updating version");
      st.executeUpdate("update SYSTEM_PROPERTY set VALUE = '" + upgraderVersion() + "' where NAME = 'parabuild.schema.version' ");

      // finish
      conn.commit();

      // request post-startup config manager action
      System.setProperty(SystemConstants.SYSTEM_PROPERTY_INIT_ADVANCED_SETTINGS, "true");
    } finally {
      IoUtils.closeHard(st);
      conn.setAutoCommit(savedAutoCommit);
    }
  }


  public int upgraderVersion() {
    return 47;
  }
}
