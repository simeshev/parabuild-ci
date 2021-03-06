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
package org.parabuild.ci.remote;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.parabuild.ci.build.AgentFailureException;
import org.parabuild.ci.remote.services.RemoteFileDescriptor;

/**
 *
 */
abstract class AbstractWarapperScriptGenerator implements WrapperScriptGenerator {

  /**
   * @noinspection UNUSED_SYMBOL,UnusedDeclaration
   */
  private static final Log log = LogFactory.getLog(AbstractWarapperScriptGenerator.class); // NOPMD

  private static final String VAR_PARABUILD_SCRIPT = "PARABUILD_SCRIPT";
  private static final String VAR_CLASSPATH = "CLASSPATH";
  private static final String[] VARS_TO_ERASE = new String[]{
          "_JAVACMD",
          "_EXECJAVA",
          "PARABUILD_BASE",
          "PARABUILD_HOME",
          "PARABUILD_OPTS",
          "PARABUILD_TMPDIR",
          "PARABUILD_JAVA_HOME",
          "DEBUG_OPTS",
          "JAVA_ENDORSED_DIRS",
          "JAVA_OPTS",
          "JPDA_ADDRESS",
          "JPDA_TRANSPORT",
          "JSSE_HOME",
          "MAINCLASS",
          "SECURITY_POLICY_FILE"
  };

  protected String currentDir = null;
  protected final Agent agent;

  private final Map addedVariables = new HashMap(11);
  private String stepScriptPath = null;


  protected AbstractWarapperScriptGenerator(final Agent agent) {
    this.agent = agent;
  }


  /**
   * Sets execution directory. Execution directory
   * is the directory where the script is running from.
   */
  public final void setExecutionDirectory(final String currentDir) {
    this.currentDir = currentDir;
  }


  /**
   * Adds sheel variables to be present in the script.
   *
   * @param variables a Map with a shell variable name as a key
   *                  and variable value as value.
   */
  public final void addVariables(final Map variables) {
    this.addedVariables.putAll(variables);
  }


  /**
   * Generates wrapper sequence script file for further execution.
   *
   * @param command that will be wrapped
   * @return String absolute path to created sctep script file.
   * @throws IOException
   */
  public String generateScript(final String command) throws IOException, AgentFailureException {
    final String prefix = agent.isWindows() ? "cmd" : ".cmd";
    stepScriptPath = agent.createTempFile(prefix, getScriptSuffix(), "");
    final StringWriter result = new StringWriter(300);
    final BufferedWriter writer = makeWriter(result);
    writeProlog(writer);
    writeCleanupVarsCommands(writer);
    writeCommonCommands(writer);
    writeAddedVariables(writer);
    writeChangeDir(writer);
    writer.write(command);
    writer.newLine();
    writeEpilog(writer);
    writer.flush();
    writer.close();

    // re-create file
    agent.deleteTempFileHard(stepScriptPath);
    final String content = result.toString();
//    if (log.isDebugEnabled()) log.debug("========================= Wrapper content begin =========================");
//    if (log.isDebugEnabled()) log.debug(content);
//    if (log.isDebugEnabled()) log.debug("========================= Wrapper content end =========================");
    agent.createFile(stepScriptPath, content);
    return stepScriptPath;
  }


  protected final StringBuffer cleanupPathLikeEnvVariable(final String varName) throws IOException, AgentFailureException {
//    if (log.isDebugEnabled()) log.debug("Cleanup variables");
    final String catalinaHome = agent.getSystemProperty("catalina.base");
    final RemoteFileDescriptor fileDescriptor = agent.getFileDescriptor(catalinaHome + "//..");
    final StringBuffer resultingPathEnvVar = new StringBuffer(50);
    String originalPathEnvVar = agent.getEnvVariable(varName);
    if (originalPathEnvVar == null) { // second check for windows
      // REVIEWME: vimeshev - 2006-10-04 - instead of hacking the
      // case a better approach would be passing an array of
      // possible var names to this method or something alike.
      originalPathEnvVar = agent.getEnvVariable(varName.toUpperCase());
    }
    if (originalPathEnvVar != null) {
      final String pathSeparator = agent.pathSeparator();
      //System.out.println("DEBUG: pathSeparator = " + pathSeparator);
      for (final StringTokenizer st = new StringTokenizer(originalPathEnvVar, pathSeparator); st.hasMoreTokens();) {
        final String pathElem = st.nextToken();
        if (pathElem.startsWith(fileDescriptor.getCanonicalPath()) || pathElem.length() == 0) {
          //System.out.println("DEBUG: pathElem = " + pathElem);
          continue;
        }
        resultingPathEnvVar.append(pathElem).append(pathSeparator);
      }
    }
    return resultingPathEnvVar;
  }


