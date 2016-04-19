package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.TerminateSessionsForTokenParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

/**
 * Terminates session of the user identified by its session database id. Only admins can execute this command.
 */
public class TerminateSessionsForTokenCommand<T extends TerminateSessionsForTokenParameters> extends CommandBase<T> {

    @Inject
    private SessionDataContainer sessionDataContainer;

    public TerminateSessionsForTokenCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        DbUser terminatedUser = sessionDataContainer.getUser(
                sessionDataContainer.getSessionIdBySsoAccessToken(getParameters().getSsoAccessToken()),
                false);
        if (terminatedUser != null) {
            log.debug("Terminating session for user {}@{}", terminatedUser.getLoginName(), terminatedUser.getDomain());
        }
        sessionDataContainer.cleanupEngineSessionsForSsoAccessToken(getParameters().getSsoAccessToken());
        setSucceeded(true);
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.UNASSIGNED;
    }
}
