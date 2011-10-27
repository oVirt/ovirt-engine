package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ADElementTimeLeasedVmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class UpdateAdGroupTimeLeasedPoolCommand<T extends ADElementTimeLeasedVmPoolParametersBase> extends
        VmPoolToAdGroupBaseCommand<T> {
    private time_lease_vm_pool_map mMap;

    public UpdateAdGroupTimeLeasedPoolCommand(T parameters) {
        super(parameters);
        mMap = parameters.getTimeLeasedVmPoolMap();
        mMap.oldMap = DbFacade.getInstance().getVmPoolDAO().getTimeLeasedVmPoolMapByIdForVmPool(mMap.getid(), mMap.getvm_pool_id());
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getVmPoolDAO().updateTimeLeasedVmPoolMap(mMap);
        TimeLeasedVmPoolManager.getInstance().UpdateAction(mMap);
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_AD_GROUP_TO_TIME_LEASED_POOL
                : AuditLogType.USER_UPDATE_AD_GROUP_TO_TIME_LEASED_POOL_FAILED;
    }
}
