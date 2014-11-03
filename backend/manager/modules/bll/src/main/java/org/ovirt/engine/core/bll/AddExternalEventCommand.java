package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddExternalEventParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class AddExternalEventCommand<T extends AddExternalEventParameters> extends ExternalEventCommandBase<T> {
    private static final String OVIRT="oVirt";

    public AddExternalEventCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean result=true;
        if (getParameters().getEvent() == null || getParameters().getEvent().getOrigin().equalsIgnoreCase(OVIRT)){
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_EVENT_ILLEGAL_ORIGIN);
            result = false;
        }
        if (!result) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__EXTERNAL_EVENT);
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
                AuditLogDirector.log(event, AuditLogType.EXTERNAL_EVENT_NORMAL, message);
                break;
            case WARNING:
                AuditLogDirector.log(event, AuditLogType.EXTERNAL_EVENT_WARNING, message);
                break;
            case ERROR:
                AuditLogDirector.log(event, AuditLogType.EXTERNAL_EVENT_ERROR, message);
                break;
            case ALERT:
                AlertDirector.Alert(event, AuditLogType.EXTERNAL_ALERT, message);
                break;
        }
        AuditLog auditLog = DbFacade.getInstance().getAuditLogDao().getByOriginAndCustomEventId(getParameters().getEvent().getOrigin(), getParameters().getEvent().getCustomEventId());
        if (auditLog != null) {
            setActionReturnValue(auditLog.getAuditLogId());
            setSucceeded(true);
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return getPermissionList(getParameters().getEvent());
    }
}
