package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;

/**
 * Tries to log out a user identified by its session id
 */
public class LogoutSessionCommand<T extends VdcActionParametersBase> extends CommandBase<T> {

    @Inject
    private SessionDataContainer sessionDataContainer;

    public LogoutSessionCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDC_LOGOUT : AuditLogType.USER_VDC_LOGOUT_FAILED;
    }

    @Override
    protected boolean validate() {
        return sessionDataContainer.isSessionExists(getParameters().getSessionId());
    }

    @Override
    protected void executeCommand() {
        AuthenticationProfile profile = sessionDataContainer.getProfile(getParameters().getSessionId());
        if (profile == null) {
            setSucceeded(false);
        } else {
            sessionDataContainer.setSessionValid(getParameters().getSessionId(), false);
            setSucceeded(true);
        }
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }

}
