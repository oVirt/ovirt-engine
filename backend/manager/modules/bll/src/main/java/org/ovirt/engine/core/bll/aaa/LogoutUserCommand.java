package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

public class LogoutUserCommand<T extends LogoutUserParameters> extends CommandBase<T> {
    public LogoutUserCommand(T parameters) {
        super(parameters);
        if (getCurrentUser() == null) {
            DbUser dbUser = DbFacade.getInstance().getDbUserDao().get(parameters.getUserId());
            setCurrentUser(dbUser);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDC_LOGOUT : AuditLogType.USER_VDC_LOGOUT_FAILED;
    }

    @Override
    protected void executeCommand() {
        ExtensionProxy authn = SessionDataContainer.getInstance().getAuthn(getParameters().getSessionId());

        if (authn != null) {
            if ((authn.getContext().<Long> get(Authn.ContextKeys.CAPABILITIES) & Authn.Capabilities.LOGOUT) != 0) {
                authn.invoke(new ExtMap().mput(
                        Base.InvokeKeys.COMMAND,
                        Authn.InvokeCommands.LOGOUT
                        ).mput(
                                Authn.InvokeKeys.PRINCIPAL,
                                SessionDataContainer.getInstance().getPrincipal(getParameters().getSessionId())
                        ));
            }
            SessionDataContainer.getInstance().removeSessionOnLogout(getParameters().getSessionId());
        }
        setSucceeded(true);
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
