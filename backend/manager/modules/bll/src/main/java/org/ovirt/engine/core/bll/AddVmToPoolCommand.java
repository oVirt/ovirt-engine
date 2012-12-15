package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmToPoolParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddVmToPoolCommand<T extends AddVmToPoolParameters> extends VmPoolCommandBase<T> {
    public AddVmToPoolCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getVmId());
    }

    /**
     * Vm can be added to pool only if it not attach to user.
     *
     * @param vmId
     *            The vm id.
     * @param messages
     *            The messages.
     * @param poolId
     *            The pool id.
     * @return <c>true</c> if this instance [can add vm to pool] the specified
     *         vm id; otherwise, <c>false</c>.
     */
    public static boolean canAddVmToPool(Guid vmId, java.util.ArrayList<String> messages, NGuid poolId) {
        boolean returnValue = true;

        boolean isRunning = RemoveVmCommand.IsVmRunning(vmId);
        if (isRunning) {
            returnValue = false;
            if (messages != null) {
                messages.add(VdcBllMessages.VM_POOL_CANNOT_ADD_RUNNING_VM_TO_POOL.toString());
            }
        }
        if (DbFacade.getInstance().getVmPoolDao().getVmPoolMapByVmGuid(vmId) != null) {
            returnValue = false;
            if (messages != null) {
                messages.add(VdcBllMessages.VM_POOL_CANNOT_ADD_VM_ATTACHED_TO_POOL.toString());
            }
        }
        if (poolId != null) {
            VM vm = DbFacade.getInstance().getVmDao().get(vmId);
            if (vm != null) {
                vm_pools pool = DbFacade.getInstance().getVmPoolDao().get(poolId);
                if (pool != null) {
                    if (messages != null && !pool.getvds_group_id().equals(vm.getVdsGroupId())) {
                        messages.add(VdcBllMessages.VM_POOL_CANNOT_ADD_VM_DIFFERENT_CLUSTER.toString());
                    }
                }
            }

        }
        return returnValue;
    }

    @Override
    protected boolean canDoAction() {
        return canAddVmToPool(getParameters().getVmId(), getReturnValue().getCanDoActionMessages(), getParameters()
                .getVmPoolId());
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getVmPoolDao().addVmToPool(new VmPoolMap(getVmId(), getVmPoolId()));
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_VM_TO_POOL : AuditLogType.USER_ADD_VM_TO_POOL_FAILED;
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }
}
