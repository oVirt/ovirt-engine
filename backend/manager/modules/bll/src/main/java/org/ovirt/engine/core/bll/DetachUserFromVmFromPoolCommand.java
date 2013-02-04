package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.action.VmPoolSimpleUserParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@DisableInPrepareMode
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

    protected void DetachAllVmsFromUser() {
        List<VM> vms = DbFacade.getInstance().getVmDao().getAllForUser(getAdUserId());
        for (VM vm : vms) {
            if (getVmPoolId().equals(vm.getVmPoolId())) {
                permissions perm = DbFacade
                        .getInstance()
                        .getPermissionDao()
                        .getForRoleAndAdElementAndObject(
                                PredefinedRoles.ENGINE_USER.getId(),
                                getAdUserId(), vm.getId());
                if (perm != null) {
                    DbFacade.getInstance().getPermissionDao().remove(perm.getId());
                    RestoreVmFromBaseSnapshot(vm);
                }
            }
        }

    }

    private void RestoreVmFromBaseSnapshot(VM vm) {
        if (DbFacade.getInstance().getSnapshotDao().exists(vm.getId(), SnapshotType.STATELESS)) {
            log.infoFormat("Deleting snapshots for stateless vm {0}", vm.getId());
            VmOperationParameterBase restoreParams = new VmOperationParameterBase(vm.getId());

            // setting RestoreStatelessVm to run in new transaction so it could rollback internally if needed,
            // but still not affect this command, in order to keep permissions changes even on restore failure
            restoreParams.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            Backend.getInstance().runInternalAction(VdcActionType.RestoreStatelessVm, restoreParams,
                    new CommandContext(getExecutionContext(), getLock()));
        }
    }

    @Override
    protected void executeCommand() {
        if (getVmPoolId() != null) {
            DetachAllVmsFromUser();
        }
        setSucceeded(true);
    }
}
