package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class LogoutUserCommand<T extends LogoutUserParameters> extends CommandBase<T> {
    public LogoutUserCommand(T parameters) {
        super(parameters);
        if (getCurrentUser() == null) {
            DbUser dbUser = DbFacade.getInstance().getDbUserDao().get(parameters.getUserId());
            setCurrentUser(new VdcUser(dbUser.getuser_id(), dbUser.getusername(), dbUser.getdomain()));
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDC_LOGOUT : AuditLogType.USER_VDC_LOGOUT_FAILED;
    }

    @Override
    protected void executeCommand() {
        if (!"".equals(getParameters().getSessionId())) {
            SessionDataContainer.getInstance().removeSession(getParameters().getSessionId());
        } else {
            SessionDataContainer.getInstance().removeSession();
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
