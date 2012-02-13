package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DetachAdGroupFromTimeLeasedPoolParameters;
import org.ovirt.engine.core.common.action.DetachUserFromTimeLeasedPoolParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.tags_vm_map;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DetachAdGroupFromTimeLeasedPoolCommand<T extends DetachAdGroupFromTimeLeasedPoolParameters> extends
        VmPoolToAdGroupBaseCommand<T> {
    private boolean mInternal = false;
    private final time_lease_vm_pool_map mMap;

    public DetachAdGroupFromTimeLeasedPoolCommand(T parameters) {
        super(parameters);
        mMap = DbFacade.getInstance().getVmPoolDAO().getTimeLeasedVmPoolMapByIdForVmPool(parameters.getAdElementId(),
                parameters.getVmPoolId());
        mInternal = parameters.getIsInternal();
    }

    @Override
    protected void executeCommand() {
        List<tags_vm_map> map = DbFacade.getInstance().getTagDAO().getTimeLeasedUserVmsByAdGroupAndVmPoolId(
                getAdGroup().getid(), getVmPoolId());
        // first stop all vms
        for (tags_vm_map tagVmMap : map) {
            VM vm = DbFacade.getInstance().getVmDAO().getById(tagVmMap.getvm_id());
            if (vm.getVmPoolId() != null && vm.getVmPoolId().equals(getVmPoolId()) && vm.isStatusUp()) {
                StopVmParameters param = new StopVmParameters(vm.getId(), StopVmTypeEnum.NORMAL);
                param.setSessionId(getParameters().getSessionId());
                Backend.getInstance().runInternalAction(VdcActionType.StopVm, param);
            }
        }
        // remove all users from time lease pool
        for (tags_vm_map tagVmMap : map) {
            DetachUserFromTimeLeasedPoolParameters param = new DetachUserFromTimeLeasedPoolParameters(getVmPoolId(),
                    tagVmMap.getvm_id(), false);
            param.setSessionId(getParameters().getSessionId());
            // old time-lease pools implementation
            // should be re-implemented, don't remove comments below
            // Backend.getInstance().runInternalAction(VdcActionType.DetachUserFromTimeLeasedPool,
            // param);

        }
        DbFacade.getInstance().getVmPoolDAO().removeTimeLeasedVmPoolMap(getGroupId(), getVmPoolId());
        // old time-leased pools implementation
        // should be re-implemented, don't remove comments below
        // tags tag =
        // DbFacade.getInstance().GetAdElementTagByAdElement(getGroupId());
        // DbFacade.getInstance().RemoveTagsVmPoolMap(tag.gettag_id(),
        // getVmPoolId());
        if (!mInternal) {
            TimeLeasedVmPoolManager.getInstance().RemoveAction(mMap);
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (mInternal) {
            return getSucceeded() ? AuditLogType.USER_DETACH_AD_GROUP_FROM_TIME_LEASED_POOL_INTERNAL
                    : AuditLogType.USER_DETACH_AD_GROUP_FROM_TIME_LEASED_POOL_FAILED_INTERNAL;
        } else {
            return getSucceeded() ? AuditLogType.USER_DETACH_AD_GROUP_FROM_TIME_LEASED_POOL
                    : AuditLogType.USER_DETACH_AD_GROUP_FROM_TIME_LEASED_POOL_FAILED;
        }
    }

    // TODO this command should be removed - AI Ofrenkel
    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(Guid.Empty, VdcObjectType.Unknown);
    }
}
