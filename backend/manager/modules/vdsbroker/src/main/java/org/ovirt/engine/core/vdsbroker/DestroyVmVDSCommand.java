package org.ovirt.engine.core.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.generic.RepositoryException;
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
            final VM curVm = DbFacade.getInstance().getVmDAO().getById(parameters.getVmId());
            DestroyVDSCommand<DestroyVmVDSCommandParameters> vdsBrokerCommand =
                new DestroyVDSCommand<DestroyVmVDSCommandParameters>(parameters);
            vdsBrokerCommand.Execute();
            if (vdsBrokerCommand.getVDSReturnValue().getSucceeded()) {
                if (curVm.getstatus() == VMStatus.Down) {
                    getVDSReturnValue().setReturnValue(VMStatus.Down);
                }

                // Updating the DB
                ResourceManager.getInstance().InternalSetVmStatus(curVm,
                        parameters.getGracefully() ? VMStatus.PoweringDown : VMStatus.Down);

                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                    @Override
                    public Void runInTransaction() {

                        try {
                            curVm.guestLogoutTimeTreatmentAfterDestroy();
                            // SaveVmDynamicToDBThreaded(curVm);
                            DbFacade.getInstance().getVmDynamicDAO().update(curVm.getDynamicData());
                            DbFacade.getInstance().getVmStatisticsDAO().update(curVm.getStatisticsData());
                            List<VmNetworkInterface> interfaces = curVm.getInterfaces();
                            if (interfaces != null && interfaces.size() > 0) {
                                for (VmNetworkInterface ifc : interfaces) {
                                    VmNetworkStatistics stats = ifc.getStatistics();
                                    DbFacade.getInstance().getVmNetworkStatisticsDAO().update(stats);
                                }
                            }

                            // if using stop then call to ProcessOnVmStop because
                            // will not be called from UpdateRunTimeInfo
                            if (!parameters.getGracefully()) {
                                ResourceManager.getInstance()
                                .getEventListener()
                                .ProcessOnVmStop(curVm.getvm_guid());
                            }
                        } catch (RepositoryException ex) {
                            log.errorFormat(
                                    "VDS::destroy Failed to update vds status in database,  vds = {1} : {2}, error = {3}",
                                    getVds().getvds_id(),
                                    getVds().getvds_name(),
                                    ex.getMessage());
                            log.error("Exception: ", ex);
                            throw ex;
                        }

                        getVds().setmem_commited(getVds().getmem_commited() - curVm.getvm_mem_size_mb());
                        getVds().setmem_commited(getVds().getmem_commited() - getVds().getguest_overhead());
                        getVds().setvms_cores_count(getVds().getvms_cores_count() - curVm.getnum_of_cpus());
                        _vdsManager.UpdateDynamicData(getVds().getDynamicData());
                        return null;
                    }
                });

                getVDSReturnValue().setReturnValue(curVm.getstatus());
            } else if (vdsBrokerCommand.getVDSReturnValue().getExceptionObject() != null) {
                log.errorFormat("VDS::destroy Failed destroying vm '{0}' in vds = {1} : {2}, error = {3}",
                        parameters.getVmId(),
                        getVds().getvds_id(),
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

    private static LogCompat log = LogFactoryCompat.getLog(DestroyVmVDSCommand.class);
}
