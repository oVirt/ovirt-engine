package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachAdGroupTimeLeasedPoolCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AttachAdGroupTimeLeasedPoolCommand<T extends AttachAdGroupTimeLeasedPoolCommandParameters> extends
        VmPoolToAdGroupBaseCommand<T> {
    public AttachAdGroupTimeLeasedPoolCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        AdGroupsHandlingOperationBase.AddAdGroupToDBWithPermissionIfNeeded(getParameters().getAdGroup(),
                getParameters().getSessionId(), getCompensationContext());

        DbFacade.getInstance().getVmPoolDAO().addTimeLeasedVmPoolMap(getParameters().getTimeLeasedVmPoolMap());
        TimeLeasedVmPoolManager.getInstance().AddAction(getParameters().getTimeLeasedVmPoolMap());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ATTACH_AD_GROUP_TO_TIME_LEASED_POOL
                : AuditLogType.USER_ATTACH_AD_GROUP_TO_TIME_LEASED_POOL_FAILED;
    }

    // TODO this command should be removed - AI Ofrenkel
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.Empty, VdcObjectType.Unknown,
                getActionType().getActionGroup()));
    }
}
