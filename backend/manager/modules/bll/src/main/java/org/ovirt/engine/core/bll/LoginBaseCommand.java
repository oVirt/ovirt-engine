package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.AAAExtensionException;
import org.ovirt.engine.api.extensions.AAAExtensionException.AAAExtensionError;
import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.Authenticator;
import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.aaa.DirectoryUtils;
import org.ovirt.engine.core.aaa.PasswordAuthenticator;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LoginResult;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcLoginReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public abstract class LoginBaseCommand<T extends LoginUserParameters> extends CommandBase<T> {
    protected static final Log log = LogFactory.getLog(LoginBaseCommand.class);

    private static final EnumMap<AAAExtensionError, AuditLogType> auditLogMap = new EnumMap<>(AAAExtensionError.class);
    private static final EnumMap<AAAExtensionError, VdcBllMessages> vdcBllMessagesMap = new EnumMap<>(AAAExtensionError.class);

    static {
        auditLogMap.put(AAAExtensionError.CREDENTIALS_EXPIRED, AuditLogType.USER_ACCOUNT_PASSWORD_EXPIRED);
        auditLogMap.put(AAAExtensionError.GENERAL_ERROR, AuditLogType.USER_VDC_LOGIN_FAILED);
        auditLogMap.put(AAAExtensionError.INCORRECT_CREDENTIALS, AuditLogType.AUTH_FAILED_INVALID_CREDENTIALS);
        auditLogMap.put(AAAExtensionError.LOCKED_OR_DISABLED_ACCOUNT, AuditLogType.USER_ACCOUNT_DISABLED_OR_LOCKED);
        auditLogMap.put(AAAExtensionError.TIMED_OUT, AuditLogType.AUTH_FAILED_CONNECTION_TIMED_OUT);
        auditLogMap.put(AAAExtensionError.SERVER_IS_NOT_AVAILABLE, AuditLogType.AUTH_FAILED_CONNECTION_ERROR);

        vdcBllMessagesMap.put(AAAExtensionError.GENERAL_ERROR, VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
        vdcBllMessagesMap.put(AAAExtensionError.INCORRECT_CREDENTIALS,
                VdcBllMessages.USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWORD);
        vdcBllMessagesMap.put(AAAExtensionError.LOCKED_OR_DISABLED_ACCOUNT, VdcBllMessages.USER_ACCOUNT_DISABLED);
        vdcBllMessagesMap.put(AAAExtensionError.SERVER_IS_NOT_AVAILABLE,
                VdcBllMessages.USER_FAILED_TO_AUTHENTICATE_SERVER_IS_NOT_AVAILABLE);
        vdcBllMessagesMap.put(AAAExtensionError.TIMED_OUT, VdcBllMessages.USER_FAILED_TO_AUTHENTICATE_TIMED_OUT);
        vdcBllMessagesMap.put(AAAExtensionError.CREDENTIALS_EXPIRED, VdcBllMessages.USER_PASSWORD_EXPIRED);
    }

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

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDC_LOGIN : AuditLogType.USER_VDC_LOGIN_FAILED;
    }

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
        DbUser dbUser = SessionDataContainer.getInstance().getUser(false);
        if (dbUser != null) {
            addCanDoActionMessage(VdcBllMessages.USER_IS_ALREADY_LOGGED_IN);
            return false;
        }

        // Verify that the login name and password have been provided:
        String loginName = getParameters().getLoginName();
        if (loginName == null) {
            log.errorFormat(
                "Can't login user because no login name has been provided."
            );
            addCanDoActionMessage(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            return false;
        }
        String password = getParameters().getPassword();
        if (password == null) {
            log.errorFormat(
                "Can't login user \"{0}\" because no password has been provided.",
                loginName
            );
            return false;
        }

        // Check that the authentication profile name has been provided:
        String profileName = getParameters().getProfileName();
        if (profileName == null) {
            log.errorFormat(
                "Can't login user \"{0}\" because no authentication profile name has been provided.",
                loginName
            );
            addCanDoActionMessage(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            return false;
        }

        // Check that the authentication profile exists:
        AuthenticationProfile profile = AuthenticationProfileRepository.getInstance().getProfile(profileName);
        if (profile == null) {
            log.errorFormat(
                "Can't login user \"{0}\" because authentication profile \"{1}\" doesn't exist.",
                loginName, profileName
            );
            addCanDoActionMessage(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            return false;
        }

        // Check that the authenticator provided by the profile supports password authentication:
        Authenticator authenticator = profile.getAuthenticator();
        if (!(authenticator instanceof PasswordAuthenticator)) {
            log.errorFormat(
                "Can't login user \"{0}\" because the authentication profile \"{1}\" doesn't support password " +
                "authentication.",
                loginName, profileName
            );
            addCanDoActionMessage(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            return false;
        }
        PasswordAuthenticator passwordAuthenticator = (PasswordAuthenticator) authenticator;

        // Perform the actual authentication:
        try {
            passwordAuthenticator.authenticate(loginName, password);
        } catch (AAAExtensionException ex) {
            log.infoFormat(
                    "Can't login user \"{0}\" with authentication profile \"{1}\" because the authentication failed.",
                    loginName,
                    profileName);
            AuditLogType auditLogType = auditLogMap.get(ex.getError());
            //if found matching audit log type, and it's not general login failure audit log (which will be logged anyway due to CommandBase.log)
            if (auditLogType != null && auditLogType != AuditLogType.USER_VDC_LOGIN_FAILED) {
                LdapBrokerUtils.logEventForUser(loginName, auditLogType);
            }

            VdcBllMessages canDoActionMsg = vdcBllMessagesMap.get(ex.getError());

            getReturnValue().setSucceeded(false);
            if (canDoActionMsg == VdcBllMessages.USER_PASSWORD_EXPIRED) {
                boolean addedUserPasswordExpiredCDA = false;
                if (passwordAuthenticator.getChangeExpiredPasswordMsg() != null) {
                    addCanDoActionMessage(VdcBllMessages.USER_PASSWORD_EXPIRED_CHANGE_MSG_PROVIDED);
                    getReturnValue().getCanDoActionMessages().add(String.format("$MSG %1$s", passwordAuthenticator.getChangeExpiredPasswordMsg()));
                    addedUserPasswordExpiredCDA = true;
                }
                if (passwordAuthenticator.getChangeExpiredPasswordURL() != null) {
                    addCanDoActionMessage(VdcBllMessages.USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED);
                    getReturnValue().getCanDoActionMessages().add(String.format("$URL %1$s", passwordAuthenticator.getChangeExpiredPasswordURL()));
                    addedUserPasswordExpiredCDA = true;
                }
                if (!addedUserPasswordExpiredCDA) {
                    addCanDoActionMessage(VdcBllMessages.USER_PASSWORD_EXPIRED);
                }
            } else {
                getReturnValue().getCanDoActionMessages().add(canDoActionMsg.name());
            }
            return false;
        }

        // Check that the user exists in the directory associated to the authentication profile:
        Directory directory = profile.getDirectory();
        DirectoryUser directoryUser = directory.findUser(loginName);
        if (directoryUser == null) {
            log.infoFormat(
                "Can't login user \"{0}\" with authentication profile \"{1}\" because the user doesn't exist in the " +
                "directory.",
                loginName, profileName
            );
            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
            return false;
        }


        // Check that the user exists in the database, if it doesn't exist then we need to add it now:
        dbUser = getDbUserDAO().getByExternalId(directory.getName(), directoryUser.getId());
        if (dbUser == null) {
            dbUser = new DbUser(directoryUser);
            dbUser.setId(Guid.newGuid());
            String groupIds = DirectoryUtils.getGroupIdsFromUser(directoryUser);
            dbUser.setGroupIds(groupIds);
            getDbUserDAO().save(dbUser);
        }

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
        SessionDataContainer.getInstance().setPassword(password);

        return true;
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        if (log.isDebugEnabled()) {
            log.debug("IsUserAutorizedToRunAction: login - no permission check");
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
                // TODO: Will look at this later.
                // DbFacade.getInstance().updateLastAdminCheckStatus(dbUser.getId());
            }
        });
    }
}
