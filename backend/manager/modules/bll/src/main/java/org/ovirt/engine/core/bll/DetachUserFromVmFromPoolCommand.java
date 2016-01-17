package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DetachUserFromVmFromPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@DisableInPrepareMode
public class DetachUserFromVmFromPoolCommand<T extends DetachUserFromVmFromPoolParameters> extends
        VmPoolSimpleUserCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected DetachUserFromVmFromPoolCommand(Guid commandId) {
        super(commandId);
    }

    public DetachUserFromVmFromPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));

    }

    protected void detachVmFromUser() {
        Permission perm = DbFacade
                .getInstance()
                .getPermissionDao()
                .getForRoleAndAdElementAndObject(
                        PredefinedRoles.ENGINE_USER.getId(),
                        getAdUserId(), getParameters().getVmId());
        if (perm != null) {
            DbFacade.getInstance().getPermissionDao().remove(perm.getId());
            if (getParameters().getIsRestoreStateless()) {
                VM vm = DbFacade.getInstance().getVmDao().get(getParameters().getVmId());
                if (vm != null) {
                    restoreVmFromBaseSnapshot(vm);
                }
            }
        }
    }

    private void restoreVmFromBaseSnapshot(VM vm) {
        if (DbFacade.getInstance().getSnapshotDao().exists(vm.getId(), SnapshotType.STATELESS)) {
            log.info("Deleting snapshots for stateless vm '{}'", vm.getId());
            VmOperationParameterBase restoreParams = new VmOperationParameterBase(vm.getId());

            // setting RestoreStatelessVm to run in new transaction so it could rollback internally if needed,
            // but still not affect this command, in order to keep permissions changes even on restore failure
            restoreParams.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            runInternalAction(VdcActionType.RestoreStatelessVm, restoreParams,
                    getContext().withCompensationContext(null));
        }
    }

    @Override
    protected void executeCommand() {
        if (getVmPoolId() != null) {
            detachVmFromUser();
        }
        setSucceeded(true);
    }
}
