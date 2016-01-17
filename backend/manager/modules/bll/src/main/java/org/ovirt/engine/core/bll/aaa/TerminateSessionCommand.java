package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.TerminateSessionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

/**
 * Terminates session of the user identified by its session database id. Only admins can execute this command.
 */
public class TerminateSessionCommand<T extends TerminateSessionParameters> extends CommandBase<T> {

    @Named
    @Inject
    private Predicate<Guid> isSystemSuperUserPredicate;

    @Inject
    private SessionDataContainer sessionDataContainer;

    public TerminateSessionCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        String terminatedSessionId = sessionDataContainer.getSessionIdBySeqId(getParameters().getTerminatedSessionDbId());

        if (terminatedSessionId == null) {
            log.info(
                    "Cannot terminate session with database id '{}', it doesn't exist anymore.",
                    getParameters().getTerminatedSessionDbId());
            setSucceeded(false);
            return;
        }

        // store terminated user username for audit log
        DbUser terminatedUser = sessionDataContainer.getUser(terminatedSessionId, false);
        if (terminatedUser != null) {
            addCustomValue(
                    "TerminatedSessionUsername",
                    String.format("%s@%s", terminatedUser.getLoginName(), terminatedUser.getDomain()));
        }

        setReturnValue(
                getBackend().logoff(
                        new VdcActionParametersBase(terminatedSessionId)));
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        if (isSystemSuperUserPredicate.test(getCurrentUser().getId())) {
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
        return getSucceeded()
                ? AuditLogType.USER_VDC_SESSION_TERMINATED
                : AuditLogType.USER_VDC_SESSION_TERMINATION_FAILED;
    }
}
