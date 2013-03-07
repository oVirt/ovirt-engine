package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.BrokerUtils;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapReturnValueBase;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByUserNameParameters;
import org.ovirt.engine.core.bll.adbroker.UserAuthenticationResult;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LoginResult;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcLoginReturnValueBase;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public abstract class LoginBaseCommand<T extends LoginUserParameters> extends CommandBase<T> {
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

    private void HandleAuthenticationError(List<VdcBllMessages> errorMessages) {
        // check if authentication failed due to password expiration
        LdapBroker adFactory =
                LdapFactory.getInstance(BrokerUtils.getLoginDomain(getParameters().getUserName(), getDomain()));

        if (adFactory == null) {
            addCanDoActionMessage(VdcBllMessages.USER_FAILED_TO_AUTHENTICATION_WRONG_AUTHENTICATION_METHOD);
            getReturnValue().setLoginResult(LoginResult.CantAuthenticate);
            log.errorFormat(getReturnValue().getCanDoActionMessages().get(0) + " : {0}", getParameters().getUserName());
            return;
        }

        LoginResult result = LoginResult.CantAuthenticate;
        if (!Config.<String> GetValue(ConfigValues.AuthenticationMethod).toUpperCase().equals("LDAP")) {
            // In case we're using LDAP+GSSAPI/Kerberos - and there was an
            // authentication error -
            // we cannot query information about the user - we can do it only in
            // local user
            Object tempVar = null;
            Object execResult =
                    adFactory.RunAdAction(
                            AdActionType.GetAdUserByUserName,
                            new LdapSearchByUserNameParameters(getParameters().getSessionId(),
                                    getDomain(),
                                    getParameters()
                                            .getUserName()));
            if (execResult != null) {
                LdapReturnValueBase adExecResult = (LdapReturnValueBase) execResult;
                tempVar = adExecResult.getReturnValue();
            }
            LdapUser user = (LdapUser) ((tempVar instanceof LdapUser) ? tempVar : null);
            if (user != null && user.getPasswordExpired()) {
                // If the password is expired - report just the error to the user
                errorMessages.clear();
                errorMessages.add(VdcBllMessages.USER_PASSWORD_EXPIRED);
                result = LoginResult.PasswordExpired;
            }
        }
        // If for some reason the error messages list is still empty - add the general "user can't authenticate" message
        if (errorMessages.size() == 0) {
            errorMessages.add(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
        }

        for (VdcBllMessages msg : errorMessages) {
            getReturnValue().getCanDoActionMessages().add(msg.name());
        }
        getReturnValue().setLoginResult(result);
        log.errorFormat(getReturnValue().getCanDoActionMessages().get(0) + " : {0}", getParameters().getUserName());
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

    protected LdapUser ldapUser;

    @Override
    protected void executeCommand() {
        // add user session
        setActionReturnValue(getCurrentUser());
        // Persist the most updated version of the user
        UserCommandBase.persistAuthenticatedUser(ldapUser);
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
        boolean authenticated = false;
        IVdcUser vdcUser = SessionDataContainer.getInstance().getUser(false);
        if (vdcUser == null) {
            boolean domainFound = false;
            List<String> vdcDomains = LdapBrokerUtils.getDomainsList();
            for (String domain : vdcDomains) {
                if (StringUtils.equals(domain.toLowerCase(), getDomain().toLowerCase())) {
                    domainFound = true;
                    break;
                }
            }
            if (!domainFound) {
                addCanDoActionMessage(VdcBllMessages.USER_CANNOT_LOGIN_DOMAIN_NOT_SUPPORTED);
                return false;
            }

            UserAuthenticationResult result = authenticateUser();
            // If no result object is returned from authentication - create a result object with general authentication
            // error
            if (result == null) {
                result = new UserAuthenticationResult(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            }
            ldapUser = result.getUser();
            authenticated = result.isSuccessful();

            if ((!authenticated || ldapUser == null)) {
                HandleAuthenticationError(result.getErrorMessages());
                authenticated = false;
            }
        } else {
            addCanDoActionMessage(VdcBllMessages.USER_IS_ALREADY_LOGGED_IN);
        }
        if (authenticated) {
            /*
             * Check login permissions
             * We do it here and not via the getPermissionCheckSubjects mechanism, because we need the user to be logged in to
             * the system in order to perform this check. The user is indeed logged in when running every command
             * except the login command
             */
            if (!checkUserAndGroupsAuthorization(ldapUser.getUserId(), ldapUser.getGroupIds(), getActionType().getActionGroup(), MultiLevelAdministrationHandler.BOTTOM_OBJECT_ID, VdcObjectType.Bottom, true)) {
                addCanDoActionMessage(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
                return false;
            }

            // Retrieve the MLA admin status of the user.
            // This may be redundant in some use-cases, but looking forward to Single Sign On,
            // we will want this info
            VdcUser currentUser = new VdcUser(ldapUser);
            boolean isAdmin = MultiLevelAdministrationHandler.isAdminUser(currentUser);
            log.debugFormat("Checking if user {0} is an admin, result {1}", currentUser.getUserName(), isAdmin);
            currentUser.setAdmin(isAdmin);
            setCurrentUser(currentUser);
        }
        return authenticated;
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        if (log.isDebugEnabled()) {
            log.debugFormat("IsUserAutorizedToRunAction: Login - no permission check");
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
                DbFacade.getInstance().updateLastAdminCheckStatus(ldapUser.getUserId());
            }
        });
    }
}
