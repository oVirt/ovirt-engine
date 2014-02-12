package org.ovirt.engine.core.bll;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileManager;
import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbUserDAO;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter takes the login name and the authentication profile name from the authenticated principal and performs
 * an automatic login of that user, without checking any password. This assumes that the user has already been
 * authenticated by an external mechanism and that the login name and the authentication profile name are contained
 * in the name of the principal object separated by an at sign.
 */
public class AutomaticLoginFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(AutomaticLoginFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
            throws IOException, ServletException {
        doFilter((HttpServletRequest) req, (HttpServletResponse) rsp, chain);
    }

    private void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain)
            throws IOException, ServletException {
        // In order to perform the automatic login the principal needs to be populated, if it isn't then we just forward
        // the request to the next filter:
        Principal principal = req.getUserPrincipal();
        if (principal == null) {
            chain.doFilter(req, rsp);
            return;
        }

        // If the user is already logged in then this filter doesn't need to do anything else:
        DbUser dbUser = SessionDataContainer.getInstance().getUser(req.getSession().getId(), false);
        if (dbUser != null) {
            chain.doFilter(req, rsp);
            return;
        }

        // Extract the login name and the authentication profile name from the principal:
        String principalName = principal.getName();
        int index = principalName.lastIndexOf('@');
        if (index == -1) {
            log.error(
                "Can't login user because the principal name \"{}\" doesn't contain the name of the authentication " +
                "profile.",
                principalName
            );
            rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String loginName = principalName.substring(0, index);
        String profileName = principalName.substring(index + 1);

        // Check that the authentication profile exists:
        AuthenticationProfile profile = AuthenticationProfileManager.getInstance().getProfile(profileName);
        if (profile == null) {
            log.error(
                "Can't login user \"{}\" because authentication profile \"{}\" doesn't exist.",
                loginName, profileName
            );
            rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Check that the user exists in the directory associated to the authentication profile:
        Directory directory = profile.getDirectory();
        if (directory == null) {
            log.info(
                "Can't login user \"{}\" with authentication profile \"{}\" because the directory doesn't exist.",
                profileName
            );
            rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        DirectoryUser directoryUser = directory.findUser(loginName);
        if (directoryUser == null) {
            log.info(
                "Can't login user \"{}\" with authentication profile \"{}\" because the user doesn't exist in the " +
                "directory.",
                loginName, profileName
            );
            rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Check that the user exists in the database, if it doesn't exist then we need to add it now:
        DbUserDAO dbUserDao = DbFacade.getInstance().getDbUserDao();
        dbUser = dbUserDao.getByExternalId(directory.getName(), directoryUser.getId());
        if (dbUser == null) {
            dbUser = new DbUser(directoryUser);
            dbUser.setId(Guid.newGuid());
            dbUserDao.save(dbUser);
        }

        // Check if the user has permission to log in:
        PermissionDAO permissionDao = DbFacade.getInstance().getPermissionDao();
        Guid permissionId = permissionDao.getEntityPermissionsForUserAndGroups(
            dbUser.getId(),
            dbUser.getGroupIds(),
            VdcActionType.LoginUser.getActionGroup(),
            MultiLevelAdministrationHandler.BOTTOM_OBJECT_ID,
            VdcObjectType.Bottom,
            true
        );
        if (permissionId == null) {
            log.info(
                "Can't login user \"{}\" with authentication profile \"{}\" because the user doesn't have the " +
                "required permission.",
                loginName, profileName
            );
            rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Retrieve the MLA admin status of the user, this may be redundant in some use-cases, but looking forward to
        // single sign on we will want this information:
        boolean isAdmin = MultiLevelAdministrationHandler.isAdminUser(dbUser);
        dbUser.setAdmin(isAdmin);

        // Attach the user to the session:
        SessionDataContainer.getInstance().setUser(req.getSession().getId(), dbUser);

        // Forward the request to the next filter in the chain:
        chain.doFilter(req, rsp);
    }
}
