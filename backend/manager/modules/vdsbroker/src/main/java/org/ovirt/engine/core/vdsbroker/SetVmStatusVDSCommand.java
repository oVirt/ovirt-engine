package org.ovirt.engine.core.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVmStatusVDSCommand<P extends SetVmStatusVDSCommandParameters> extends VDSCommandBase<P> {

    private static final Logger log = LoggerFactory.getLogger(SetVmStatusVDSCommand.class);

    public SetVmStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        SetVmStatusVDSCommandParameters parameters = getParameters();
        final VMStatus status = parameters.getStatus();

        if (status == null) {
            log.warn("got request to change the status of VM whose id is '{}' to null,  ignoring", parameters.getVmId());
            return;
        }

        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(parameters.getVmId());
        vmDynamic.setStatus(status);
        if (status.isNotRunning()) {
            resourceManager.removeAsyncRunningVm(parameters.getVmId());
            VmStatistics vmStatistics = DbFacade.getInstance().getVmStatisticsDao().get(parameters.getVmId());
            VM vm = new VM(null, vmDynamic, vmStatistics);
            resourceManager.internalSetVmStatus(vm, status, parameters.getExitStatus());
            resourceManager.getVmManager(parameters.getVmId()).update(vm.getStatisticsData());
            List<VmNetworkInterface> interfaces = vm.getInterfaces();
            if (interfaces != null && !interfaces.isEmpty()) {
                for (VmNetworkInterface ifc : interfaces) {
                    VmNetworkStatistics stats = ifc.getStatistics();
                    DbFacade.getInstance().getVmNetworkStatisticsDao().update(stats);
                }
            }

        } else if (status == VMStatus.Unknown) {
            resourceManager.removeAsyncRunningVm(parameters.getVmId());
        }
        DbFacade.getInstance().getVmDynamicDao().update(vmDynamic);
    }
}
