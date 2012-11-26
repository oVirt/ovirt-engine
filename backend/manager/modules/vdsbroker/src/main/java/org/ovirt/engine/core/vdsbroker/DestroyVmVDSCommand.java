package org.ovirt.engine.core.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DestroyVDSCommand;

public class DestroyVmVDSCommand<P extends DestroyVmVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public DestroyVmVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {

        if (_vdsManager != null) {

            final DestroyVmVDSCommandParameters parameters = getParameters();
            ResourceManager.getInstance().RemoveAsyncRunningVm(parameters.getVmId());

            final VM curVm = DbFacade.getInstance().getVmDao().get(parameters.getVmId());
            curVm.setInterfaces(DbFacade.getInstance().getVmNetworkInterfaceDao().getAllForVm(curVm.getId()));

            DestroyVDSCommand<DestroyVmVDSCommandParameters> vdsBrokerCommand =
                    new DestroyVDSCommand<DestroyVmVDSCommandParameters>(parameters);
            vdsBrokerCommand.Execute();
            if (vdsBrokerCommand.getVDSReturnValue().getSucceeded()) {
                if (curVm.getStatus() == VMStatus.Down) {
                    getVDSReturnValue().setReturnValue(VMStatus.Down);
                }

                // Updating the DB
                ResourceManager.getInstance().InternalSetVmStatus(curVm,
                        parameters.getGracefully() ? VMStatus.PoweringDown : VMStatus.Down);

                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                    @Override
                    public Void runInTransaction() {

                        curVm.guestLogoutTimeTreatmentAfterDestroy();
                        // SaveVmDynamicToDBThreaded(curVm);
                        DbFacade.getInstance().getVmDynamicDao().update(curVm.getDynamicData());
                        DbFacade.getInstance().getVmStatisticsDao().update(curVm.getStatisticsData());
                        List<VmNetworkInterface> interfaces = curVm.getInterfaces();
                        if (interfaces != null && interfaces.size() > 0) {
                            for (VmNetworkInterface ifc : interfaces) {
                                VmNetworkStatistics stats = ifc.getStatistics();
                                DbFacade.getInstance().getVmNetworkStatisticsDao().update(stats);
                            }
                        }
                        getVds().setmem_commited(getVds().getmem_commited() - curVm.getVmMemSizeMb());
                        getVds().setmem_commited(getVds().getmem_commited() - getVds().getguest_overhead());
                        getVds().setvms_cores_count(getVds().getvms_cores_count() - curVm.getNumOfCpus());
                        _vdsManager.UpdateDynamicData(getVds().getDynamicData());
                        return null;
                    }
                });

                // if using stop then call to ProcessOnVmStop because
                // will not be called from UpdateRunTimeInfo
                if (!parameters.getGracefully()) {
                    onVmStop(curVm);
                }

                getVDSReturnValue().setReturnValue(curVm.getStatus());
            } else if (vdsBrokerCommand.getVDSReturnValue().getExceptionObject() != null) {
                log.errorFormat("VDS::destroy Failed destroying vm '{0}' in vds = {1} : {2}, error = {3}",
                        parameters.getVmId(),
                        getVds().getId(),
                        getVds().getvds_name(),
                        vdsBrokerCommand
                                .getVDSReturnValue().getExceptionString());
                getVDSReturnValue().setSucceeded(false);
                getVDSReturnValue().setExceptionString(vdsBrokerCommand.getVDSReturnValue()
                        .getExceptionString());
                getVDSReturnValue().setExceptionObject(vdsBrokerCommand.getVDSReturnValue()
                        .getExceptionObject());
                getVDSReturnValue().setVdsError(vdsBrokerCommand.getVDSReturnValue().getVdsError());
            }
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }

    /**
     * signal the event listener that this VM is down. Because we don't care for failures, to prevent a side effect of
     * aborting the current TX this method is being invoked in a new TX of its own.
     * @param curVm
     *            current VM
     */
    private void onVmStop(final VM curVm) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                ResourceManager.getInstance()
                        .getEventListener()
                        .processOnVmStop(curVm.getId());
                return null;
            }
        });
    }

    private static Log log = LogFactory.getLog(DestroyVmVDSCommand.class);
}
