package org.parabuild.ci.webui.vcs.repository.client.server;

import org.parabuild.ci.common.InputValidator;
import org.parabuild.ci.webui.vcs.repository.common.ParabuildFlexTable;

/**
 * An empty panel to show when VCS configuration is not supported yet.
 */
public final class DummyServerAttributePanel extends VCSServerAttributePanel {

  /**
   * Creates {@link ParabuildFlexTable}.
   *
   * @param columnCount number of columns managed by {@link #flexTableIterator()}.
   */
  public DummyServerAttributePanel(final int columnCount) {

    super(columnCount);
  }


  @Override
  public boolean validate(final InputValidator inputValidator) {

    return true;
  }
}