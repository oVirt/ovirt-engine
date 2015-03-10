package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
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
    public LogoutSessionCommand(T parameters) {
        this(parameters, null);
    }

    public LogoutSessionCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDC_LOGOUT : AuditLogType.USER_VDC_LOGOUT_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        return SessionDataContainer.getInstance().isSessionExists(getParameters().getSessionId());
    }

    @Override
    protected void executeCommand() {
        AuthenticationProfile profile = SessionDataContainer.getInstance().getProfile(getParameters().getSessionId());
        if (profile == null) {
            setSucceeded(false);
        } else {
            if ((profile.getAuthn().getContext().<Long> get(Authn.ContextKeys.CAPABILITIES) & Authn.Capabilities.LOGOUT) != 0) {
                profile.getAuthn().invoke(new ExtMap().mput(
                        Base.InvokeKeys.COMMAND,
                        Authn.InvokeCommands.LOGOUT
                        ).mput(
                                Authn.InvokeKeys.PRINCIPAL,
                                SessionDataContainer.getInstance().getPrincipalName(getParameters().getSessionId())
                        ));
            }
            SessionDataContainer.getInstance().removeSessionOnLogout(getParameters().getSessionId());
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
