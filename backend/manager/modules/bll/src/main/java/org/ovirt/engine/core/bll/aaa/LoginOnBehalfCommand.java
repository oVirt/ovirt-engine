package org.ovirt.engine.core.bll.aaa;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LoginOnBehalfParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class LoginOnBehalfCommand<T extends LoginOnBehalfParameters> extends CommandBase<T> {
    protected static final Logger log = LoggerFactory.getLogger(LoginOnBehalfCommand.class);
    private String logInfo;

    @Inject
    private SessionDataContainer sessionDataContainer;

    @Inject
    private DirectoryUtils directoryUtils;

    @Inject
    private DbUserDao dbUserDao;

    public LoginOnBehalfCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        return isInternalExecution() ? true : failValidation(EngineMessage.USER_CANNOT_RUN_ACTION_INTERNAL_COMMAND);
    }

    @Override
    protected void executeCommand() {
        try {
            DbUser dbUser = getDbUser();
            logInfo = String.format("for user %s", dbUser.getLoginName());
            getReturnValue().setActionReturnValue(createSession(dbUser, dbUser.getDomain(), loginOnBehalf(dbUser)));
            setSucceeded(true);
        } catch (Exception ex) {
            log.error("Unable to create engine session: {}", ex.getMessage());
            log.debug("Unable to create engine session", ex);
        }
    }

    private DbUser getDbUser() {
        DbUser dbUser = null;
        switch (getParameters().getQueryType()) {
            case ByInternalId:
                logInfo = String.format("for internal id %s", getParameters().getUserId());
                dbUser = dbUserDao.get(getParameters().getUserId());
                break;
            case ByPrincipalName:
                logInfo = String.format("for principal name: %s, authz name: %s",
                        getParameters().getPrincipalName(), getParameters().getAuthzName());
                dbUser = getDbUserForPrincipalName(getParameters().getPrincipalName(), getParameters().getAuthzName());
                break;
            case ByExternalId:
                logInfo = String.format("for external id: %s, authz name: %s",
                        getParameters().getExternalId(), getParameters().getAuthzName());
                dbUser = dbUserDao.getByExternalId(getParameters().getAuthzName(), getParameters().getExternalId());
                break;
        }
        if (dbUser == null) {
            throw new EngineException(EngineError.PRINCIPAL_NOT_FOUND, "User not found in database");
        }
        return dbUser;
    }

    private DbUser getDbUserForPrincipalName(String principalName, String authzName) {
        Map<String, Object> response = SsoOAuthServiceUtils.fetchPrincipalRecord(
                getSessionDataContainer().getSsoAccessToken(getParameters().getSessionId()),
                authzName,
                principalName,
                false,
                false
        );
        ExtMap principalRecord = null;
        if (response.containsKey("result")) {
            Collection<ExtMap> records = (Collection<ExtMap>) response.get("result");
            if (!records.isEmpty()) {
                principalRecord = records.iterator().next();
            }
        }
        if (principalRecord == null) {
            throw new EngineException(EngineError.PRINCIPAL_NOT_FOUND,
                    String.format("%s in domain '%s", principalName, authzName));
        }
        DbUser user =  new DbUser(directoryUtils.mapPrincipalRecordToDirectoryUser(authzName, principalRecord));
        user.setId(Guid.newGuid());
        return user;
    }

    private ExtMap loginOnBehalf(DbUser dbUser) {
        Map<String, Object> response = SsoOAuthServiceUtils.findLoginOnBehalfPrincipalById(
                dbUser.getDomain(),
                dbUser.getNamespace(),
                Arrays.asList(dbUser.getExternalId()),
                true,
                true);

        Collection<ExtMap> principalRecords = Collections.emptyList();
        if (response.containsKey("result")) {
            principalRecords = (Collection<ExtMap>) response.get("result");
        }
        if (principalRecords.isEmpty()) {
            throw new EngineException(EngineError.PRINCIPAL_NOT_FOUND,
                    String.format(" user %s in domain '%s", dbUser.getLoginName(), dbUser.getDomain()));
        }
        return principalRecords.iterator().next();
    }

    private String createSession(DbUser mappedUser, String authzName, ExtMap principalRecord) {
        directoryUtils.flatGroups(principalRecord);
        DbUser dbUser = directoryUtils.mapPrincipalRecordToDbUser(authzName, principalRecord);
        dbUser.setId(mappedUser.getId());
        String engineSessionId;
        byte[] s = new byte[64];
        new SecureRandom().nextBytes(s);
        engineSessionId = new Base64(0).encodeToString(s);
        sessionDataContainer.setUser(engineSessionId, dbUser);
        sessionDataContainer.refresh(engineSessionId);
        return engineSessionId;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("LoginOnBehalfLogInfo", logInfo);
        return getSucceeded() ? AuditLogType.USER_LOGIN_ON_BEHALF : AuditLogType.USER_LOGIN_ON_BEHALF_FAILED;
    }
}
