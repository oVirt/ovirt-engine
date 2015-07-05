package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

import java.util.Collections;
import java.util.List;

public class RemoveAuditLogByIdCommand<T extends RemoveAuditLogByIdParameters> extends ExternalEventCommandBase<T> {

    private static final String OVIRT="oVirt";

    public RemoveAuditLogByIdCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        AuditLog event = getAuditLogDao().get(getParameters().getAuditLogId());

        if (event == null) {
            return failCanDoAction(EngineMessage.AUDIT_LOG_CANNOT_REMOVE_AUDIT_LOG_NOT_EXIST);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getAuditLogDao()
                .remove(getParameters().getAuditLogId());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        AuditLog event = getAuditLogDao().get(getParameters().getAuditLogId());
        if (AuditLogSeverity.ALERT.equals(event.getSeverity())) {
            return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                    VdcObjectType.System,
                    ActionGroup.AUDIT_LOG_MANAGEMENT));
        }

        return getPermissionList(event);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("AuditLogId", String.valueOf(getParameters().getAuditLogId()));
        return getSucceeded() ? AuditLogType.UNASSIGNED : AuditLogType.USER_REMOVE_AUDIT_LOG_FAILED;
    }
}