  protected final void writeCleanupVarsCommands(final BufferedWriter writer) throws IOException, AgentFailureException {
    // cleanup paths
    writeSetCommand(writer, pathVarName(), cleanupPathLikeEnvVariable(pathVarName()).toString());
    writeSetCommand(writer, VAR_CLASSPATH, cleanupPathLikeEnvVariable(VAR_CLASSPATH).toString());

    // reset wars
    for (int i = 0; i < VARS_TO_ERASE.length; i++) {
      writeSetCommand(writer, VARS_TO_ERASE[i], "");
    }
  }


  protected final void writeCommonCommands(final BufferedWriter writer) throws IOException {
    // process "signature" env vars
    writeSetCommand(writer, VAR_PARABUILD_SCRIPT, stepScriptPath);
  }


  /**
   * Outputs optional added variables.
   *
   * @see #addVariables(Map)
   */
  protected final void writeAddedVariables(final BufferedWriter writer) throws IOException {
    for (final Iterator i = addedVariables.entrySet().iterator(); i.hasNext();) {
      final Map.Entry nameValuePair = (Map.Entry) i.next();
      writeSetCommand(writer, (String) nameValuePair.getKey(), (String) nameValuePair.getValue());
    }
  }


  /**
   * Returns a list of variables to erase
   */
  public static final String[] getVarsToErase() {
    final String[] src = VARS_TO_ERASE;
    final String[] result = new String[src.length];
    System.arraycopy(src, 0, result, 0, src.length);
    return result;
  }


  /**
   * Writes a string and a newline after the string
   */
  protected final void writeln(final BufferedWriter bw, final String s) throws IOException {
    bw.write(s);
    bw.newLine();
  }


  protected abstract void writeSetCommand(BufferedWriter scriptWriter, String variable, String value) throws IOException;


  protected abstract String pathVarName();


  /**
   * @return suffix such as ".bat" or ".sh"
   */
  protected abstract String getScriptSuffix();


  /**
   * This method must be overtritten to contain a platform-specific
   * commmand to CD to a current directory.
   *
   * @param writer created in {@link #makeWriter} method
   */
  protected abstract void writeChangeDir(BufferedWriter writer) throws IOException, AgentFailureException;


  /**
   * This method may be overtritten to contain a optional
   * script prolog lines. An implementot should use this chance
   * to write at the very beginning of the script.
   *
   * @param writer created in {@link #makeWriter} method
   */
  protected abstract void writeProlog(BufferedWriter writer) throws IOException;


  /**
   * This method by be overtritten to contain a optional
   * script epilog lines. An implementor should use this chance
   * to write at the very end of the script.
   *
   * @param writer created in {@link #makeWriter} method
   */
  protected abstract void writeEpilog(BufferedWriter writer);


  /**
   * Should create a platform-specific buffered writer. The writer
   * will be closed by caller.
   *
   * @param result
   * @return platform-specific {@link BufferedWriter}
   */
  protected abstract BufferedWriter makeWriter(StringWriter result);


  public String toString() {
    return "AbstractWarapperScriptGenerator{" +
            "currentDir='" + currentDir + '\'' +
            ", agent=" + agent +
            ", addedVariables=" + addedVariables +
            ", stepScriptPath='" + stepScriptPath + '\'' +
            '}';
  }
}
