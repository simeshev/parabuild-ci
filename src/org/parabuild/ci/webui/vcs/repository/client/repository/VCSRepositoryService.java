package org.parabuild.ci.webui.vcs.repository.client.repository;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * An RPC service interface.
 */
@RemoteServiceRelativePath("service/repository")
public interface VCSRepositoryService extends RemoteService {

  /**
   * Returns a repository by its ID.
   *
   * @param repositoryVO the VO representation of a repository to save to the database.
   */
  void saveRepository(VCSRepositoryClientVO repositoryVO);
}