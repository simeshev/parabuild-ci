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
package org.parabuild.ci.search;

import java.io.*;
import java.util.*;
import org.apache.commons.logging.*;
import org.apache.lucene.document.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.*;

import org.parabuild.ci.archive.*;
import org.parabuild.ci.common.*;
import org.parabuild.ci.configuration.*;
import org.parabuild.ci.error.*;
import org.parabuild.ci.error.Error;
import org.parabuild.ci.object.*;
import org.parabuild.ci.services.*;

/**
 * Provides access to search functionality.
 */
public final class SearchManager {

  /** @noinspection UNUSED_SYMBOL,UnusedDeclaration*/
  private static final Log log = LogFactory.getLog(SearchManager.class); // NOPMD

  /**
   * Singleton instance.
   */
  private static final SearchManager instance = new SearchManager();

  /**
   * Reference to search service.
   */
  private SearchService searchService = null;
  private ConfigurationManager cm = null;


  /**
   * Singleton constructor.
   */
  private SearchManager() {
    this.searchService = ServiceManager.getInstance().getSearchService();
    this.cm = ConfigurationManager.getInstance();
  }


  /**
   * @return Singleton instance.
   */
  public static SearchManager getInstance() {
    return instance;
  }


  /**
   * Search given query String.
   */
  public Hits search(final SearchRequest request) throws IOException, ParseException {
    // REVIEWME: simeshev@parabuilci.org -> implement in full.

//    if (log.isDebugEnabled()) log.debug("request: " + request.toString());
    final StringBuffer searchQuery = new StringBuffer(request.getSearchQuery());

    // if buildID available, limit query to the given build.
    final String stringBuildID = request.getParameter(SearchRequestParameter.BUILD_ID);
    if (StringUtils.isValidInteger(stringBuildID)
      && Integer.parseInt(stringBuildID) != BuildConfig.UNSAVED_ID) {
      searchQuery.append(" +").append(LuceneDocumentFactory.FIELD_BUILD_ID).
        append(':').append(stringBuildID);
    }

//    if (log.isDebugEnabled()) log.debug("searchQuery: " + searchQuery);
    return searchService.search(searchQuery.toString(), LuceneDocumentFactory.FIELD_CONTENT);
  }


  /**
   * Indexes part of multi-file sequence log (directory).
   */
  public void index(final StepLog stepLog, final File actualLogContent, final String fileNameInArchiveDir) {
    if (actualLogContent == null || !actualLogContent.exists()) return;
    try {
      // REVIEWME: simeshev@parabuilci.org -> html docs parsing?
      //
      // make document
      final StepRun stepRun = cm.getStepRun(stepLog.getStepRunID());
      final BuildRun buildRun = cm.getBuildRun(stepRun.getBuildRunID());
      // REVIEWME: simeshev@parabuilci.org -> lazy document making - move creating a doc to
      // index request.
      final Document document = LuceneDocumentFactory.makeDocument(buildRun, stepRun, stepLog, actualLogContent, fileNameInArchiveDir);

      // send index request to indexer queue
      searchService.queueIndexRequest(new DocumentIndexRequest(document));
    } catch (Exception e) {
      reportIndexRequestError(e);
    }
  }


  /**
   * Indexes single-file sequence log.
   */
  public void index(final StepLog stepLog, final File actualLogContent) {
    if (actualLogContent == null || !actualLogContent.exists()) return;
    try {
      // make document
      final StepRun stepRun = cm.getStepRun(stepLog.getStepRunID());
      final BuildRun buildRun = cm.getBuildRun(stepRun.getBuildRunID());
      final Document document = LuceneDocumentFactory.makeDocument(buildRun, stepRun, stepLog, actualLogContent);

      // send index request to indexer queue
      searchService.queueIndexRequest(new DocumentIndexRequest(document));
    } catch (Exception e) {
      reportIndexRequestError(e);
    }
  }


  /**
   * Indexes changes in the given build run.
   *
   * @param buildRun
   */
  public void indexChanges(final BuildRun buildRun) {
    try {
      for (final Iterator i = cm.getBuildRunParticipants(buildRun).iterator(); i.hasNext();) {
        final ChangeList changeList = (ChangeList)i.next();
        final Document document = LuceneDocumentFactory.makeDocument(buildRun, changeList, cm.getChanges(changeList));
        // send index request to indexer queue
        searchService.queueIndexRequest(new DocumentIndexRequest(document));
      }
    } catch (Exception e) {
      reportIndexRequestError(e);
    }
  }


  /**
   * Indexes step result. For step result only step result file
   * names are indexed.
   */
  public void index(final StepResult stepResult) {
    try {
      // preExecute
      final StepRun stepRun = cm.getStepRun(stepResult.getStepRunID());
      final BuildRun buildRun = cm.getBuildRun(stepRun.getBuildRunID());
      final ArchiveManager am = ArchiveManagerFactory.getArchiveManager(buildRun.getActiveBuildID());
      // go through the list of entries
      final List entries = am.getArchivedResultEntries(stepResult);
      for (final Iterator i = entries.iterator(); i.hasNext();) {
        // make document
        final String fileName = ((ArchiveEntry)i.next()).getEntryName();
        final Document document = LuceneDocumentFactory.makeDocument(buildRun, stepResult, fileName);
        // queue
        searchService.queueIndexRequest(new DocumentIndexRequest(document));
      }
    } catch (Exception e) {
      reportIndexRequestError(e);
    }
  }


  /**
   * Helper method to report index request errors to
   * administrator.
   *
   * @param e - exception
   */
  private void reportIndexRequestError(final Exception e) {
    final Error error = new Error("Log indexing error: " + StringUtils.toString(e));
    error.setSendEmail(true);
    error.setDetails(e);
    error.setSubsystemName(Error.ERROR_SUSBSYSTEM_SEARCH);
    ErrorManagerFactory.getErrorManager().reportSystemError(error);
  }


  /**
   * @see SearchService.IndexRequest
   */
  private static final class DocumentIndexRequest implements SearchService.IndexRequest {

    private Document document = null;


    /**
     * Constructor.
     *
     * @param document
     */
    public DocumentIndexRequest(final Document document) {
      this.document = document;
    }


    public Document getDocumentToIndex() {
      return document;
    }
  }


  public String toString() {
    return "SearchManager{" +
            "searchService=" + searchService +
            ", cm=" + cm +
            '}';
  }
}
