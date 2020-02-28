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
package org.parabuild.ci.versioncontrol;

import java.util.*;
import org.apache.commons.logging.*;

import org.parabuild.ci.common.ValidationException;

/**
 * This class is responsible for validating and cleaning
 * up PVCS project path.
 */
final class PVCSProjectListParser {

  /** @noinspection UNUSED_SYMBOL,UnusedDeclaration*/
  private static final Log log = LogFactory.getLog(PVCSProjectListParser.class); // NOPMD

  private static final String PREFIX = "/";


  /**
   * Validates PVCS project path.
   *
   * @param originalProject
   *
   * @throws ValidationException
   */
  public void validate(final String originalProject) throws ValidationException {
    parseAndValidate(originalProject);
  }


  /**
   * Parses PVCS project path.
   *
   * @param originalProject
   *
   * @return List of {@link RepositoryPath} objects.
   *
   * @throws ValidationException
   */
  public List parseProjects(final String originalProject) throws ValidationException {
    return parseAndValidate(originalProject);
  }


  private static List parseAndValidate(final String originalProject) throws ValidationException {
    final DepotPathParser depotPathParser = new DepotPathParser("Project");
    final List list = depotPathParser.parseDepotPath(originalProject);
    for (int i = 0, n = list.size(); i < n; i++) {
      final RepositoryPath repositoryPath = (RepositoryPath)list.get(i);
      repositoryPath.setPath('/' + repositoryPath.getPath());
//      if (log.isDebugEnabled()) log.debug("repositoryPath: " + repositoryPath.getPath());
      if (!repositoryPath.getPath().startsWith(PREFIX)) {
        throw new ValidationException("Project should start with \"" + PREFIX + '\"');
      }
    }
    return list;
  }
}
