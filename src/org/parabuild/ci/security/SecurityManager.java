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
package org.parabuild.ci.security;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.Query;
import net.sf.hibernate.type.Type;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parabuild.ci.build.BuildState;
import org.parabuild.ci.common.FatalConfigurationException;
import org.parabuild.ci.common.StringUtils;
import org.parabuild.ci.configuration.ConfigurationManager;
import org.parabuild.ci.configuration.DisplayUserGroupVO;
import org.parabuild.ci.configuration.GroupMemberVO;
import org.parabuild.ci.configuration.ResultGroupManager;
import org.parabuild.ci.configuration.SystemConfigurationManager;
import org.parabuild.ci.configuration.SystemConfigurationManagerFactory;
import org.parabuild.ci.configuration.SystemConstants;
import org.parabuild.ci.configuration.TransactionCallback;
import org.parabuild.ci.merge.MergeManager;
import org.parabuild.ci.merge.MergeState;
import org.parabuild.ci.merge.MergeStatus;
import org.parabuild.ci.object.BuildConfig;
import org.parabuild.ci.object.BuildConfigAttribute;
import org.parabuild.ci.object.Group;
import org.parabuild.ci.object.GroupBuildAccess;
import org.parabuild.ci.object.MergeConfiguration;
import org.parabuild.ci.object.ResultGroup;
import org.parabuild.ci.object.ResultGroupAccess;
import org.parabuild.ci.object.SystemProperty;
import org.parabuild.ci.object.User;
import org.parabuild.ci.object.UserGroup;
import org.parabuild.ci.object.UserProperty;
import org.parabuild.ci.project.ProjectManager;
import org.parabuild.ci.services.BuildManager;
import org.parabuild.ci.webui.common.Pages;
import viewtier.ui.TierletContext;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Security manager
 */
public final class SecurityManager {

  private static final Log log = LogFactory.getLog(SecurityManager.class);

  /**
   * Name of a HttpSession attribute that holds current user.
   */
  public static final String SESSION_ATTRIBUTE_USER = "parabuild-user";

  /**
   * This prefix is used to form and names of build user rights cache
   * names.
   */
  private static final String BUILD_USER_RIHGTS_CACHE_NAME_PREFIX = "build_user_rights_cache_";

  /**
   * This prefix is used to form and names of result user rights cache
   * names.
   */
  private static final String RESULT_USER_RIGHTS_CACHE_NAME_PREFIX = "result_user_rights_cache_";

  /**
   * Singleton instance.
   */
  private static final SecurityManager instance = new SecurityManager();

  private final ConfigurationManager cm = ConfigurationManager.getInstance();
  private final Object lock = new Object(); // NOPMD
  private static final String SYSTEM_PASSWORD = SystemConstants.SYSTEM_PASSWORD;


  /**
   * Singleton constructor.
   */
  private SecurityManager() {
  }


  /**
   * @return singleton instance.
   */
  public static SecurityManager getInstance() {
    return instance;
  }


  /**
   * @return true if users that are not logged in can access
   *         builds belonging to Anonymous group
   */
  public boolean isAnonymousAccessEnabled() {
    return SystemConfigurationManagerFactory.getManager().getSystemPropertyValue(SystemProperty.ENABLE_ANONYMOUS_BUILDS, SystemProperty.OPTION_UNCHECKED)
            .equals(SystemProperty.OPTION_CHECKED);
  }


  /**
   * True if given user can see build results and status.
   *
   * @param userID        int user ID
   * @param activeBuildID int build ID
   * @return true if user can view build
   */
  public boolean userCanViewBuild(final int userID, final int activeBuildID) {
//
//    // check if build is accesseble for non-logged in users -
//    // then it should be accessible to this user too.
//    if (isAnonymousAccessEnabled()) {
//      // get anon group ID
//      Group anonGroup = cm.getGroupByName(Group.SYSTEM_ANONYMOUS_GROUP);
//      if (anonGroup != null) {
//        // check if build in the anon group
//        GroupBuildAccess gba = cm.getGroupBuildAccess(anonGroup.getID(), activeBuildID);
//        if (gba != null) {
//          // build is acceisble by anon users, allow access
//          // without regard to if user is logged in.
//          return true;
//        }
//      }
//    }
//
//    // if we are here, build is not accessible to anon users
//    if (userID == -1) {
//      return false; // anon user
//    } else {
//      // handle set user ID
//      Integer foundUserID = (Integer)ConfigurationManager.runInHibernate(new TransactionCallback() {
//        public Object runInTransaction() throws Exception {
//          return session.createQuery("" +
//            "select ug.userID from UserGroup as ug, GroupBuildAccess as gba" +
//            " where ug.groupID = gba.groupID and ug.userID = ? and gba.buildID = ?")
//            .setCacheable(true)
//            .setInteger(0, userID)
//            .setInteger(1, activeBuildID)
//            .uniqueResult();
//        }
//      });
//
//      return foundUserID != null && foundUserID.intValue() == userID;
//    }
    if (activeBuildID == -1) {
      return false;
    }
    final User user = userID == -1 ? (User) null : getUser(userID);
    return getUserBuildRights(user, activeBuildID).isAllowedToViewBuild();
  }


  /**
   * @return List of BuildState objects filtered accordingly to
   *         user rights.
   */
  public List getUserBuildStatuses(final int userID) {
    final List statuses = BuildManager.getInstance().getCurrentBuildsStatuses();
    for (final Iterator i = statuses.iterator(); i.hasNext();) {
      final BuildState buildState = (BuildState) i.next();
      final int buildID = buildState.getActiveBuildID();
      if (!userCanViewBuild(userID, buildID)) {
        i.remove();
      }
    }
    return statuses;
  }


  /**
   * Returns list of build statuses to be used in feeds.
   *
   * @param userID
   * @return list of build statuses to be used in feeds.
   */
  public List getFeedBuildStatuses(final int userID) {
    if (SystemConfigurationManagerFactory.getManager().isAnonymousAccessToProtectedFeedsIsEnabled()) {
      // anon access to protected builds is enabled, get all
      return BuildManager.getInstance().getCurrentBuildsStatuses();
    } else {
      // builds should be filtered
      return getUserBuildStatuses(userID);
    }
  }


  /**
   * Assign given user as a build creator. Build creator have all rights on the build, namely create, update, delete.
   *
   * @param activeBuildID active build ID
   * @param userID        user ID
   */
  public void assignBuildCreator(final int activeBuildID, final int userID) {

    // Build creator can already be assingned - if so, overwrite
    BuildConfigAttribute bca = cm.getBuildAttribute(activeBuildID, BuildConfigAttribute.BUILD_CREATOR);
    if (bca == null) {
      bca = new BuildConfigAttribute(activeBuildID, BuildConfigAttribute.BUILD_CREATOR, userID);
    } else {
      bca.setPropertyValue(userID);
    }
    cm.saveObject(bca);

    // Invalidate permission caches pertinent to this build
    invalidateBuildRightSetCache(activeBuildID);
  }


  /**
   * Adds given build ID to Anonymous group. If the group does
   * not exist, the build won't be added.
   *
   * @param activeBuildID to add.
   */
  public void addBuildToAnonymousGroup(final int activeBuildID) {
    final Group anonGroup = getGroupByName(Group.SYSTEM_ANONYMOUS_GROUP);
    if (anonGroup != null) {
      final int anonGroupID = anonGroup.getID();
      cm.validateIsActiveBuildID(activeBuildID);
      if (getGroupBuildAccess(anonGroupID, activeBuildID) == null) {
        cm.saveObject(new GroupBuildAccess(anonGroupID, activeBuildID));
        invalidateBuildRightSetCache(activeBuildID);
      }
    }
  }


