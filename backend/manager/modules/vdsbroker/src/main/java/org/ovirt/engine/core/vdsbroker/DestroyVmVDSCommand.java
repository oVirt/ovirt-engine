package org.ovirt.engine.core.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
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
        resourceManager.removeAsyncRunningVm(parameters.getVmId());

        final VM curVm = DbFacade.getInstance().getVmDao().get(parameters.getVmId());
        curVm.setInterfaces(DbFacade.getInstance().getVmNetworkInterfaceDao().getAllForVm(curVm.getId()));
        curVm.setvNumaNodeList(getVmNumaNodeDao().getAllVmNumaNodeByVmId(curVm.getId()));

        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(VDSCommandType.Destroy, parameters);
        if (vdsReturnValue.getSucceeded()) {
            if (curVm.getStatus() == VMStatus.Down) {
                getVDSReturnValue().setReturnValue(VMStatus.Down);
            }

            changeStatus(parameters, curVm);

            TransactionSupport.executeInNewTransaction(() -> {
                curVm.setStopReason(getParameters().getReason());
                vmManager.update(curVm.getDynamicData());
                vmManager.update(curVm.getStatisticsData());
                update(curVm.getInterfaces());
                getVmNumaNodeDao().massUpdateVmNumaNodeRuntimePinning(curVm.getvNumaNodeList());
                return null;
            });
            getVDSReturnValue().setReturnValue(curVm.getStatus());
        } else if (vdsReturnValue.getExceptionObject() != null) {
            log.error("Failed to destroy VM '{}' in VDS = '{}' , error = '{}'",
                    parameters.getVmId(),
                    getParameters().getVdsId(),
                    vdsReturnValue.getExceptionString());
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
            getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
            getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
        }
    }

    private void changeStatus(DestroyVmVDSCommandParameters parameters, VM curVm) {
        // do the state transition only if that VM is really running on SRC
        if (getParameters().getVdsId().equals(curVm.getRunOnVds())) {
            resourceManager.internalSetVmStatus(curVm, VMStatus.PoweringDown);
        }
    }

    protected VmNumaNodeDao getVmNumaNodeDao() {
        return DbFacade.getInstance().getVmNumaNodeDao();
    }

    private void update(List<VmNetworkInterface> interfaces) {
        if (interfaces == null) {
            return;
        }
        for (VmNetworkInterface ifc : interfaces) {
            vmManager.update(ifc.getStatistics());
        }
    }
}
