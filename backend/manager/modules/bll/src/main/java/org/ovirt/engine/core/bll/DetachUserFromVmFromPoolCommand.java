package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.action.VmPoolSimpleUserParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DetachUserFromVmFromPoolCommand<T extends VmPoolSimpleUserParameters> extends
        VmPoolSimpleUserCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     * @param commandId
     */
    protected DetachUserFromVmFromPoolCommand(Guid commandId) {
        super(commandId);
    }

    public DetachUserFromVmFromPoolCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());

    }

    protected boolean IsUserAttachedToPool() {
        // Check first if user attached to pool directly
        boolean attached = getVmPoolId() != null
                && DbFacade.getInstance().getEntityPermissions(getAdUserId(), ActionGroup.VM_POOL_BASIC_OPERATIONS,
                        getVmPoolId().getValue(), VdcObjectType.VmPool) != null;
        return attached;
    }

    protected void DetachAllVmsFromUser() {
        List<VM> vms = DbFacade.getInstance().getVmDAO().getAllForUser(getAdUserId());
        for (VM vm : vms) {
            if (getVmPoolId() != null && getVmPoolId().equals(vm.getVmPoolId())) {
                permissions perm = DbFacade
                        .getInstance()
                        .getPermissionDAO()
                        .getForRoleAndAdElementAndObject(
                                PredefinedRoles.ENGINE_USER.getId(),
                                getAdUserId(), vm.getId());
                if (perm != null) {
                    DbFacade.getInstance().getPermissionDAO().remove(perm.getId());
                    RestoreVmFromBaseSnapshot(vm);
                }
            }
        }

    }

    private void RestoreVmFromBaseSnapshot(VM vm) {
        if (DbFacade.getInstance().getSnapshotDao().exists(getVm().getId(), SnapshotType.STATELESS)) {
            log.infoFormat("Deleting snapshots for stateless vm {0}", vm.getId());
            Backend.getInstance().runInternalAction(VdcActionType.RestoreStatelessVm,
                    new VmOperationParameterBase(vm.getId()),
                    new CommandContext(getCompensationContext()));
        }
    }

    @Override
    protected void executeCommand() {
        if (IsUserAttachedToPool()) {
            DetachAllVmsFromUser();
        }
        setSucceeded(true);
    }
}