  /**
   * Returns user associated with the session or null if user is
   * not logged in.
   *
   * @param request
   * @return user associated with the session or null if user is
   *         not logged in.
   */
  public User getUserFromRequest(final HttpServletRequest request) {
    final HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }
    return (User) session.getAttribute(SESSION_ATTRIBUTE_USER);
  }


  /**
   * Returns user associated with the context or null if user is
   * not logged in.
   *
   * @param context
   * @return user associated with the session or null if user is
   *         not logged in.
   */
  public User getUserFromContext(final TierletContext context) {
    if (context == null) {
      return null;
    }
    return getUserFromRequest(context.getHttpServletRequest());
  }


  /**
   * Returns user ID associated with the context or
   * User.UNSAVED_ID if user is not logged in.
   *
   * @param context
   * @return user associated with the session or null if user is
   *         not logged in.
   */
  public int getUserIDFromContext(final TierletContext context) {
    if (context == null) {
      return User.UNSAVED_ID;
    }
    return getUserIDFromRequest(context.getHttpServletRequest());
  }


  /**
   * Encrypts password.
   *
   * @param originalPassword original password to encrypt.
   * @return encrypted hex encoded password
   */
  public static String encryptPassword(final String originalPassword) {
    return SecurityUtils.encrypt(originalPassword, SYSTEM_PASSWORD);
  }


  /**
   * Decrypts password.
   *
   * @param encrypedPassword encrypted hex-encoded password to
   *                         decrypt.
   * @return encrypted hex encoded password
   */
  public static String decryptPassword(final String encrypedPassword) {
    return SecurityUtils.decrypt(encrypedPassword, SystemConstants.SYSTEM_PASSWORD);
  }


  public BuildRights getUserBuildRights(final User user, final int activeBuildID) {

    // if we are here, build is not accessible to anon users
    if (user == null || user.getUserID() == -1) {
      // check if build is accesseble for non-logged in users -
      BuildRights buildRights = getCachedBuildRightSet(activeBuildID, -1);
      if (buildRights != null) {
        return buildRights;
      }
      buildRights = getAnonymousBuildRights(activeBuildID);
      cacheBuildRightSet(activeBuildID, -1, buildRights);
      return buildRights;
    }

    // check if is an admin
    if (user.isAdmin()) {
      return BuildRights.ALL_RIGHTS;
    }

    // try to get from cache
    BuildRights buildRights = getCachedBuildRightSet(activeBuildID, user.getUserID());
    if (buildRights != null) {
      return buildRights;
    }

    // get rights
//    if (log.isDebugEnabled()) log.debug("activeBuildID: " + activeBuildID);
//    if (log.isDebugEnabled()) log.debug("user: " + user);
    final List groups = (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.createQuery("select gr from Group as gr, UserGroup as ug, GroupBuildAccess as gba" +
                " where ug.userID = ? and gba.buildID = ? and ug.groupID = gba.groupID and gr.ID = gba.groupID")
                .setCacheable(true)
                .setInteger(0, user.getUserID())
                .setInteger(1, activeBuildID)
                .list();
      }
    });

    if (groups.isEmpty()) {
      buildRights = getAnonymousBuildRights(activeBuildID);
    } else {
      // collect permissions
      boolean allowedToStartBuild = false;
      boolean allowedToStopBuild = false;
      boolean allowedToCreateBuild = false;
      boolean allowedToUpdateBuild = false;
      boolean allowedToDeleteBuild = false;
      boolean allowedToPublishResults = false;
      boolean allowedToDeleteResults = false;
      boolean allowedToActivateBuild = false;
      boolean allowedToViewBuild = true; // as it is a member of the group, it should be viewable
      for (final Iterator i = groups.iterator(); i.hasNext();) {
        final Group group = (Group) i.next();
//        if (log.isDebugEnabled()) log.debug("group: " + group);
        allowedToCreateBuild |= group.isAllowedToCreateBuild();
        allowedToDeleteBuild |= group.isAllowedToDeleteBuild();
        allowedToStartBuild |= group.isAllowedToStartBuild();
        allowedToStopBuild |= group.isAllowedToStopBuild();
        allowedToUpdateBuild |= group.isAllowedToUpdateBuild();
        allowedToPublishResults |= group.isAllowedToPublishResults();
        allowedToDeleteResults |= group.isAllowedToDeleteResults();
        allowedToViewBuild |= group.isAllowedToViewBuild();
        allowedToActivateBuild |= group.isAllowedToActivateBuild();
      }

      // Augment group-based rights with the rights of the build creator. See PARABUILD-1418 for more information.
      final Integer creatorID = cm.getBuildAttributeValue(activeBuildID, BuildConfigAttribute.BUILD_CREATOR, User.UNSAVED_INTEGER_ID);
      if (creatorID.intValue() == user.getUserID()) {
        // Creator is set
        allowedToDeleteBuild = true;
        allowedToStartBuild = true;
        allowedToStopBuild = true;
        allowedToUpdateBuild = true;
        allowedToViewBuild = true;
        allowedToActivateBuild = true;
      }

      // Make result
      buildRights = new BuildRights(allowedToCreateBuild, allowedToDeleteBuild,
              allowedToStartBuild, allowedToStopBuild, allowedToUpdateBuild,
              allowedToViewBuild, allowedToActivateBuild, allowedToPublishResults,
              allowedToDeleteResults);
    }

    // check if public access enabled
    final byte buildConfigAccess = getBuildConfigAccess(activeBuildID);
    if (buildConfigAccess != BuildConfig.ACCESS_PUBLIC && !buildRights.isAllowedToUpdateBuild()) {
      return cacheBuildRightSet(activeBuildID, user.getUserID(), BuildRights.NO_RIGHTS);
    }

