package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

public class RemoveAuditLogByIdCommand<T extends RemoveAuditLogByIdParameters> extends ExternalEventCommandBase<T> {

    private static final String OVIRT="oVirt";

    @Inject
    private AuditLogDirector auditLogDirector;

    public RemoveAuditLogByIdCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        AuditLog event = getAuditLogDao().get(getParameters().getAuditLogId());

        if (event == null) {
            return failValidation(EngineMessage.AUDIT_LOG_CANNOT_REMOVE_AUDIT_LOG_NOT_EXIST);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        AuditLog auditLog = getAuditLogDao().get(getParameters().getAuditLogId());
        getAuditLogDao().remove(getParameters().getAuditLogId());
        setAuditLogDetails(auditLog);
        // clean cache manager entry (if exists)
        evict(auditLogDirector.composeSystemObjectId(this, auditLog.getLogType()));
        setSucceeded(true);
    }

    private void setAuditLogDetails(AuditLog auditLog) {
        this.setStorageDomainId(auditLog.getStorageDomainId());
        this.setStoragePoolId(auditLog.getStoragePoolId());
        this.setClusterId(auditLog.getClusterId());
        this.setVdsId(auditLog.getVdsId());
        this.setVmId(auditLog.getVmId());
        this.setVmTemplateId(auditLog.getVmTemplateId());
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
