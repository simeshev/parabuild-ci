package org.parabuild.ci.webui.vcs.repository.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RepositoryServiceAsync {

  void saveRepository(VCSRepositoryClientVO repositoryVO, AsyncCallback<Void> callback);
}