//    if (log.isDebugEnabled()) log.debug("rightSet from SM: " + rightSet);
//    if (log.isDebugEnabled()) log.debug("buildID from SM: " + activeBuildID);
    return cacheBuildRightSet(activeBuildID, user.getUserID(), buildRights);
  }


  public BuildRights getUserBuildRights(final int userID, final int activeBuildID) {
    return getUserBuildRights(getUser(userID), activeBuildID);
  }


  private BuildRights cacheBuildRightSet(final int activeBuildID, final int userID, final BuildRights buildRights) {
    try {
      final Cache cache = getBuildRightSetCache(activeBuildID);
      if (cache != null) {
        cache.put(new Element(new Integer(userID), buildRights));
      }
    } catch (CacheException e) {
      if (log.isDebugEnabled()) {
        log.debug("e: " + e);
      }
    }
    return buildRights;
  }


  private BuildRights getCachedBuildRightSet(final int activeBuildID, final int userID) {
    BuildRights buildRights = null;
    try {
      final Cache cache = getBuildRightSetCache(activeBuildID);
      if (cache == null) {
        return null;
      }
      final Element element = cache.get(new Integer(userID));
      if (element != null) {
        buildRights = (BuildRights) element.getValue();
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("e: " + e);
      }
    }
//    if (log.isDebugEnabled()) log.debug("cached rightSet for build ID " + activeBuildID + ", user ID " + userID + " : " + rightSet);
    return buildRights;
  }


  /**
   * Returns or creates build right set cache.
   */
  private Cache getBuildRightSetCache(final int activeBuildID) throws CacheException {
    return getAccessRightsCache(makeBuildCacheRegionName(activeBuildID));
  }


  private String makeBuildCacheRegionName(final int activeBuildID) {
    return BUILD_USER_RIHGTS_CACHE_NAME_PREFIX + activeBuildID;
  }


  private BuildRights getAnonymousBuildRights(final int activeBuildID) {
    if (!isAnonymousAccessEnabled()) {
      return BuildRights.NO_RIGHTS;
    }
    // get anon group ID
    final Group anonGroup = getGroupByName(Group.SYSTEM_ANONYMOUS_GROUP);
    if (anonGroup == null) {
      return BuildRights.NO_RIGHTS;
    }
    // check if build in the anon group
    final GroupBuildAccess gba = getGroupBuildAccess(anonGroup.getID(), activeBuildID);
    if (gba != null) {
      // check if build is marked as public
      final byte buildConfigAccess = getBuildConfigAccess(activeBuildID);
      if (buildConfigAccess == BuildConfig.ACCESS_PUBLIC) {
        // build is acceisble by anon users, allow access
        // without regard to if user is logged in.
        return BuildRights.VIEW_ONLY_RIGHTS;
      }
    }
    return BuildRights.NO_RIGHTS;
  }


  public BuildRights groupToBuildRights(final Group group) {
    final BuildRights result;
    if (group.getName().equals(Group.SYSTEM_ADMIN_GROUP)) {
      result = BuildRights.ALL_RIGHTS;
    } else if (group.getName().equals(Group.SYSTEM_ANONYMOUS_GROUP)) {
      result = BuildRights.VIEW_ONLY_RIGHTS;
    } else {
      result = new
              BuildRights(group.isAllowedToCreateBuild(),
              group.isAllowedToDeleteBuild(),
              group.isAllowedToStartBuild(),
              group.isAllowedToStopBuild(),
              group.isAllowedToUpdateBuild(),
              group.isAllowedToViewBuild(),
              group.isAllowedToStartBuild() || group.isAllowedToStopBuild(),
              group.isAllowedToPublishResults(),
              group.isAllowedToDeleteResults());
    }
    return result;
  }


  /**
   * Finds user by name
   *
   * @param userName to check
   * @return boolean if exists
   */
  public User getUserByName(final String userName) {
    return (User) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final List result = session.find("from User as u where u.name = ?",
                new String[]{userName.toLowerCase()},
                new Type[]{Hibernate.STRING});
        if (result.size() == 1) {
          return result.get(0);
        }
        return null;
      }
    });
  }


  /**
   * Finds administrator
   *
   * @param userName     to check
   * @param userPassword to check
   * @return boolean if exists
   */
  public boolean administratorExists(final String userName, final String userPassword) throws NoSuchAlgorithmException {
    final User user = getUserByNameAndPassword(userName, StringUtils.digest(userPassword));
    return user != null && user.isAdmin();
  }


  /**
   * Finds user by name
   *
   * @param ID to retrieve
   * @return non-null User object if exists
   */
  public User getUser(final int ID) {
    return (User) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.get(User.class, new Integer(ID));
      }
    });
  }


  /**
   * Returns all users.
   */
  public List getAllUsers() {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.find("select from User order by name");
      }
    });
  }


  /**
   * Returns all admin users.
   */
  public List getAdminUsers() {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.find("select from User user where user.admin = yes and user.enabled = yes order by user.name");
      }
    });
  }


  /**
   * Returns all groups.
   */
  public List getAllGroups() {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.find("select from Group order by name");
      }
    });
  }


  /**
   * Returns group by ID
   *
   * @param ID to retrieve
   * @return non-null Group object if exists
   */
  public Group getGroup(final int ID) {
    return (Group) cm.getObject(Group.class, ID);
  }


  /**
   * Returns group by name
   */
  public Group getGroupByName(final String name) {
    return (Group) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final Query q = session.createQuery("from Group where upper(name) = ?");
        q.setCacheable(true);
        q.setString(0, name.toUpperCase());
        return q.uniqueResult();
      }
    });
  }


  /**
   * Finds user by name
   *
   * @return boolean if exists
   */
  public User getUserByNameAndEmail(final String userName, final String userEmail) {
    return (User) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final List result = session.find("from User as u where u.name = ? and u.email=?",
                new String[]{userName.toLowerCase(), userEmail.toLowerCase()},
                new Type[]{Hibernate.STRING, Hibernate.STRING});
        if (result.size() == 1) {
          return result.get(0);
        }
        return null;
      }
    });
  }


  /**
   * @return true only if default admin user with default
   *         password is defined. I.e. there were no changes made
   *         to users after installation.
   */
  public boolean onlyDefaultAdminExists() {
    return ((Boolean) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        // first check number of users
        final Iterator i = session.iterate("select count(*) from usr in class User");
        int count = 0;
        if (i.hasNext()) {
          count = ((Number) i.next()).intValue();
        }
        return Boolean.valueOf(count == 1 && administratorExists(User.DEFAULT_ADMIN_USER, User.DEFAULT_ADMIN_PASSW));
      }
    })).booleanValue();
  }


  /**
   * Logs in administrator
   *
   * @param userName     to login
   * @param userPassword to login
   * @return User, null if not found
   */
  public User loginAdministrator(final String userName, final String userPassword) throws NoSuchAlgorithmException {
    final String digestedPassword = StringUtils.digest(userPassword);
    return (User) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final List result = session.find("from User as a"
                + " where a.name=? and a.password=? and a.admin= 'Y'",
                new Object[]{userName.toLowerCase(), digestedPassword},
                new Type[]{Hibernate.STRING, Hibernate.STRING});
        if (result.size() != 1) {
          return null;
        }
        return result.get(0);
      }
    });
  }


  /**
   * Returns user by name and password.
   *
   * @param userName     to login
   * @param userPassword to login
   * @return User, null if not found
   * @throws FatalConfigurationException if digest can not be created.
   */
  public User login(final String userName, final String userPassword) throws FatalConfigurationException {
    if (StringUtils.isBlank(userName)) {
      return null;
    }
    try {
      if (!SystemConfigurationManagerFactory.getManager().isLDAPAuthenticationEnabled()) {
        // LDAP is not enabled, do straight authentication via Parabuild DB
//        if (log.isDebugEnabled()) log.debug("LDAP is not enabled, do straight authentication via Parabuild DB");
        return getUserByNameAndPassword(userName, StringUtils.digest(userPassword));
      }

      // even if LDAP is enabled it is possible that a
      // particular user is not set to be authenticated via
      // LDAP
      final User userByName = getUserByName(userName);
//        if (log.isDebugEnabled()) log.debug("userByName: " + userByName);
      if (userByName != null) {
        //
        // user with the given name exists in Parabuild DB
        //
        if (userByName.isAuthenticateUsingLDAP()) {
//            if (log.isDebugEnabled()) log.debug("authenticate using LDAP");
          // authenticate using LDAP
          final JNDIUser jndiUser = authenticateUsingLDAP(userName, userPassword);
          if (jndiUser == null) {
            return null;
          }

          // user successfuly authenticated
//            if (log.isDebugEnabled()) log.debug("user successfuly authenticated");
          return userByName;
        } else {
          // user doesn't want to be authenticated via LDAP
//            if (log.isDebugEnabled()) log.debug("user doesn't want to be authenticated via LDAP");
          final String digestedPassword = StringUtils.digest(userPassword);
          if (!userByName.getPassword().equals(digestedPassword)) {
            return null;
          }
          // user successfuly authenticated
//            if (log.isDebugEnabled()) log.debug("user successfuly authenticated");
          return userByName;
        }
      } else {
//          if (log.isDebugEnabled()) log.debug("jndiUser with given name doesn't exist in Parabuild DB");
        //
        // jndiUser with given name doesn't exist in Parabuild DB
        //
        // try to authenticate using LDAP
        final JNDIUser jndiUser = authenticateUsingLDAP(userName, userPassword);
//          if (log.isDebugEnabled()) log.debug("jndiUser: " + jndiUser);
        if (jndiUser == null) {
          return null;
        }

        // check if e-mail is present
//          if (log.isDebugEnabled()) log.debug("authenticated jndiUser: " + jndiUser);
        if (StringUtils.isBlank(jndiUser.getEmail())) {
          throw new FatalConfigurationException("E-mail for user \"" + userName + "\" cannot be found. Please contact server administrator.");
        }

        // user successfuly authenticated via LDAP, create user
        // and optionally attach it to a group if configured.
        final User user = new User(userName.toLowerCase(), StringUtils.digest(userPassword), jndiUser.getRoles());
        user.setFullName(userName);
        user.setEmail(jndiUser.getEmail().toLowerCase());
        user.setAuthenticateUsingLDAP(true);
//          if (log.isDebugEnabled()) log.debug("saving user: " + user);
        save(user);
        final int addToGroupID = SystemConfigurationManagerFactory.getManager().getLDAPAddFirstTimeUserToGroupID();
        if (addToGroupID != Group.UNSAVED_ID) {
          save(new UserGroup(user.getUserID(), addToGroupID));
        }
        return user;
      }
    } catch (NoSuchAlgorithmException e) {
      throw new FatalConfigurationException(e);
    } catch (NamingException e) {
      throw new FatalConfigurationException(e);
    }
  }


  /**
   * Helper method to authenticate usin LDAP/JNDI authenticator.
   *
   * @param userName
   * @param userPassword
   */
  private JNDIUser authenticateUsingLDAP(final String userName, final String userPassword) throws NamingException {
    return createJNDIAuthenticator().authenticate(userName, userPassword);
  }


  /**
   * Creates JNDI Authenticator.
   *
   * @return JNDI Authenticator.
   */
  public JNDIAuthenticator createJNDIAuthenticator() {
    final SystemConfigurationManager scm = SystemConfigurationManagerFactory.getManager();
    final JNDIAuthenticator authenticator = new JNDIAuthenticator(scm.getLDAPUserLookupMode(), true);
    final String ldapConnectionPassword = scm.getLDAPConnectionPassword();
    authenticator.setConnectionCredentials(ldapConnectionPassword == null ? null : decryptPassword(ldapConnectionPassword));
    authenticator.setConnectionSecurityLevel(scm.getLDAPConnectionSecurityLevel());
    authenticator.setConnectionURL(scm.getLDAPConnectionURL());
    authenticator.setConnectionPrincipal(scm.getLDAPConnectionUserName());
    authenticator.setDigestAlgorithm(scm.getLDAPCredentialsDigest());
    authenticator.setSearchEntireSubtree(scm.getLDAPSearchEntireSubtree());
    authenticator.setUserBase(scm.getLDAPBaseElementForUserSearches());
    authenticator.setUserDistinguishedNameTemplate(scm.getLDAPUserDistinguishedNameTemplate());
    authenticator.setUserPasswordAttributeName(scm.getLDAPUserPasswordAttributeName());
    authenticator.setUserSearchTemplate(scm.getLDAPUserSearchTemplate());
    authenticator.setUserEmailAttributeName(scm.getLDAPUserEmailAttributeName());
    authenticator.setLDAPVersion(scm.getLDAPVersion());
    authenticator.setReferrals(scm.getLDAPReferrals());
    return authenticator;
  }


  private User getUserByNameAndPassword(final String userName, final String digestedPassword) {
    return (User) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.createQuery("from User as a"
                + " where a.name=? and a.password=?")
                .setString(0, userName.toLowerCase())
                .setString(1, digestedPassword)
                .setCacheable(true)
                .uniqueResult();
      }
    });
  }


  /**
   * @return List of user properties
   */
  public List getUserProperties(final int userID) {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final Query q = session.createQuery("from UserProperty as up where up.userID = ?");
        q.setInteger(0, userID);
        q.setCacheable(true);
        return q.list();
      }
    });
  }


  /**
   * @return user property
   */
  public UserProperty getUserProperty(final int userID, final String propertyName) {
    return (UserProperty) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final Query q = session.createQuery("from UserProperty as up where up.userID = ? and up.name = ?");
        q.setInteger(0, userID);
        q.setString(1, propertyName);
        q.setCacheable(true);
        return q.uniqueResult();
      }
    });
  }


  /**
   * @return user property
   */
  public String getUserPropertyValue(final int userID, final String propertyName) {
    return (String) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final Query q = session.createQuery("select up.value from UserProperty as up where up.userID = ? and up.name = ?");
        q.setInteger(0, userID);
        q.setString(1, propertyName);
        q.setCacheable(true);
        return q.uniqueResult();
      }
    });
  }


  /**
   * @return list of displaybale user groups. The list contain
   *         both groups user is a memeber and not. This list is
   *         suitable for displaying editable group list.
   * @see DisplayUserGroupVO
   */
  public List getDisplayUserGroups(final int userID) {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final List result = new ArrayList(11);

        // first, get goups user belongs to
        Query q = session.createQuery("from UserGroup ug where ug.userID = ?")
                .setCacheable(true)
                .setInteger(0, userID);
        for (final Iterator i = q.iterate(); i.hasNext();) {
          final UserGroup userGroup = (UserGroup) i.next();
          final int groupID = userGroup.getGroupID();
          final Group group = getGroup(groupID);
          result.add(new DisplayUserGroupVO(true, groupID, group.getName()));
        }

        // second, add "not there" groups
        q = session.createQuery("from Group gr " +
                "where gr.ID not in " +
                "(select ug.groupID from UserGroup ug where ug.userID = ?)")
                .setCacheable(true)
                .setInteger(0, userID);
        for (final Iterator i = q.iterate(); i.hasNext();) {
          final Group group = (Group) i.next();
          if (group.getName().equals(Group.SYSTEM_ANONYMOUS_GROUP)) {
            // do not add anon group - users should not be added to anon group.
            continue;
          }
          result.add(new DisplayUserGroupVO(false, group.getID(), group.getName()));
        }

        // sort and return
        Collections.sort(result, DisplayUserGroupVO.GROUP_NAME_ORDER);
        return result;
      }
    });
  }


  /**
   * @param groupID
   * @return List of users who are members of the given group.
   */
  public List getGroupUsers(final int groupID) {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final Query q = session.createQuery("select u from User u, UserGroup ug " +
                " where ug.groupID = ? and ug.userID = u.userID")
                .setCacheable(true)
                .setInteger(0, groupID);
        return q.list();
      }
    });
  }


  public List getSecurityGroupBuilds(final int groupID) {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final List result = new ArrayList(11);

        // first, get builds memeber of this group
        Query q = session.createQuery("select bc.buildName, bc.activeBuildID " +
                " from BuildConfig bc, ActiveBuild ab, GroupBuildAccess gba " +
                " where gba.groupID = ? " +
                "   and gba.buildID = ab.ID " +
                "   and ab.deleted = no " +
                "   and ab.ID = bc.buildID")
                .setCacheable(true)
                .setInteger(0, groupID);
        for (final Iterator i = q.iterate(); i.hasNext();) {
          final Object[] groupBuildAccess = (Object[]) i.next();
          final Integer activeBuildID = (Integer) groupBuildAccess[1];
          final String buildName = (String) groupBuildAccess[0];
          result.add(new GroupMemberVO(true, activeBuildID.intValue(), buildName));
        }

        // second, add "not there" builds
        q = session.createQuery("select abc from ActiveBuildConfig abc, ActiveBuild ab " +
                " where abc.buildID = ab.ID " +
                "   and ab.deleted = no " +
                "   and ab.ID not in (select gba.buildID " +
                "                       from GroupBuildAccess gba " +
                "                       where gba.groupID = ?)")
                .setCacheable(true)
                .setInteger(0, groupID);
        for (final Iterator i = q.iterate(); i.hasNext();) {
          final BuildConfig bc = (BuildConfig) i.next();
          result.add(new GroupMemberVO(false, bc.getBuildID(), bc.getBuildName()));
        }

        // sort and return
        Collections.sort(result, GroupMemberVO.NAME_ORDER);
        return result;
      }
    });
  }


  /**
   * Returns {@link List} of {@link GroupBuildAccess
   * } objects associated with the given active build
   * configuration.
   *
   * @param activeBuildConfigID
   * @return {@link List} of {@link GroupBuildAccess
   *         } objects associated with the given buildID.
   */
  public List getGroupBuildAccessList(final int activeBuildConfigID) {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final Query q = session.createQuery("from GroupBuildAccess gba where gba.buildID = ?")
                .setCacheable(true)
                .setInteger(0, activeBuildConfigID);
        return q.list();
      }
    });
  }


  /**
   * @param groupID
   * @param activeBuildID
   * @return GroupBuildAccess by groupID and buildID
   */
  public GroupBuildAccess getGroupBuildAccess(final int groupID, final int activeBuildID) {
    return (GroupBuildAccess) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.createQuery("from GroupBuildAccess gba where gba.buildID = ? and gba.groupID = ?")
                .setCacheable(true)
                .setInteger(0, activeBuildID)
                .setInteger(1, groupID)
                .uniqueResult();
      }
    });
  }


  /**
   * Saves list of build attributes
   *
   * @param settings
   */
  public void saveUserProperties(final int userID, final List settings) {
    ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        for (final Iterator i = settings.iterator(); i.hasNext();) {
          final UserProperty up = (UserProperty) i.next();
          if (up.getUserID() == User.UNSAVED_ID) {
            up.setUserID(userID);
          }
          session.saveOrUpdateCopy(up);
        }
        return null;
      }
    });
  }


  public void deleteUserFromGroup(final int userID, final int groupID) {
    ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        session.delete("from UserGroup ug where ug.userID = ? and ug.groupID = ?",
                new Object[]{new Integer(userID), new Integer(groupID)},
                new Type[]{Hibernate.INTEGER, Hibernate.INTEGER});
        return null;
      }
    });
    invalidateRightSetCaches();
  }


  public void deleteBuildFromGroup(final int activeBuildID, final int groupID) {
    if (ConfigurationManager.validateActiveID) {
      cm.validateIsActiveBuildID(activeBuildID);
    }
    ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        session.delete("from GroupBuildAccess gba where gba.buildID = ? and gba.groupID = ?",
                new Object[]{new Integer(activeBuildID), new Integer(groupID)},
                new Type[]{Hibernate.INTEGER, Hibernate.INTEGER});
        return null;
      }
    });
    invalidateBuildRightSetCache(activeBuildID);
  }


  public GroupBuildAccess save(final GroupBuildAccess groupBuildAccess) {
    if (ConfigurationManager.validateActiveID) {
      cm.validateIsActiveBuildID(groupBuildAccess.getBuildID());
    }
    return (GroupBuildAccess) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        cm.saveObject(groupBuildAccess);
        invalidateBuildRightSetCache(groupBuildAccess.getBuildID());
        return groupBuildAccess;
      }
    });
  }


  public User save(final User user) {
    return (User) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        session.saveOrUpdate(user);
        invalidateRightSetCaches();
        return user;
      }
    });
  }


  /**
   * Saves group
   *
   * @param group
   */
  public Group save(final Group group) {
    return (Group) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        session.saveOrUpdate(group);
        invalidateRightSetCaches();
        return group;
      }
    });
  }


  public UserGroup save(final UserGroup userGroup) {
    return (UserGroup) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        cm.saveObject(userGroup);
        invalidateRightSetCaches();
        return userGroup;
      }
    });
  }


  public void invalidateBuildRightSetCache(final int activeBuildID) {
    System.setProperty(SystemConstants.SYSTEM_PROPERTY_RELOAD_PRINCIPAL, "true");
    try {
      invalidate(getBuildRightSetCache(activeBuildID));
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("e: " + e);
      }
    }

    try {
      invalidate(getGeneralAccessRightsCache());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("e: " + e);
      }
    }
  }


  public void invalidateRightSetCaches() {
    System.setProperty(SystemConstants.SYSTEM_PROPERTY_RELOAD_PRINCIPAL, "true");
    try {
      final String[] cacheNames = CacheManager.getInstance().getCacheNames();
      for (int i = 0; i < cacheNames.length; i++) {
        final String cacheName = cacheNames[i];
        if (cacheName.startsWith(BUILD_USER_RIHGTS_CACHE_NAME_PREFIX)
                || cacheName.startsWith(RESULT_USER_RIGHTS_CACHE_NAME_PREFIX)) {
          final Cache cache = CacheManager.getInstance().getCache(cacheName);
          invalidate(cache);
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("e: " + e);
      }
    }
    try {
      final Cache cache = getGeneralAccessRightsCache();
      invalidate(cache);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("e: " + e);
      }
    }
  }


  public void deleteGroup(final Group group) {
    ConfigurationManager.getInstance().deleteObject(group);
    invalidateRightSetCaches();
  }


  public void deleteUser(final User user) {
    ConfigurationManager.getInstance().deleteObject(user);
    invalidateRightSetCaches();
  }


  public void deleteGroupBuildAccess(final GroupBuildAccess buildGroup) {
    if (buildGroup != null) {
      ConfigurationManager.getInstance().deleteObject(buildGroup);
      invalidateBuildRightSetCache(buildGroup.getBuildID());
    }
  }


  /**
   * Gets build configuration from request according to user
   * access rights. If user is not allowed to acces the
   * configuration, an {@link AccessForbiddenException} is
   * thrown.
   */
  public BuildConfig getBuildConfigurationFromRequest(final HttpServletRequest req) throws BadRequestException, AccessForbiddenException {

    final BuildConfig buildConfig = getBuildConfiguration(req);

    // validate user can acces it.
    final int userID = getUserIDFromRequest(req);
    if (!userCanViewBuild(userID, buildConfig.getBuildID())) {
      throw new AccessForbiddenException("User cannot access this build. Build ID: " + Integer.toString(buildConfig.getBuildID()) + ", user ID: " + Integer.toString(userID));
    }

    // return result
    return buildConfig;
  }


  /**
   * Gets build configuration from request according to feed
   * access preferences and to user access rights. If user
   * is not allowed to acces the configuration, an {@link
   * AccessForbiddenException} is thrown.
   */
  public BuildConfig getFeedBuildConfigurationFromRequest(final HttpServletRequest req) throws BadRequestException, AccessForbiddenException {
    if (SystemConfigurationManagerFactory.getManager().isAnonymousAccessToProtectedFeedsIsEnabled()) {
      // anon access to protected builds is enabled, get all
      return getBuildConfiguration(req);
    } else {
      // builds should be filtered
      return getBuildConfigurationFromRequest(req);
    }
  }


  public int getUserIDFromRequest(final HttpServletRequest req) {
    final User user = getUserFromRequest(req);
    return user != null ? user.getUserID() : User.UNSAVED_ID;
  }


  public void saveUserProperty(final UserProperty userProperty) {
    ConfigurationManager.getInstance().saveObject(userProperty);
  }


  public List findUsersByEmail(final String email) {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final Query q = session.createQuery("select u from User u where u.email = ?");
        q.setString(0, email);
        q.setCacheable(true);
        return q.list();
      }
    });
  }


  public List findUserPropertiesByValue(final String propertyName, final String propertyValue) {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final Query q = session.createQuery("from UserProperty as up where up.name = ? and up.value = ?");
        q.setString(0, propertyName);
        q.setString(1, propertyValue);
        q.setCacheable(true);
        return q.list();
      }
    });
  }


  /**
   * @param userID
   * @return refresh rate for the build status page.
   */
  public int getBuildStatusRefreshSecs(final int userID) {
    final SystemConfigurationManager systemCM = SystemConfigurationManagerFactory.getManager();
    if (userID == User.UNSAVED_ID) {
      return systemCM.getBuildStatusRefreshSecs();
    }
    final String refreshRate = getUserPropertyValue(userID, UserProperty.BUILD_STATUS_REFRESH_RATE);
    if (StringUtils.isBlank(refreshRate)) {
      return systemCM.getBuildStatusRefreshSecs();
    }
    return Integer.parseInt(refreshRate);
  }


  /**
   * Returns access for the given build ID. If a build with the
   * given ID does not exist return {@link
   * BuildConfig#ACCESS_PRIVATE}.
   */
  private byte getBuildConfigAccess(final int activeBuildID) {
    // check if it is an non-null build
    if (activeBuildID == BuildConfig.UNSAVED_ID) {
      return BuildConfig.ACCESS_PRIVATE;
    }

    final Byte access = (Byte) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final Query q = session.createQuery("select bc.access from BuildConfig bc " +
                " where bc.buildID = ?");
        q.setInteger(0, activeBuildID);
        q.setCacheable(true);
        return q.uniqueResult();
      }
    });

    // return result
    return access != null ? access.byteValue() : BuildConfig.ACCESS_PRIVATE;
  }


  /**
   * @return list of build build groups
   */
  public List getGroupList() {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.createQuery("select gr from Group as gr order by gr.name")
                .setCacheable(true)
                .list();
      }
    });
  }


  /**
   * @param tierletContext
   * @return true if user in the context can see the change list
   *         descriptions or false if cannot.
   */
  public boolean userCanSeeChangeListDescriptions(final TierletContext tierletContext) {

    //
    // REVIEWME: vimeshev 2006-09-06 - performance - every time
    // goes to SystemConfigurationManagerFactory to get a setting
    //

    // for null context show the descriptions
    if (tierletContext == null) {
      return true;
    }
    final int userIDFromRequest = getUserIDFromContext(tierletContext);

    // for non-anonymous users show the descriptions
    if (userIDFromRequest != User.UNSAVED_ID) {
      return true;
    }

    // return according to the setting
    return !SystemConfigurationManagerFactory.getManager().isHideChangeListDescriptionsFromAnonymousUsers();
  }


  public ResultGroupRights createResultGroupRightsFromgroup(final Group group) {
    final ResultGroupRights result;
    if (group.getName().equals(Group.SYSTEM_ADMIN_GROUP)) {
      result = ResultGroupRights.ALL_RIGHTS;
    } else if (group.getName().equals(Group.SYSTEM_ANONYMOUS_GROUP)) {
      result = ResultGroupRights.VIEW_ONLY_RIGHTS;
    } else {
      result = new ResultGroupRights(group.isAllowedToCreateResultGroup(),
              group.isAllowedToDeleteResultGroup(), group.isAllowedToUpdateResultGroup(),
              group.isAllowedToViewResultGroup());
    }
    return result;
  }


  /**
   * @return List of ResultGroup objects filtered accordingly to
   *         user rights.
   */
  public List getUserResultGroups(final int userID) {
    final List resultGroups = ResultGroupManager.getInstance().getResultGroups();
    for (final Iterator i = resultGroups.iterator(); i.hasNext();) {
      final ResultGroup resultGroup = (ResultGroup) i.next();
      final int resultGroupID = resultGroup.getID();
      if (!userCanViewResultGroup(userID, resultGroupID)) {
        i.remove();
      }
    }
    return resultGroups;
  }


  /**
   * True if given user can see build results and status.
   *
   * @param userID       int user ID
   * @param resutGroupID int build ID
   * @return true if user can view build
   */
  public boolean userCanViewResultGroup(final int userID, final int resutGroupID) {
    if (resutGroupID == -1) {
      return false;
    }
    final User user = userID == -1 ? (User) null : getUser(userID);
    return getUserResultGroupRights(user, resutGroupID).isAllowedToViewResultGroup();
  }


  /**
   * Returns user rights for the user associated with the given
   * context.
   */
  public ResultGroupRights getUserResultGroupRights(final TierletContext tierletContext, final int resutGroupID) {
    return getUserResultGroupRights(getUserFromContext(tierletContext), resutGroupID);
  }


  public ResultGroupRights getUserResultGroupRights(final User user, final int resutGroupID) {

    // if we are here, build is not accessible to anon users
    if (user == null || user.getUserID() == -1) {
      // check if build is accesseble for non-logged in users -
      ResultGroupRights resultGroupRights = getCachedResultGroupRightSet(resutGroupID, -1);
      if (resultGroupRights != null) {
        return resultGroupRights;
      }
      resultGroupRights = getAnonymousResultGroupRights(resutGroupID);
      cacheResultGroupRightSet(resutGroupID, -1, resultGroupRights);
      return resultGroupRights;
    }

    // check if is an admin
    if (user.isAdmin()) {
      return ResultGroupRights.ALL_RIGHTS;
    }

    // try to get from cache
    ResultGroupRights resultGroupRights = getCachedResultGroupRightSet(resutGroupID, user.getUserID());
    if (resultGroupRights != null) {
      return resultGroupRights;
    }

    // get rights
//    if (log.isDebugEnabled()) log.debug("resutGroupID: " + resutGroupID);
//    if (log.isDebugEnabled()) log.debug("user: " + user);
    final List groups = (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.createQuery("select gr from Group as gr, UserGroup as ug, ResultGroupAccess as rga" +
                " where ug.userID = ? and rga.resultGroupID = ? " +
                " and ug.groupID = rga.groupID and gr.ID = rga.groupID")
                .setCacheable(true)
                .setInteger(0, user.getUserID())
                .setInteger(1, resutGroupID)
                .list();
      }
    });

    if (groups.isEmpty()) {
      resultGroupRights = getAnonymousResultGroupRights(resutGroupID);
    } else {
      // collect permissions
      boolean allowedToCreateResultGroup = false;
      boolean allowedToUpdateResultGroup = false;
      boolean allowedToDeleteResultGroup = false;
      boolean allowedToViewResultGroup = true; // as it is a member of the group, it should be viewable
      for (final Iterator i = groups.iterator(); i.hasNext();) {
        final Group group = (Group) i.next();
//        if (log.isDebugEnabled()) log.debug("group: " + group);
        allowedToCreateResultGroup |= group.isAllowedToCreateResultGroup();
        allowedToDeleteResultGroup |= group.isAllowedToDeleteResultGroup();
        allowedToUpdateResultGroup |= group.isAllowedToUpdateResultGroup();
        allowedToViewResultGroup |= group.isAllowedToViewResultGroup();
      }

      // make result
      resultGroupRights = new ResultGroupRights(allowedToCreateResultGroup,
              allowedToDeleteResultGroup, allowedToUpdateResultGroup,
              allowedToViewResultGroup);
    }

