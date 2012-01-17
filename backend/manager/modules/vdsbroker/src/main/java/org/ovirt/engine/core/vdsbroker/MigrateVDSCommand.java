package org.ovirt.engine.core.vdsbroker;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
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
            command.Execute();
            VDSReturnValue vdsReturnValue = command.getVDSReturnValue();
            if (vdsReturnValue.getSucceeded()) {
                retval = VMStatus.MigratingFrom;
            } else {
                log.error("VDS::migrate:: Failed migration setting vm status to ERROR");
                retval = VMStatus.NotResponding;
                getVDSReturnValue().setSucceeded(false);
                getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
                getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
                getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
            }
            // update the db
            // VM vm = _vdsManager.VmDict[parameters.VmId];
            final VM vm = DbFacade.getInstance().getVmDAO().getById(parameters.getVmId());
            ResourceManager.getInstance().InternalSetVmStatus(vm, retval);
            if (retval == VMStatus.MigratingFrom) {
                vm.setmigrating_to_vds(parameters.getDstVdsId());

                // get vdsEventListener from callback channel (if wcf-user backend) or resource manager
                if (ResourceManager.getInstance().getBackendCallback() != null) {
                    ResourceManager.getInstance().AddAsyncRunningVm(parameters.getVmId(),
                            ResourceManager.getInstance().getBackendCallback());
                }
            }

            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    DbFacade.getInstance().getVmDynamicDAO().update(vm.getDynamicData());
                    return null;
                }
            });

            if (retval == VMStatus.MigratingFrom) {
                UpdateDestinationVdsThreaded(parameters.getDstVdsId(), vm);
            }

            getVDSReturnValue().setReturnValue(retval);
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }

    private void UpdateDestinationVdsThreaded(Guid dstVdsId, VM vm) {
        VdsManager vdsManager = ResourceManager.getInstance().GetVdsManager(dstVdsId);

        if (vdsManager != null) {
            // TODO use thread pool
            Class<?>[] inputTypes = new Class[] { VdsManager.class, VM.class };
            Object[] inputParams = new Object[] { vdsManager, vm };
            SchedulerUtilQuartzImpl.getInstance().scheduleAOneTimeJob(this, "UpdateDestinationVdsOnTimer", inputTypes,
                    inputParams, 0, TimeUnit.MILLISECONDS);
        }
    }

    @OnTimerMethodAnnotation("UpdateDestinationVdsOnTimer")
    public void UpdateDestinationVdsOnTimer(final VdsManager vdsManager, final VM vm) {
        synchronized (vdsManager.getLockObj()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Suppress, new TransactionMethod<Object>() {
                @Override
                public Object runInTransaction() {
                    VDS vds = null;
                    try {
                        vds = DbFacade.getInstance().getVdsDAO().get(vdsManager.getVdsId());
                        vds.setvm_count(vds.getvm_count() + 1);
                        vds.setpending_vcpus_count(vds.getpending_vcpus_count() + vm.getnum_of_cpus());
                        vds.setpending_vmem_size(vds.getpending_vmem_size() + vm.getMinAllocatedMem());
                        if (log.isDebugEnabled()) {
                            log.debugFormat(
                                    "IncreasePendingVms::MigrateVm Increasing vds {0} pending vcpu count, now {1}, and pending vmem size, now {2}. Vm: {3}",
                                    vds.getvds_name(),
                                    vds.getpending_vcpus_count(),
                                    vds.getpending_vmem_size(),
                                    vm.getvm_name());
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
                                    vds.getvds_id(),
                                    vds.getvds_name(),
                                    ex.toString(),
                                    ex.getStackTrace()[0]);
                        }
                    }

                    return null;
                }
            });
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(MigrateVDSCommand.class);
}
