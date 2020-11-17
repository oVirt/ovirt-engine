package org.ovirt.engine.core.bll.storage.lease;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmLeaseParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.LeaseVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.VmDynamicDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class AddVmLeaseCommand<T extends VmLeaseParameters> extends VmLeaseCommandBase<T> {

    @Inject
    private VmDynamicDao vmDynamicDao;

    public AddVmLeaseCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public AddVmLeaseCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected VDSCommandType getLeaseAction() {
        return VDSCommandType.AddVmLease;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.addLease;
    }

    @Override protected void executeCommand() {
        getParameters().setFailureExpected(true);
        log.info("Verify that VM '{}' lease doesn't exists on storage domain '{}'",
                getParameters().getVmId(),
                getParameters().getStorageDomainId());
        ActionReturnValue retVal = runInternalAction(ActionType.GetVmLeaseInfo, getParameters());
        if (retVal.getSucceeded() && retVal.getActionReturnValue() != null) {
            log.info("VM '{}' lease already exists on storage domain '{}': '{}'",
                    getParameters().getVmId(),
                    getParameters().getStorageDomainId(),
                    retVal.getActionReturnValue());
            getParameters().setVmLeaseInfo(retVal.getActionReturnValue());
            setSucceeded(true);
            return;
        }
        getParameters().setFailureExpected(false);
        log.info("Creating new VM '{}' lease, because the VM lease doesn't exists on storage domain '{}'",
                getParameters().getVmId(),
                getParameters().getStorageDomainId());
        super.executeCommand();
    }

    @Override
    protected void endSuccessfully() {
        // it would be nicer to get this as part of the tasks rather
        // than initiating another call to the host, this approach is
        // easier and backward compatible though
        if (getParameters().getVmLeaseInfo() == null) {
            ActionReturnValue retVal = runInternalAction(ActionType.GetVmLeaseInfo,
                    new VmLeaseParameters(getParameters().getStoragePoolId(),
                            getParameters().getStorageDomainId(),
                            getParameters().getVmId()));

            if (retVal == null || !retVal.getSucceeded()) {
                return;
            }
            getParameters().setVmLeaseInfo(retVal.getActionReturnValue());
        }

        vmDynamicDao.updateVmLeaseInfo(getParameters().getVmId(), getParameters().getVmLeaseInfo());

        if (getParameters().isHotPlugLease()) {
            boolean hotPlugSucceeded = false;
            try {
                hotPlugSucceeded = runVdsCommand(VDSCommandType.HotPlugLease,
                        new LeaseVDSParameters(getParameters().getVdsId(), getParameters().getVmId(),
                                getParameters().getStorageDomainId())).getSucceeded();
            } catch (EngineException e) {
                log.error("Failure in hot plugging a lease to VM {}, message: {}",
                        getParameters().getVmId(), e.getMessage());
            }

            if (!hotPlugSucceeded) {
                setVmId(getParameters().getVmId());
                auditLog(this, AuditLogType.HOT_PLUG_LEASE_FAILED);
                getReturnValue().setEndActionTryAgain(false);
                setSucceeded(false);
                return;
            }
        }
        setSucceeded(true);
    }
}
