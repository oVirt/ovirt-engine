package org.ovirt.engine.core.bll;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authn.AuthRecord;
import org.ovirt.engine.api.extensions.aaa.Mapping;
import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.aaa.DirectoryUtils;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LoginResult;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcLoginReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public abstract class LoginBaseCommand<T extends LoginUserParameters> extends CommandBase<T> {
    protected static final Log log = LogFactory.getLog(LoginBaseCommand.class);

    private static final Map<Integer, AuditLogType> auditLogMap = new HashMap<>();
    private static final Map<Integer, VdcBllMessages> vdcBllMessagesMap = new HashMap<>();

    static {
        auditLogMap.put(Authn.AuthResult.CREDENTIALS_EXPIRED, AuditLogType.USER_ACCOUNT_PASSWORD_EXPIRED);
        auditLogMap.put(Authn.AuthResult.GENERAL_ERROR, AuditLogType.USER_VDC_LOGIN_FAILED);
        auditLogMap.put(Authn.AuthResult.CREDENTIALS_INVALID, AuditLogType.AUTH_FAILED_INVALID_CREDENTIALS);
        auditLogMap.put(Authn.AuthResult.CREDENTIALS_INCORRECT, AuditLogType.AUTH_FAILED_INVALID_CREDENTIALS);
        auditLogMap.put(Authn.AuthResult.ACCOUNT_LOCKED, AuditLogType.USER_ACCOUNT_DISABLED_OR_LOCKED);
        auditLogMap.put(Authn.AuthResult.ACCOUNT_DISABLED, AuditLogType.USER_ACCOUNT_DISABLED_OR_LOCKED);
        auditLogMap.put(Authn.AuthResult.TIMED_OUT, AuditLogType.USER_ACCOUNT_DISABLED_OR_LOCKED);

        vdcBllMessagesMap.put(Authn.AuthResult.GENERAL_ERROR, VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
        vdcBllMessagesMap.put(Authn.AuthResult.CREDENTIALS_INVALID,
                VdcBllMessages.USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWORD);
        vdcBllMessagesMap.put(Authn.AuthResult.CREDENTIALS_INCORRECT,
                VdcBllMessages.USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWORD);
        vdcBllMessagesMap.put(Authn.AuthResult.ACCOUNT_LOCKED, VdcBllMessages.USER_ACCOUNT_DISABLED);
        vdcBllMessagesMap.put(Authn.AuthResult.ACCOUNT_DISABLED, VdcBllMessages.USER_ACCOUNT_DISABLED);
        vdcBllMessagesMap.put(Authn.AuthResult.TIMED_OUT, VdcBllMessages.USER_FAILED_TO_AUTHENTICATE_TIMED_OUT);
        vdcBllMessagesMap.put(Authn.AuthResult.CREDENTIALS_EXPIRED, VdcBllMessages.USER_PASSWORD_EXPIRED);
    }

    private ExtensionProxy authnExtension;

    private AuthenticationProfile profile;

    private ExtMap authRecord;

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
        boolean result = isUserCanBeAuthenticated() && attachUserToSession();
        if (! result) {
            logAutheticationFailure();
        }
        return result;
    }

    protected boolean attachUserToSession() {
        if (!StringUtils.isEmpty(getParameters().getSessionId())) {
            SessionDataContainer.getInstance().setUser(getParameters().getSessionId(), getCurrentUser());
        } else {
            if (!SessionDataContainer.getInstance().setUser(getCurrentUser())) {
                return failCanDoAction(VdcBllMessages.USER_CANNOT_LOGIN_SESSION_MISSING);
            }

            SessionDataContainer.getInstance().refresh();

            int userSessionHardLimit = Config.<Integer> getValue(ConfigValues.UserSessionHardLimit);
            Date validTo = userSessionHardLimit != 0 ? DateUtils.addMinutes(new Date(), userSessionHardLimit) : null;
            if (authRecord.<String> get(AuthRecord.VALID_TO) != null) {
                try {
                    Date fromExtension =
                            new SimpleDateFormat("yyyyMMddHHmmssZ").parse(authRecord.<String> get(AuthRecord.VALID_TO));
                    if (validTo != null) {
                        validTo = validTo.compareTo(fromExtension) < 0 ? validTo : fromExtension;
                    } else {
                        validTo = fromExtension;
                    }
                } catch (ParseException e) {
                    log.warn("Error parsing AuthRecord.VALID_TO . Default VALID_TO value will be set on session");
                    log.debug("Exception is ", e);
                }
            }
            SessionDataContainer.getInstance().setHardLimit(validTo);
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
        profile = AuthenticationProfileRepository.getInstance().getProfile(profileName);
        if (profile == null) {
            log.errorFormat(
                "Can't login user \"{0}\" because authentication profile \"{1}\" doesn't exist.",
                loginName, profileName
            );
            addCanDoActionMessage(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            return false;
        }

        // Check that the authenticator provided by the profile supports password authentication:
        authnExtension = profile.getAuthn();
        SessionDataContainer.getInstance().setAuthn(authnExtension);
        if (!isPasswordAuth()) {
            log.errorFormat(
                "Can't login user \"{0}\" because the authentication profile \"{1}\" doesn't support password " +
                "authentication.",
                loginName, profileName
            );
            addCanDoActionMessage(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            return false;
        }
        DbUser curUser = null;
        String curPassword = null;
        SessionDataContainer sessionDataContainer = SessionDataContainer.getInstance();
        if (StringUtils.isEmpty(getParameters().getSessionId())) {
            curUser = sessionDataContainer.getUser(false);
            curPassword = sessionDataContainer.getPassword();
        } else {
            curUser = sessionDataContainer.getUser(getParameters().getSessionId(), false);
            curPassword = sessionDataContainer.getPassword(getParameters().getSessionId());
        }
        // verify that in auto login mode , user is not taken from session.
        if (curUser != null && !StringUtils.isEmpty(curPassword)) {
            loginName = curUser.getLoginName();
            password = curPassword;
        }
        // Perform the actual authentication:
        authRecord = authenticate(loginName, password);
        if (authRecord != null) {
            DirectoryUser directoryUser =
                    AuthzUtils.fetchPrincipalRecord(profile.getAuthz(), authRecord);
            if (directoryUser == null) {
                log.infoFormat(
                        "Can't login user \"{0}\" with authentication profile \"{1}\" because the user doesn't exist in the "
                                +
                                "directory.",
                        authRecord.<String> get(Authn.AuthRecord.PRINCIPAL),
                        profileName
                        );
                addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
                return false;
            }


            // Check that the user exists in the database, if it doesn't exist then we need to add it now:
            dbUser =
                    getDbUserDAO().getByExternalId(
                            AuthzUtils.getName(profile.getAuthz()),
                            directoryUser.getId());
            if (dbUser == null) {
                dbUser = new DbUser(directoryUser);
                dbUser.setId(Guid.newGuid());
            }
            dbUser.setGroupIds(DirectoryUtils.getGroupIdsFromUser(directoryUser));
            getDbUserDAO().saveOrUpdate(dbUser);

            // Check login permissions. We do it here and not via the
            // getPermissionCheckSubjects mechanism, because we need the user to be logged in to
            // the system in order to perform this check. The user is indeed logged in when running every command
            // except the login command
            if (!checkUserAndGroupsAuthorization(dbUser.getId(),
                    dbUser.getGroupIds(),
                    getActionType().getActionGroup(),
                    MultiLevelAdministrationHandler.BOTTOM_OBJECT_ID,
                    VdcObjectType.Bottom,
                    true)) {
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
        return false;

        // Check that the user exists in the directory associated to the authentication profile:
    }

    private void logEventForUser(String userName, AuditLogType auditLogType) {
        AuditLogableBase msg = new AuditLogableBase();
        msg.setUserName(userName);
        AuditLogDirector.log(msg, auditLogType);
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

    protected void logAutheticationFailure() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.setUserName(getParameters().getLoginName());
        AuditLogDirector.log(logable, AuditLogType.USER_VDC_LOGIN_FAILED);
    }

    private boolean isPasswordAuth() {
        return (authnExtension.getContext().<Long> get(Authn.ContextKeys.CAPABILITIES).longValue() &
                Authn.Capabilities.AUTHENTICATE_PASSWORD) != 0;
    }

    private ExtMap authenticate(String user, String password) {
        ExtMap result = null;
        ExtensionProxy mapper = profile.getMapper();
        if (mapper != null) {
            user = mapper.invoke(new ExtMap().mput(
                    Base.InvokeKeys.COMMAND,
                    Mapping.InvokeCommands.MAP_USER
                    ).mput(
                            Mapping.InvokeKeys.USER,
                            user),
                    true).<String> get(Mapping.InvokeKeys.USER, user);
        }

        ExtMap outputMap = authnExtension.invoke(new ExtMap().mput(
                Base.InvokeKeys.COMMAND,
                Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS
                ).mput(
                        Authn.InvokeKeys.USER,
                        user
                ).mput(
                        Authn.InvokeKeys.CREDENTIALS,
                        password
                ));

        String principal = outputMap.<String> get(Authn.InvokeKeys.PRINCIPAL);
        if (principal != null) {
            SessionDataContainer.getInstance().setPrincipal(principal);
        }
        int authResult = outputMap.<Integer>get(Authn.InvokeKeys.RESULT);
        if (authResult != Authn.AuthResult.SUCCESS) {
            log.infoFormat(
                    "Can't login user \"{0}\" with authentication profile \"{1}\" because the authentication failed.",
                    user,
                    getParameters().getProfileName());

            AuditLogType auditLogType = auditLogMap.get(authResult);
            // if found matching audit log type, and it's not general login failure audit log (which will be logged
            // anyway due to CommandBase.log)
            if (auditLogType != null && auditLogType != AuditLogType.USER_VDC_LOGIN_FAILED) {
                logEventForUser(user, auditLogType);
            }

            if (authResult == Authn.AuthResult.CREDENTIALS_EXPIRED) {
                boolean addedUserPasswordExpiredCDA = false;
                if (outputMap.<String> get(Authn.InvokeKeys.CREDENTIALS_CHANGE_URL) != null) {
                    addCanDoActionMessage(VdcBllMessages.USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED);
                    addCanDoActionMessage(String.format("$URL %1$s",
                            outputMap.<String> get(Authn.InvokeKeys.CREDENTIALS_CHANGE_URL)));
                    addedUserPasswordExpiredCDA = true;
                }
                if (outputMap.<String> get(Authn.InvokeKeys.USER_MESSAGE) != null) {
                    addCanDoActionMessage(VdcBllMessages.USER_PASSWORD_EXPIRED_CHANGE_MSG_PROVIDED);
                    addCanDoActionMessage(String.format("$MSG %1$s",
                            outputMap.<String> get(Authn.InvokeKeys.USER_MESSAGE)));
                    addedUserPasswordExpiredCDA = true;
                }
                if (!addedUserPasswordExpiredCDA) {
                    addCanDoActionMessage(VdcBllMessages.USER_PASSWORD_EXPIRED);
                }
            } else {
                VdcBllMessages msg = vdcBllMessagesMap.get(authResult);
                if (msg == null) {
                    msg = VdcBllMessages.USER_FAILED_TO_AUTHENTICATE;
                }
                addCanDoActionMessage(msg);
            }
            result = null;
        } else {
            result = outputMap.<ExtMap> get(Authn.InvokeKeys.AUTH_RECORD);
            if (mapper != null) {
                result = mapper.invoke(new ExtMap().mput(
                                Base.InvokeKeys.COMMAND,
                                Mapping.InvokeCommands.MAP_AUTH_RECORD
                                ).mput(
                                        Authn.InvokeKeys.AUTH_RECORD,
                                result
                        )
                        , true).<ExtMap> get(
                        Authn.InvokeKeys.AUTH_RECORD,
                        result
                        );
            }

        }
        return result;
    }

}
