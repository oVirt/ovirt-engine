package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddExternalEventParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;

public class AddExternalEventCommand<T extends AddExternalEventParameters> extends ExternalEventCommandBase<T> {
    private static final String OVIRT="oVirt";

    @Inject VdsDynamicDao hostDao;

    @Inject StorageDomainDynamicDao storageDomainDynamicDao;

    public AddExternalEventCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        boolean result=true;
        if (getParameters().getEvent() == null || getParameters().getEvent().getOrigin().equalsIgnoreCase(OVIRT)){
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_EVENT_ILLEGAL_ORIGIN);
            result = false;
        }
        if (!result) {
            addValidationMessage(EngineMessage.VAR__ACTION__ADD);
            addValidationMessage(EngineMessage.VAR__TYPE__EXTERNAL_EVENT);
        }
        return result;
    }
    @Override
    protected void executeCommand() {
        AuditLogableBase event = new AuditLogableBase(getParameters().getEvent());
        event.setExternal(true);
        String message = getParameters().getEvent().getMessage();
        switch (getParameters().getEvent().getSeverity()){
            case NORMAL:
                auditLogDirector.log(event, AuditLogType.EXTERNAL_EVENT_NORMAL, message);
                break;
            case WARNING:
                auditLogDirector.log(event, AuditLogType.EXTERNAL_EVENT_WARNING, message);
                break;
            case ERROR:
                auditLogDirector.log(event, AuditLogType.EXTERNAL_EVENT_ERROR, message);
                break;
            case ALERT:
                AlertDirector.alert(event, AuditLogType.EXTERNAL_ALERT, auditLogDirector, message);
                break;
        }
        AuditLog auditLog = DbFacade.getInstance().getAuditLogDao().getByOriginAndCustomEventId(getParameters().getEvent().getOrigin(), getParameters().getEvent().getCustomEventId());
        if (auditLog != null) {
            setActionReturnValue(auditLog.getAuditLogId());
            setSucceeded(true);
        }
        // Update host external status if set
        if (hasHostExternalStatus()) {
            hostDao.updateExternalStatus(getParameters().getEvent().getVdsId(), getParameters().getExternalStatus());
        }
        // update storage domain external status if set
        if (hasStorageDomainExternalStatus()) {
            storageDomainDynamicDao.updateExternalStatus(getParameters().getEvent().getStorageDomainId(),
                    getParameters().getExternalStatus());
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = getPermissionList(getParameters().getEvent());
        // check for external host status modification
        if (hasHostExternalStatus()) {
            permissionList.add(new PermissionSubject(getParameters().getEvent().getVdsId(),
                    VdcObjectType.VDS, ActionGroup.EDIT_HOST_CONFIGURATION));
        }
        // check for external storage domain status modification
        if (hasStorageDomainExternalStatus()) {
            permissionList.add(new PermissionSubject(getParameters().getEvent().getStorageDomainId(),
                    VdcObjectType.Storage, ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION));
        }
        return permissionList;
    }

    private boolean hasHostExternalStatus() {
        return getParameters().getEvent().getVdsId() != null && getParameters().getExternalStatus() != null;
    }

    private boolean hasStorageDomainExternalStatus() {
        return getParameters().getEvent().getStorageDomainId() != null &&
                getParameters().getExternalStatus() != null;
    }
}
