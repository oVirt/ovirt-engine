package org.ovirt.engine.core.vdsbroker;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DestroyVDSCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DestroyVmVDSCommand<P extends DestroyVmVDSCommandParameters> extends ManagingVmCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(DestroyVmVDSCommand.class);

    public DestroyVmVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {

        final DestroyVmVDSCommandParameters parameters = getParameters();
        ResourceManager.getInstance().RemoveAsyncRunningVm(parameters.getVmId());

        final VM curVm = DbFacade.getInstance().getVmDao().get(parameters.getVmId());
        curVm.setInterfaces(DbFacade.getInstance().getVmNetworkInterfaceDao().getAllForVm(curVm.getId()));
        curVm.setvNumaNodeList(DbFacade.getInstance().getVmNumaNodeDAO().getAllVmNumaNodeByVmId(curVm.getId()));

        DestroyVDSCommand<DestroyVmVDSCommandParameters> vdsBrokerCommand =
                new DestroyVDSCommand<DestroyVmVDSCommandParameters>(parameters);
        vdsBrokerCommand.execute();
        if (vdsBrokerCommand.getVDSReturnValue().getSucceeded()) {
            if (curVm.getStatus() == VMStatus.Down) {
                getVDSReturnValue().setReturnValue(VMStatus.Down);
            }

            changeStatus(parameters, curVm);

            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {

                    curVm.guestLogoutTimeTreatmentAfterDestroy();
                    curVm.setStopReason(getParameters().getReason());
                    vmManager.update(curVm.getDynamicData());
                    vmManager.update(curVm.getStatisticsData());
                    List<VmNetworkInterface> interfaces = curVm.getInterfaces();
                    if (interfaces != null && interfaces.size() > 0) {
                        for (VmNetworkInterface ifc : interfaces) {
                            VmNetworkStatistics stats = ifc.getStatistics();
                            vmManager.update(stats);
                        }
                        DbFacade.getInstance()
                                .getVmNumaNodeDAO()
                                .massUpdateVmNumaNodeRuntimePinning(curVm.getvNumaNodeList());
                        return null;
                    }
                    return null;
                }
            });

            // if using stop then call to ProcessOnVmStop because
            // will not be called from UpdateRunTimeInfo
            if (!parameters.getGracefully()) {
                ResourceManager.getInstance().getEventListener()
                        .processOnVmStop(Collections.singleton(curVm.getId()));
            }

            getVDSReturnValue().setReturnValue(curVm.getStatus());
        } else if (vdsBrokerCommand.getVDSReturnValue().getExceptionObject() != null) {
            log.error("VDS::destroy Failed destroying VM '{}' in vds = '{}' , error = '{}'",
                    parameters.getVmId(),
                    getParameters().getVdsId(),
                    vdsBrokerCommand
                            .getVDSReturnValue().getExceptionString());
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(vdsBrokerCommand.getVDSReturnValue()
                    .getExceptionString());
            getVDSReturnValue().setExceptionObject(vdsBrokerCommand.getVDSReturnValue()
                    .getExceptionObject());
            getVDSReturnValue().setVdsError(vdsBrokerCommand.getVDSReturnValue().getVdsError());
        }
    }

    private void changeStatus(DestroyVmVDSCommandParameters parameters, VM curVm) {
        // do the state transition only if that VM is really running on SRC
        if (getParameters().getVdsId().equals(curVm.getRunOnVds())) {
            ResourceManager.getInstance().InternalSetVmStatus(curVm,
                    parameters.getGracefully() ? VMStatus.PoweringDown : VMStatus.Down);
        }
    }
}
