package org.ovirt.engine.core.vdsbroker;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVmStatusVDSCommand<P extends SetVmStatusVDSCommandParameters> extends VDSCommandBase<P> {

    private static final Logger log = LoggerFactory.getLogger(SetVmStatusVDSCommand.class);

    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmStatisticsDao vmStatisticsDao;
    @Inject
    private VmNetworkStatisticsDao vmNetworkStatisticsDao;

    public SetVmStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        final VMStatus status = getParameters().getStatus();
        if (status == null) {
            log.warn("got request to change the status of VM '{}' to null,  ignoring", getParameters().getVmId());
            return;
        }

        VmDynamic vmDynamic = vmDynamicDao.get(getParameters().getVmId());
        vmDynamic.setStatus(status);
        if (status.isNotRunning()) {
            resourceManager.removeAsyncRunningVm(getParameters().getVmId());
            VmStatistics vmStatistics = vmStatisticsDao.get(getParameters().getVmId());
            VM vm = new VM(null, vmDynamic, vmStatistics);
            resourceManager.internalSetVmStatus(vm, status, getParameters().getExitStatus());
            resourceManager.getVmManager(getParameters().getVmId()).update(vm.getStatisticsData());
            List<VmNetworkInterface> interfaces = vm.getInterfaces();
            if (interfaces != null && !interfaces.isEmpty()) {
                for (VmNetworkInterface ifc : interfaces) {
                    vmNetworkStatisticsDao.update(ifc.getStatistics());
                }
            }

        } else if (status == VMStatus.Unknown) {
            resourceManager.removeAsyncRunningVm(getParameters().getVmId());
        }
        vmDynamicDao.update(vmDynamic);
    }
}