//    if (log.isDebugEnabled()) log.debug("rightSet from SM: " + rightSet);
//    if (log.isDebugEnabled()) log.debug("buildID from SM: " + resutGroupID);
    return cacheResultGroupRightSet(resutGroupID, user.getUserID(), resultGroupRights);
  }


  /**
   * Returns list of result groups that user has a rights to see.
   *
   * @return List of {@link ResultGroupRights}
   */
  public List getUserResultGroups(final TierletContext tierletContext) {
    return getUserResultGroups(getUserIDFromContext(tierletContext));
  }


  /**
   * @param groupID, can be -1
   * @return list of GroupMemberVO objects denoting build results
   *         that belong or can be made beloning to or removed from, a
   *         given security group {@link Group}.
   */
  public List getSecurityGroupResults(final int groupID) {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final List result = new ArrayList(11);

        // first, get builds memeber of this group
        Query q = session.createQuery("from ResultGroupAccess rga " +
                "  where rga.groupID = ?")
                .setCacheable(true)
                .setInteger(0, groupID);
        for (final Iterator i = q.iterate(); i.hasNext();) {
          final ResultGroupAccess resultAccess = (ResultGroupAccess) i.next();
          final int resultGroupID = resultAccess.getResultGroupID();
          final ResultGroup rg = ResultGroupManager.getInstance().getResultGroup(resultGroupID);
          result.add(new GroupMemberVO(true, rg.getID(), rg.getName()));
        }

        // second, add "not there" result definitions
        q = session.createQuery("select rg from ResultGroup rg " +
                " where rg.ID not in (select rga.resultGroupID " +
                "                       from ResultGroupAccess rga " +
                "                       where rga.groupID = ?)")
                .setCacheable(true)
                .setInteger(0, groupID);
        for (final Iterator i = q.iterate(); i.hasNext();) {
          final ResultGroup rg = (ResultGroup) i.next();
          result.add(new GroupMemberVO(false, rg.getID(), rg.getName()));
        }

        // sort and return
        Collections.sort(result, GroupMemberVO.NAME_ORDER);
        return result;
      }
    });
  }


  public void save(final ResultGroupAccess resultGroupAccess) {
    ConfigurationManager.getInstance().saveObject(resultGroupAccess);
  }


  private ResultGroupRights getAnonymousResultGroupRights(final int resultGroupID) {
    if (!isAnonymousAccessEnabled()) {
      return ResultGroupRights.NO_RIGHTS;
    }
    // get anon group ID
    final Group anonGroup = getGroupByName(Group.SYSTEM_ANONYMOUS_GROUP);
    if (anonGroup == null) {
      return ResultGroupRights.NO_RIGHTS;
    }
    // check if right set in the anon group
    final ResultGroupAccess rga = getResultGroupAccess(anonGroup.getID(), resultGroupID);
    if (rga != null) {
      return ResultGroupRights.VIEW_ONLY_RIGHTS;
    }
    return ResultGroupRights.NO_RIGHTS;
  }


  /**
   * Returns result group access for this security group and for the result group.
   *
   * @param groupID       int security group ID
   * @param resultGroupID int result group ID
   * @return {@link ResultGroupAccess} or null if not found
   */
  private ResultGroupAccess getResultGroupAccess(final int groupID, final int resultGroupID) {
    return (ResultGroupAccess) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.createQuery("from ResultGroupAccess rga where rga.resultGroupID = ? and rga.groupID = ?")
                .setCacheable(true)
                .setInteger(0, resultGroupID)
                .setInteger(1, groupID)
                .uniqueResult();
      }
    });
  }


  private ResultGroupRights cacheResultGroupRightSet(final int activeResultGroupID, final int userID, final ResultGroupRights buildRights) {
    try {
      final Cache cache = getResultGroupRightSetCache(activeResultGroupID);
      if (cache != null) {
        cache.put(new Element(new Integer(userID), buildRights));
      }
    } catch (CacheException e) {
      if (log.isDebugEnabled()) {
        log.debug("e: " + e);
      }
    }
    return buildRights;
  }


  private Cache getResultGroupRightSetCache(final int resultGroupID) throws CacheException {
    return getAccessRightsCache(makeResultCacheRegionName(resultGroupID));
  }


  private String makeResultCacheRegionName(final int resultGroupID) {
    return RESULT_USER_RIGHTS_CACHE_NAME_PREFIX + resultGroupID;
  }


  /**
   * Returns cached {@link ResultGroupRights} for the given result group and user ID
   *
   * @return cached {@link ResultGroupRights} for the given result group and user ID.
   */
  private ResultGroupRights getCachedResultGroupRightSet(final int resultGroupID, final int userID) {
    ResultGroupRights resultGroupRights = null;
    try {
      final Cache cache = getResultGroupRightSetCache(resultGroupID);
      if (cache == null) {
        return null;
      }
      final Element element = cache.get(new Integer(userID));
      if (element != null) {
        resultGroupRights = (ResultGroupRights) element.getValue();
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("e: " + e);
      }
    }
//    if (log.isDebugEnabled()) log.debug("cached rightSet for build ID " + resultGroupID + ", user ID " + userID + " : " + rightSet);
    return resultGroupRights;
  }


  /**
   * Helper method to returns a cache for access right sets such
   * as builds and results. Caches are denoted by the given
   * region name.
   *
   * @param cacheRegionName to assign to the cache.
   * @return exist
   * @throws CacheException
   */
  private Cache getAccessRightsCache(final String cacheRegionName) throws CacheException {
    final CacheManager cacheManager = CacheManager.getInstance();
    Cache cache = cacheManager.getCache(cacheRegionName);
    if (cache == null) {
      synchronized (lock) {
        cache = cacheManager.getCache(cacheRegionName);
        if (cache == null) {
          cache = new Cache(cacheRegionName, 500, false, false, 1800L, 1800L);
          try {
            cacheManager.addCache(cache);
          } catch (CacheException e) {
            cache = cacheManager.getCache(cacheRegionName);
          }
        }
      }
    }
    return cache;
  }


  public void deleteResultGroupFromGroup(final int resultGroupID, final int groupID) {
    ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        session.delete("from ResultGroupAccess rga where rga.resultGroupID = ? and rga.groupID = ?",
                new Object[]{new Integer(resultGroupID), new Integer(groupID)},
                new Type[]{Hibernate.INTEGER, Hibernate.INTEGER});
        return null;
      }
    });
    invalidateResultRightsCache(resultGroupID);
  }


  public void invalidateResultRightsCache(final int resultGroupID) {
    System.setProperty(SystemConstants.SYSTEM_PROPERTY_RELOAD_PRINCIPAL, "true");
    try {
      invalidate(getResultGroupRightSetCache(resultGroupID));
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("e: " + e);
      }
    }
  }


  /**
   * Resturns list of projects filtered for the given user.
   *
   * @param tierletContext
   * @return list of projects filtered for the given user.
   */
  public List getUserProjects(final TierletContext tierletContext) {
    return ProjectManager.getInstance().getProjects();
  }


  /**
   * Helper method to get a build configuration from reques
   * parameters.
   *
   * @throws BadRequestException if a build configuration
   *                             doesn't exist for the given build ID stored in the
   *                             request.
   */
  private BuildConfig getBuildConfiguration(final HttpServletRequest req) throws BadRequestException {// get build ID param

    final String stringBuildID = req.getParameter(Pages.PARAM_BUILD_ID);

    // validate build id parameter
    if (!StringUtils.isValidInteger(stringBuildID)) {
      throw new BadRequestException("Not a valid integer: " + stringBuildID);
    }

    // get build config
    final int buildID = Integer.parseInt(stringBuildID);
    final BuildConfig buildConfig = (BuildConfig) cm.getObject(BuildConfig.class, buildID);
    if (buildConfig == null) {
      throw new BadRequestException("Not a valid build ID: " + stringBuildID);
    }
    return buildConfig;
  }


  /**
   * @param tierletContext
   * @return true if a user in the context can see files in the change list.
   */
  public boolean userCanSeeChangeListFiles(final TierletContext tierletContext) {
    //
    // REVIEWME: vimeshev 2007-04-22 - performance - every time
    // goes to SystemConfigurationManagerFactory to get a setting
    //

    // for null context show the descriptions
    if (tierletContext == null) {
      return true;
    }
    final int userIDFromRequest = getUserIDFromContext(tierletContext);

    // for non-anonymous users show the descriptions
    if (userIDFromRequest != User.UNSAVED_ID) {
      return true;
    }

    // return according to the setting
    return !SystemConfigurationManagerFactory.getManager().isHideChangeListFilesFromAnonymousUsers();
  }


  /**
   * REVIEWME: simeshev@parabuilci.org -> implement
   *
   * @param userID
   * @return List of {@link MergeStatus} objects
   */
  public List getUserMergeStates(final int userID) {
    final User user = userID == -1 ? (User) null : getUser(userID);
    final List statuses = MergeManager.getInstance().getMergeStatuses();
    for (final Iterator i = statuses.iterator(); i.hasNext();) {
      final MergeState state = (MergeState) i.next();
      if (!getUserMergeRights(user, state.getActiveMergeConfigurationID()).isAllowedToViewMerge()) {
        i.remove();
      }
    }
    return statuses;
  }


  /**
   * Returns user's merge rights. Current impementation
   * calculates access rights buy combining similar from
   * both source and target builds.
   *
   * @param user
   * @param activeMergeID
   */
  public MergeRights getUserMergeRights(final User user, final int activeMergeID) {
    // NOTE: vimeshev - 2006-15-01 - combine merge rights from both builds
    // REVIEWME: simeshev@parabuilci.org -> cache
    // REVIEWME: simeshev@parabuilci.org -> no point to pull out the whole thing if we need only source and target builds
    final MergeConfiguration mergeConfiguration = MergeManager.getInstance().getMergeConfiguration(activeMergeID);
    final int sourceBuildID = mergeConfiguration.getSourceBuildID();
    final int targetBuildID = mergeConfiguration.getTargetBuildID();
    final BuildRights sourceBuildRights = getUserBuildRights(user, sourceBuildID);
    final BuildRights targetBuildRights = getUserBuildRights(user, targetBuildID);
    final MergeRightsImpl result = new MergeRightsImpl();
    result.setAllowedToDeleteMerge(sourceBuildRights.isAllowedToDeleteBuild() || targetBuildRights.isAllowedToDeleteBuild());
    result.setAllowedToListCommands(sourceBuildRights.isAllowedToListCommands() || targetBuildRights.isAllowedToListCommands());
    result.setAllowedToResumeMerge(sourceBuildRights.isAllowedToResumeBuild() || targetBuildRights.isAllowedToResumeBuild());
    result.setAllowedToStopMerge(sourceBuildRights.isAllowedToStopBuild() || targetBuildRights.isAllowedToStopBuild());
    result.setAllowedToStartMerge(sourceBuildRights.isAllowedToStartBuild() || targetBuildRights.isAllowedToStartBuild());
    result.setAllowedToViewMerge(sourceBuildRights.isAllowedToViewBuild() || targetBuildRights.isAllowedToViewBuild());
    return result;
  }


  /**
   * @param userID
   */
  public int getMergeStatusRefreshSecs(final int userID) {
    return getBuildStatusRefreshSecs(userID);
  }


  /**
   * Gets user name by user ID
   *
   * @return boolean if exists
   */
  public String getUserName(final int userID, final String defaultValue) {
    return (String) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        final Query query = session.createQuery("select u.name from User as u where u.userID = ?");
        query.setCacheable(true);
        query.setInteger(0, userID);
        final String name = (String) query.uniqueResult();
        return name == null ? defaultValue : name;
      }
    });
  }


  public List findUsersWithEditRights(final int activeBuildID) {
    return (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
      public Object runInTransaction() throws Exception {
        return session.createQuery("select u from User u, Group as gr, UserGroup as ug, GroupBuildAccess as gba" +
                " where gba.buildID = ? " +
                " and gba.groupID = ug.groupID " +
                " and ug.userID = u.userID " +
                " and gr.ID = ug.groupID " +
                " and gr.allowedToUpdateBuild = yes ")
                .setCacheable(true)
                .setInteger(0, activeBuildID)
                .list();
      }
    });
  }


  /**
   * Creates or sets user property.
   *
   * @param userID
   * @param name
   * @param value
   */
  public void setUserProperty(final int userID, final String name, final boolean value) {
    UserProperty userProperty = getUserProperty(userID, name);
    if (userProperty == null) {
      userProperty = new UserProperty();
      userProperty.setUserID(userID);
      userProperty.setName(name);
    }
    userProperty.setValue(Boolean.toString(value));
    saveUserProperty(userProperty);
  }


  /**
   * Returns true if a user is allowed to see errors.
   *
   * @param user
   * @return
   */
  public boolean isAllowedToSeeErrors(final User user) {
    try {

      // Check if admin.
      if (user.isAdmin()) {
        return true;
      }

      // Get cache
      final Cache cache = getGeneralAccessRightsCache();
      if (cache == null) {
        return false;
      }

      // Return cached if present
      final Integer userID = new Integer(user.getUserID());
      final Element element = cache.get(userID);
      if (element != null) {
        final Boolean allowed = (Boolean) element.getValue();
        return allowed.booleanValue();
      }

      // Calcualte access rights
      final List groups = (List) ConfigurationManager.runInHibernate(new TransactionCallback() {
        public Object runInTransaction() throws Exception {
          return session.createQuery("select gr from User u, Group as gr, UserGroup as ug, GroupBuildAccess as gba" +
                  " where u.userID = ? " +
                  " and gba.groupID = ug.groupID " +
                  " and ug.userID = u.userID " +
                  " and gr.ID = ug.groupID ")
                  .setCacheable(true)
                  .setInteger(0, user.getUserID())
                  .list();
        }
      });
      boolean allowedToSeeErrors = false;
      for (final Iterator iterator = groups.iterator(); iterator.hasNext() && !allowedToSeeErrors;) {
        final Group group = (Group) iterator.next();
        allowedToSeeErrors |= group.isAllowedToCreateBuild();
        allowedToSeeErrors |= group.isAllowedToDeleteBuild();
        allowedToSeeErrors |= group.isAllowedToStartBuild();
        allowedToSeeErrors |= group.isAllowedToStopBuild();
        allowedToSeeErrors |= group.isAllowedToUpdateBuild();
      }

      // Cache
      cache.put(new Element(userID, Boolean.valueOf(allowedToSeeErrors)));

      // Return result
      return allowedToSeeErrors;

    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("e: " + e);
      }
      return false;
    }
  }


  /**
   * Returns true if user associated with the given context is an admin user.
   *
   * @param tierletContext
   * @return true if user associated with the given context is an admin user. Otherwise returns false.
   */
  public boolean isAdmin(TierletContext tierletContext) {

    if (tierletContext == null) {
      return false;
    }

    final HttpServletRequest req = tierletContext.getHttpServletRequest();
    final User user = getUserFromRequest(req);
    return user == null ? false : user.isAdmin();
  }


  private Cache getGeneralAccessRightsCache() throws CacheException {
    return getAccessRightsCache("general.access.rights");
  }


  private void invalidate(final Cache cache) throws IOException {
    if (cache != null) {
      cache.removeAll();
    }
  }


  public String toString() {
    return "SecurityManager{" +
            "cm=" + cm +
            ", lock=" + lock +
            '}';
  }
}
