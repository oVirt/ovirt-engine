package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class LogoutUserCommand<T extends LogoutUserParameters> extends CommandBase<T> {
    public LogoutUserCommand(T parameters) {
        super(parameters);
        if (getCurrentUser() == null) {
            DbUser dbUser = DbFacade.getInstance().getDbUserDAO().get(parameters.getUserId());
            setCurrentUser(new VdcUser(dbUser.getuser_id(), dbUser.getusername(), dbUser.getdomain()));
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDC_LOGOUT : AuditLogType.USER_VDC_LOGOUT_FAILED;
    }

    @Override
    protected void executeCommand() {
        Guid userId = (getParameters()).getUserId();
        String httpSessionId = getParameters().getHttpSessionId();
        if (httpSessionId != null) {
            DbFacade.getInstance().getDbUserDAO().removeUserSession(httpSessionId, userId);
            SessionDataContainer.getInstance().removeSession(httpSessionId);
        } else if (!StringHelper.EqOp(getParameters().getSessionId(), "")) {
            SessionDataContainer.getInstance().removeSession(getParameters().getSessionId());
        } else {
            SessionDataContainer.getInstance().removeSession();
        }
        setSucceeded(true);
    }

    @Override
    protected boolean IsUserAutorizedToRunAction() {
        return true;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyMap();
    }

}
