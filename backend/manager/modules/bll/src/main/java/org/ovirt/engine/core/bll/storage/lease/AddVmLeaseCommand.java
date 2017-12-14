package org.ovirt.engine.core.bll.storage.lease;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmLeaseParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.LeaseVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.dao.VmStaticDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class AddVmLeaseCommand<T extends VmLeaseParameters> extends VmLeaseCommandBase<T> {

    @Inject
    private VmStaticDao vmStaticDao;

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
        return AsyncTaskType.addVmLease;
    }

    @Override
    protected void endSuccessfully() {
        // it would be nicer to get this as part of the tasks rather
        // than initiating another call to the host, this approach is
        // easier and backward compatible though
        VDSReturnValue retVal = null;
        try {
            retVal = runVdsCommand(VDSCommandType.GetVmLeaseInfo,
                    new VmLeaseVDSParameters(getParameters().getStoragePoolId(),
                            getParameters().getStorageDomainId(),
                            getParameters().getVmId()));
        } catch (EngineException e) {
            log.error("Failure in getting lease info for VM {}, message: {}",
                    getParameters().getVmId(), e.getMessage());
        }

        if (retVal == null || !retVal.getSucceeded()) {
            log.error("Failed to get info on the lease of VM {}", getParameters().getVmId());
            return;
        }

        vmStaticDao.updateVmLeaseInfo(
                getParameters().getVmId(),
                (Map<String, String>) retVal.getReturnValue());

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
