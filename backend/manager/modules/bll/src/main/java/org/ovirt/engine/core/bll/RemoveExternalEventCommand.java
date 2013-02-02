package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.action.RemoveExternalEventParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveExternalEventCommand <T extends RemoveExternalEventParameters> extends ExternalEventCommandBase<T> {

    private static final long serialVersionUID = 1L;
    private final String OVIRT="oVirt";

    public RemoveExternalEventCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        // check if such event exists
        AuditLog event = DbFacade.getInstance().getAuditLogDao().get(getParameters().getId());
        if (event == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_EVENT_NOT_FOUND);
        }
        if (OVIRT.equalsIgnoreCase(event.getOrigin())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_EVENT_ILLEGAL_ORIGIN);
        }
        if (event.getseverity() == AuditLogSeverity.ALERT) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_EVENT_ILLRGAL_OPERATION);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__EXTERNAL_EVENT);
    }

    @Override
    protected void executeCommand() {
       DbFacade.getInstance().getAuditLogDao().remove(getParameters().getId());
       setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        AuditLog event = DbFacade.getInstance().getAuditLogDao().get(getParameters().getId());
        return  getPermissionList(event);
    }
}
