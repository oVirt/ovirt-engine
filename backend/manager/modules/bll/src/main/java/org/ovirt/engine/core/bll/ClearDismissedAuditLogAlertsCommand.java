package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

import java.util.Collections;
import java.util.List;

public class ClearDismissedAuditLogAlertsCommand<T extends VdcActionParametersBase> extends CommandBase<T> {

    public ClearDismissedAuditLogAlertsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        return true;
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getAuditLogDao().clearDismissedAlerts();
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.AUDIT_LOG_MANAGEMENT));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_CLEAR_ALL_DISMISSED_AUDIT_LOG : AuditLogType.USER_CLEAR_ALL_DISMISSED_AUDIT_LOG_FAILED;
    }
}
