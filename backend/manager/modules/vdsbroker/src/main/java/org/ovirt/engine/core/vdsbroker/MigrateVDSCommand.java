package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.MigrateBrokerVDSCommand;

public class MigrateVDSCommand<P extends MigrateVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public MigrateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        MigrateVDSCommandParameters parameters = getParameters();
        if (_vdsManager != null) {
            VMStatus retval;
            MigrateBrokerVDSCommand<MigrateVDSCommandParameters> command =
                    new MigrateBrokerVDSCommand<MigrateVDSCommandParameters>(parameters);
            command.execute();
            VDSReturnValue vdsReturnValue = command.getVDSReturnValue();

            final VM vm = DbFacade.getInstance().getVmDao().get(parameters.getVmId());

            if (vdsReturnValue.getSucceeded()) {
                retval = VMStatus.MigratingFrom;
                ResourceManager.getInstance().InternalSetVmStatus(vm, VMStatus.MigratingFrom);
                    vm.setMigratingToVds(parameters.getDstVdsId());
                    ResourceManager.getInstance().AddAsyncRunningVm(parameters.getVmId());
            } else {
                retval = vm.getStatus();
                log.error("VDS::migrate:: Failed Vm migration");
                getVDSReturnValue().setSucceeded(false);
                getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
                getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
                getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
            }

            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    DbFacade.getInstance().getVmDynamicDao().update(vm.getDynamicData());
                    return null;
                }
            });

            if (retval == VMStatus.MigratingFrom) {
                updateDestinationVdsThreaded(parameters.getDstVdsId(), vm);
            }

            getVDSReturnValue().setReturnValue(retval);
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }

    private void updateDestinationVdsThreaded(Guid dstVdsId, final VM vm) {
        final VdsManager vdsManager = ResourceManager.getInstance().GetVdsManager(dstVdsId);

        if (vdsManager != null) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    updateDestinationVdsOnTimer(vdsManager, vm);
                }
            });
        }
    }

    private void updateDestinationVdsOnTimer(final VdsManager vdsManager, final VM vm) {
        synchronized (vdsManager.getLockObj()) {
            VDS vds = DbFacade.getInstance().getVdsDao().get(vdsManager.getVdsId());
            try {
                vds.setVmCount(vds.getVmCount() + 1);
                vds.setPendingVcpusCount(vds.getPendingVcpusCount() + vm.getNumOfCpus());
                vds.setPendingVmemSize(vds.getPendingVmemSize() + vm.getMinAllocatedMem());
                if (log.isDebugEnabled()) {
                    log.debugFormat(
                            "IncreasePendingVms::MigrateVm Increasing vds {0} pending vcpu count, now {1}, and pending vmem size, now {2}. Vm: {3}",
                            vds.getVdsName(),
                            vds.getPendingVcpusCount(),
                            vds.getPendingVmemSize(),
                            vm.getName());
                }
                vdsManager.UpdateDynamicData(vds.getDynamicData());
            } catch (RuntimeException ex) {
                if (vds == null) {
                    log.fatalFormat(
                            "VDS::migrate:: Could not update destination vds commited memory to db. vds {0} : was not find, error: {1}, {2}",
                            vdsManager.getVdsId(),
                            ex.toString(),
                            ex.getStackTrace()[0]);
                } else {
                    log.fatalFormat(
                            "VDS::migrate:: Could not update destination vds commited memory to db. vds {0} : {1}, error: {2}, {3}",
                            vds.getId(),
                            vds.getVdsName(),
                            ex.toString(),
                            ex.getStackTrace()[0]);
                }
            }

        }
    }

    private static Log log = LogFactory.getLog(MigrateVDSCommand.class);
}
