package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.bll.adbroker.UserAuthenticationResult;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LoginResult;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcLoginReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public abstract class LoginBaseCommand<T extends LoginUserParameters> extends CommandBase<T> {
    private LdapUser directoryUser;
    private DbUser dbUser;

    public LoginBaseCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected VdcLoginReturnValueBase createReturnValue() {
        return new VdcLoginReturnValueBase();
    }

    @Override
    public VdcLoginReturnValueBase getReturnValue() {
        return (VdcLoginReturnValueBase) super.getReturnValue();
    }

    public String getUserPassword() {
        return getParameters().getUserPassword();
    }

    public String getDomain() {
        return getParameters().getDomain();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDC_LOGIN : AuditLogType.USER_VDC_LOGIN_FAILED;
    }

    protected abstract UserAuthenticationResult authenticateUser();

    @Override
    protected void executeCommand() {
        // add user session
        setActionReturnValue(getCurrentUser());

        getReturnValue().setLoginResult(LoginResult.Autheticated);
        // Permissions for this user might been changed since last login so
        // update his isAdmin flag accordingly
        updateUserData();

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        return isUserCanBeAuthenticated() && attachUserToSession();
    }

    protected boolean attachUserToSession() {
        if (!StringUtils.isEmpty(getParameters().getSessionId())) {
            SessionDataContainer.getInstance().setUser(getParameters().getSessionId(), getCurrentUser());
        } else if (!SessionDataContainer.getInstance().setUser(getCurrentUser())) {
            return failCanDoAction(VdcBllMessages.USER_CANNOT_LOGIN_SESSION_MISSING);
        }
        return true;
    }

    protected boolean isUserCanBeAuthenticated() {
        // Check if the user is already logged in:
        dbUser = SessionDataContainer.getInstance().getUser(false);
        if (dbUser != null) {
            addCanDoActionMessage(VdcBllMessages.USER_IS_ALREADY_LOGGED_IN);
            return false;
        }

        // Find the name of the directory where the user should be
        // authenticated:
        List<String> directories = LdapBrokerUtils.getDomainsList();
        boolean found = false;
        for (String directory : directories) {
            if (StringUtils.equalsIgnoreCase(directory, getDomain())) {
                found = true;
                break;
            }
        }
        if (!found) {
            addCanDoActionMessage(VdcBllMessages.USER_CANNOT_LOGIN_DOMAIN_NOT_SUPPORTED);
            return false;
        }

        // Perform the actual authentication:
        UserAuthenticationResult result = authenticateUser();
        if (result == null) {
            addCanDoActionMessage(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            return false;
        }
        if (!result.isSuccessful()) {
            for (VdcBllMessages message : result.getErrorMessages()) {
                addCanDoActionMessage(message);
            }
            return false;
        }

        // Check that the user exists in the database, if it doesn't exist then
        // we need to add it now:
        directoryUser = result.getUser();
        dbUser = UserCommandBase.persistAuthenticatedUser(directoryUser);

        // Check login permissions. We do it here and not via the
        // getPermissionCheckSubjects mechanism, because we need the user to be logged in to
        // the system in order to perform this check. The user is indeed logged in when running every command
        // except the login command
        if (!checkUserAndGroupsAuthorization(dbUser.getId(), dbUser.getGroupIds(), getActionType().getActionGroup(), MultiLevelAdministrationHandler.BOTTOM_OBJECT_ID, VdcObjectType.Bottom, true)) {
            addCanDoActionMessage(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
            return false;
        }

        // Retrieve the MLA admin status of the user.
        // This may be redundant in some use-cases, but looking forward to Single Sign On,
        // we will want this info
        boolean isAdmin = MultiLevelAdministrationHandler.isAdminUser(dbUser);
        log.debugFormat("Checking if user {0} is an admin, result {1}", dbUser.getLoginName(), isAdmin);
        dbUser.setAdmin(isAdmin);
        setCurrentUser(dbUser);

        // Add the user password to the session, as it will be needed later
        // when trying to log on to virtual machines:
        SessionDataContainer.getInstance().setPassword(getUserPassword());

        return true;
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        if (log.isDebugEnabled()) {
            log.debugFormat("IsUserAutorizedToRunAction: login - no permission check");
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }

    private void updateUserData() {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                DbFacade.getInstance().updateLastAdminCheckStatus(dbUser.getId());
            }
        });
    }
}
