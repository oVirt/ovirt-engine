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
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.EventFloodRegulator;
import org.ovirt.engine.core.dao.AuditLogDao;


public class RemoveAuditLogByIdCommand<T extends RemoveAuditLogByIdParameters> extends ExternalEventCommandBase<T> {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private AuditLogDao auditLogDao;
    private AuditLog auditLog;

    public RemoveAuditLogByIdCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getAuditLog() == null) {
            return failValidation(EngineMessage.AUDIT_LOG_CANNOT_REMOVE_AUDIT_LOG_NOT_EXIST);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        AuditLog auditLog = getAuditLog();
        auditLogDao.remove(getParameters().getAuditLogId());
        setAuditLogDetails(auditLog);

        // clear the id so the auditLog will be considered as a system-level auditLog
        auditLog.setUserId(Guid.Empty);
        AuditLogable logableToClear = createAuditLogableImpl(auditLog);

        // clean cache manager entry (if exists)
        EventFloodRegulator eventFloodRegulator = new EventFloodRegulator(logableToClear, auditLog.getLogType());
        eventFloodRegulator.evict();
        setSucceeded(true);
    }

    private AuditLog getAuditLog() {
        if (auditLog == null) {
            auditLog = auditLogDao.get(getParameters().getAuditLogId());
        }

        return auditLog;
    }

    private void setAuditLogDetails(AuditLog auditLog) {
        this.setStorageDomainId(auditLog.getStorageDomainId());
        this.setStoragePoolId(auditLog.getStoragePoolId());
        this.setClusterId(auditLog.getClusterId());
        this.setVdsId(auditLog.getVdsId());
        this.setVmId(auditLog.getVmId());
        this.setVmTemplateId(auditLog.getVmTemplateId());
    }


    private AuditLogable createAuditLogableImpl(AuditLog event) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setStorageDomainId(event.getStorageDomainId());
        logable.setStoragePoolId(event.getStoragePoolId());
        logable.setUserId(event.getUserId());
        logable.setClusterId(event.getClusterId());
        logable.setVdsId(event.getVdsId());
        logable.setVmId(event.getVmId());
        logable.setVmTemplateId(event.getVmTemplateId());
        logable.setCustomId(event.getCustomId());

        return logable;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        AuditLog event = getAuditLog();
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
