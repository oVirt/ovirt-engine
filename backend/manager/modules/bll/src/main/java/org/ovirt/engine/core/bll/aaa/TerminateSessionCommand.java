package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.TerminateSessionParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * Terminates session of the user identified by its session database id. Only admins can execute this command.
 */
public class TerminateSessionCommand<T extends TerminateSessionParameters> extends CommandBase<T> {

    @Named
    @Inject
    private Predicate<DbUser> isSystemSuperUserPredicate;

    @Inject
    private SessionDataContainer sessionDataContainer;

    private static final String UNKNOWN = "UNKNOWN";

    private String sessionId;
    private String sourceIp;

    public TerminateSessionCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        sessionId = sessionDataContainer.getSessionIdBySeqId(getParameters().getTerminatedSessionDbId());

        if (sessionId == null) {
            log.info(
                    "Cannot terminate session with database id '{}', it doesn't exist anymore.",
                    getParameters().getTerminatedSessionDbId());
            setSucceeded(false);
            return;
        }

        sourceIp = sessionDataContainer.getSourceIp(sessionId);

        // store terminated user username for audit log
        DbUser terminatedUser = sessionDataContainer.getUser(sessionId, false);
        if (terminatedUser != null) {
            addCustomValue(
                    "TerminatedSessionUsername",
                    String.format("%s@%s", terminatedUser.getLoginName(), terminatedUser.getDomain()));
        }

        setReturnValue(
                backend.logoff(
                        new ActionParametersBase(sessionId)));
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        if (isSystemSuperUserPredicate.test(getCurrentUser())) {
            return true;
        } else {
            addValidationMessage(EngineMessage.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
            return false;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("SessionID", StringUtils.isEmpty(sessionId) ? UNKNOWN : sessionId);
        addCustomValue("SourceIP", StringUtils.isEmpty(sourceIp) ? UNKNOWN : sourceIp);

        return getSucceeded()
                ? AuditLogType.USER_VDC_SESSION_TERMINATED
                : AuditLogType.USER_VDC_SESSION_TERMINATION_FAILED;
    }
}
