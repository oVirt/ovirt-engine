package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DetachUserFromTimeLeasedPoolParameters;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DetachUserFromTimeLeasedPoolCommand<T extends DetachUserFromTimeLeasedPoolParameters> extends
        VmPoolSimpleUserCommandBase<T> {

    public DetachUserFromTimeLeasedPoolCommand(T parameters) {
        super(parameters);
    }

    /**
     * 1. Check if there is time leased pool and vm attached to it. 2. For all
     * vms attached to user: If vm running - stop it. Detach user from this vm.
     * 3. Detach user from vm
     */
    @Override
    protected void executeCommand() {
        time_lease_vm_pool_map map = DbFacade.getInstance().getVmPoolDAO().getTimeLeasedVmPoolMapByIdForVmPool(getAdUserId(),
                getVmPoolId());
        if (map != null) {
            // java.util.ArrayList<tags_vm_map> tagsVmMap =
            // DbFacade.getInstance().GetTagVmMapByAdElementId(getAdUserId());
            //
            // if (tagsVmMap.size() != 0) {
            // for (tags_vm_map tagVm : tagsVmMap) {
            //
            // VM vm =
            // DbFacade.getInstance().GetvmsBy_vm_guid(tagVm.getvm_id());
            // if (getVmPoolId().equals(vm.getVmPoolId())) {
            // if (vm.isStatusUp()) {
            // StopVmParameters tempVar = new StopVmParameters(tagVm.getvm_id(),
            // StopVmTypeEnum.NORMAL);
            // tempVar.setShouldBeLogged(false);
            // Backend.getInstance().runInternalAction(VdcActionType.StopVm,
            // tempVar);
            // }
            //
            // VmPoolSimpleUserParameters tempVar2 = new
            // VmPoolSimpleUserParameters(map.getvm_pool_id(),
            // getAdUserId());
            // tempVar2.setShouldBeLogged(false);
            // Backend.getInstance().runInternalAction(VdcActionType.DetachUserFromVmFromPool,
            // tempVar2);
            // }
            // }
            // }

            // old time-leased pool implementation
            // should be re-implemented, don't remove comments below

            // VmPoolSimpleUserParameters tempVar3 = new
            // VmPoolSimpleUserParameters(map.getvm_pool_id(), map.getid());
            // tempVar3.setShouldBeLogged(false);
            // Backend.getInstance().runInternalAction(VdcActionType.DetachVmPoolFromUser,
            // tempVar3);

            DbFacade.getInstance().getVmPoolDAO().removeTimeLeasedVmPoolMap(map.getid(), map.getvm_pool_id());
            if (!getParameters().getIsInternal()) {
                TimeLeasedVmPoolManager.getInstance().RemoveAction(map);
            }
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        AuditLogType auditLogType;
        if (getParameters().getIsInternal()) {
            auditLogType = (getSucceeded()) ? AuditLogType.USER_DETACH_USER_FROM_TIME_LEASED_POOL_INTERNAL
                    : AuditLogType.USER_DETACH_USER_FROM_TIME_LEASED_POOL_FAILED_INTERNAL;
        } else {
            auditLogType = (getSucceeded()) ? AuditLogType.USER_DETACH_USER_FROM_TIME_LEASED_POOL
                    : AuditLogType.USER_DETACH_USER_FROM_TIME_LEASED_POOL_FAILED;
        }
        return auditLogType;
    }
}
