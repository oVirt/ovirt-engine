package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DisplayAllAuditLogEventsCommand<T extends VdcActionParametersBase> extends CommandBase<T> {

    public DisplayAllAuditLogEventsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        return true;
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getAuditLogDao().displayAllEvents();
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
        return getSucceeded() ? AuditLogType.USER_DISPLAY_ALL_AUDIT_LOG : AuditLogType.USER_DISPLAY_ALL_AUDIT_LOG_FAILED;
    }
}
