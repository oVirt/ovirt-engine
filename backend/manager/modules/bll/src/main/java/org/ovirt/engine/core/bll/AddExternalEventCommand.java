package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddExternalEventParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.AuditLogDao;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.di.Injector;

public class AddExternalEventCommand<T extends AddExternalEventParameters> extends ExternalEventCommandBase<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private AuditLogDao auditLogDao;
    @Inject
    private VdsDynamicDao vdsDynamicDao;
    @Inject
    private StorageDomainDynamicDao storageDomainDynamicDao;

    public AddExternalEventCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getEvent() == null
                || AuditLog.OVIRT_ORIGIN.equalsIgnoreCase(getEvent().getOrigin())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_EVENT_ILLEGAL_ORIGIN);
        }
        AuditLog auditLog =
                auditLogDao.getByOriginAndCustomEventId(getEvent().getOrigin(), getEvent().getCustomEventId());
        if (auditLog != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_EVENT_DUPLICATE_CUSTOM_ID);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__EXTERNAL_EVENT);
    }

    @Override
    protected void executeCommand() {
        AuditLogableBase event = Injector.injectMembers(new AuditLogableBase(getParameters().getEvent()));
        event.setExternal(true);
        String message =
                StringUtils.abbreviate(getEvent().getMessage(), Config.getValue(ConfigValues.MaxAuditLogMessageLength));

        switch (getEvent().getSeverity()){
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
                auditLogDirector.log(event, AuditLogType.EXTERNAL_ALERT, message);
                break;
        }

        AuditLog auditLog =
                auditLogDao.getByOriginAndCustomEventId(getEvent().getOrigin(), getEvent().getCustomEventId());
        if (auditLog != null) {
            setActionReturnValue(auditLog.getAuditLogId());
            setSucceeded(true);
        }

        // Update host external status if set
        if (hasHostExternalStatus()) {
            vdsDynamicDao.updateExternalStatus(getEvent().getVdsId(), getParameters().getExternalStatus());
        }
        // update storage domain external status if set
        if (hasStorageDomainExternalStatus()) {
            storageDomainDynamicDao.updateExternalStatus(getEvent().getStorageDomainId(),
                    getParameters().getExternalStatus());
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = getPermissionList(getEvent());
        // check for external host status modification
        if (hasHostExternalStatus()) {
            permissionList.add(new PermissionSubject(getEvent().getVdsId(),
                    VdcObjectType.VDS, ActionGroup.EDIT_HOST_CONFIGURATION));
        }
        // check for external storage domain status modification
        if (hasStorageDomainExternalStatus()) {
            permissionList.add(new PermissionSubject(getEvent().getStorageDomainId(),
                    VdcObjectType.Storage, ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION));
        }
        return permissionList;
    }

    private boolean hasHostExternalStatus() {
        return getEvent().getVdsId() != null && getParameters().getExternalStatus() != null;
    }

    private boolean hasStorageDomainExternalStatus() {
        return getEvent().getStorageDomainId() != null && getParameters().getExternalStatus() != null;
    }

    private AuditLog getEvent() {
        return getParameters().getEvent();
    }
}
