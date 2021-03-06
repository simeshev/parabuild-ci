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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parabuild.ci.build.AgentFailureException;
import org.parabuild.ci.common.IoUtils;
import org.parabuild.ci.common.StringUtils;
import org.parabuild.ci.remote.Agent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * CVSCommand is a helper class to execute CVS commands and to
 * return resulting files  for stdout and stderr.
 * <p/>
 * To enable logging of CVS commands, system property
 * parabuild.cvscmdd.enabled should be set to true.
 */
final class CVSCommand extends VersionControlRemoteCommand {

  private static final Log log = LogFactory.getLog(CVSCommand.class);
  private static final String CAPTION_SYSTEM_ERROR = "System error: ";

  private String remotePasswFileName = null;
  private String cvsRoot = null;
  private String cvsPassword = null;
  private String cvsRshPath = null;


  /**
   * Constructor, accepts StringBuffer as a command
   *
   * @param agent
   * @param commandSb
   * @throws IOException if agent checkout dir does not exist.
   */
  CVSCommand(final Agent agent, final StringBuffer commandSb) throws IOException, AgentFailureException {
    super(agent, true);
    setCommand(commandSb.toString());
    setCurrentDirectory(agent.getCheckoutDirName());
    // PARABUILD_CHECKOUT_DIR is used as a signature - Build runner will report
    // it as one of the process signatures.
    super.addEnvironment("PARABUILD_CHECKOUT_DIR", agent.getCheckoutDirName());
    super.signatureRegistry.register(agent.getCheckoutDirName());
  }


  /**
   * Callback method - this method is called right after call to
   * execute.
   * <p/>
   * This method can be overriden by children to accomodate
   * post-execute processing such as command log analisys e.t.c.
   *
   * @param resultCode - execute command result code.
   */
  protected void postExecute(final int resultCode) throws IOException {
    //if (log.isDebugEnabled()) log.debug("IoUtils.fileToString(getStderrFile()) = " + IoUtils.fileToString(getStderrFile()));
    //if (log.isDebugEnabled()) log.debug("IoUtils.fileToString(getStdoutFile()) = " + IoUtils.fileToString(getStdoutFile()));
    BufferedReader reader = null;
    try {
      if (getStderrFile().exists() && getStderrFile().length() > 0L) {
        reader = new BufferedReader(new FileReader(getStderrFile()));
        String line = reader.readLine();
        while (line != null) {
//          if (log.isDebugEnabled()) log.debug("line: " + line);
          if (line.indexOf("no such repository") >= 0) {
            throw new IOException("CVS root \"" + cvsRoot + "\" is invalid");
          }
          if (line.indexOf("cvs server: cannot find module") >= 0) {
            throw new IOException(line);
          }
          if (line.indexOf("authorization failed:") >= 0) {
            throw new IOException("Authorization failed for user from CVS root \"" + cvsRoot + '\"');
          }
          if (line.indexOf("may only specify a positive, non-zero, integer port") >= 0) {
            throw new IOException("Invalid CVS root \"" + cvsRoot + '\"');
          }
          if (line.indexOf("Connection refused") >= 0) {
            throw new IOException("Can not connect to the server for CVS root \"" + cvsRoot + "\" - connection refused");
          }
          if (line.indexOf("Unknown host") >= 0) {
            throw new IOException("Can not connect to the server for CVS root \"" + cvsRoot + "\" - unknown host");
          }
          if (line.indexOf("Unknown command:") >= 0) {
            throw new IOException(CAPTION_SYSTEM_ERROR + line);
          }
          if (line.indexOf("[update aborted]") >= 0) {
            throw new IOException(CAPTION_SYSTEM_ERROR + line);
          }
          if (line.indexOf("[log aborted]") >= 0) {
            throw new IOException(CAPTION_SYSTEM_ERROR + line);
          }
          // see # 765 - suppressed "consult above messages if any" following "end of file from server" as there is never "above messages"
          if (line.indexOf("[checkout aborted]: end of file from server") >= 0) {
            throw new IOException(CAPTION_SYSTEM_ERROR + "cvs [checkout aborted]: end of file from server");
          }
          //
          if (line.indexOf("[checkout aborted]") >= 0) {
            throw new IOException(CAPTION_SYSTEM_ERROR + line);
          }
          if (line.indexOf("[server aborted]: no such tag") >= 0) {
            throw new IOException("Cannot find branch \"" + cvsRoot + '\"');
          }
          if (line.indexOf("[history aborted]") >= 0) {
            throw new IOException(CAPTION_SYSTEM_ERROR + line);
          }
          if (line.indexOf("[checkout aborted]: the :pserver: access method is not available on this system") >= 0) {
            throw new IOException(CAPTION_SYSTEM_ERROR + line);
          }
          if (line.indexOf("[rtag aborted]") >= 0) {
            throw new IOException(CAPTION_SYSTEM_ERROR + line);
          }
          line = reader.readLine();
        }
      }
    } catch (IOException e) {
      throw new IOException("Error while analyzing error log: " + StringUtils.toString(e));
    } finally {
      IoUtils.closeHard(reader);
    }
  }


  /**
   * Callback method - this method is called before execute.
   */
  protected void preExecute() throws IOException, AgentFailureException {
    // validate
    validateCheckoutDir();
    validateCommand(getCommand());

    if (log.isDebugEnabled() && StringUtils.systemPropertyEquals("parabuild.cvscmdd.enabled", "true")) {
      log.debug("command: " + getCommand());
    }
    // create CVS password first
    final CVSPasswordGenerator cvsPasswordGenerator = new CVSPasswordGenerator();
    cvsPasswordGenerator.setPassword(cvsPassword);
    cvsPasswordGenerator.setCVSRoot(cvsRoot);
    final String password = cvsPasswordGenerator.generatePassword();
    remotePasswFileName = agent.createTempFile(".auto", ".scm", password);
    super.addEnvironment("CVS_PASSFILE", remotePasswFileName);
    super.addEnvironment("CVS_RSH", cvsRshPath);
  }


  public void setCVSPassword(final String cvsPassword) {
    this.cvsPassword = cvsPassword;
  }


  public void setCVSRoot(final String cvsRoot) {
    this.cvsRoot = cvsRoot;
  }


  public void setCVSRshPath(final String cvsRshPath) {
    this.cvsRshPath = cvsRshPath;
  }


  /**
   * Removes temp files
   */
  public final void cleanup() throws AgentFailureException {
    super.cleanup();
    agent.deleteTempFileHard(remotePasswFileName);
  }


  /**
   * Validates checkout dir
   */
  private void validateCheckoutDir() throws IOException, AgentFailureException {
    if (remoteCurrentDir == null) {
      throw new IllegalArgumentException("CVS checkout directory can not be null");
    }
    if (!agent.checkoutDirExists()) {
      throw new IllegalArgumentException("CVS checkout directory \"" + remoteCurrentDir + "\" doesn't exist");
    }
  }


  /**
   * Validates command
   *
   * @param command
   */
  private void validateCommand(final String command) {
    if (StringUtils.isBlank(command)) {
      throw new IllegalArgumentException("CVS command is blank");
    }
  }


  public String toString() {
    return "CVSCommand{" +
            "remotePasswFileName='" + remotePasswFileName + '\'' +
            ", cvsRoot='" + cvsRoot + '\'' +
            ", cvsPassword='" + cvsPassword + '\'' +
            ", cvsRshPath='" + cvsRshPath + '\'' +
            '}';
  }
}
