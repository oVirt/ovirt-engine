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

public class SetVmStatusVDSCommand<P extends SetVmStatusVDSCommandParameters> extends VDSCommandBase<P> {
    public SetVmStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVDSCommand() {
        SetVmStatusVDSCommandParameters parameters = getParameters();
        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(parameters.getVmId());
        vmDynamic.setstatus(parameters.getStatus());
        if (VM.isStatusDown(parameters.getStatus())) {
            ResourceManager.getInstance().RemoveAsyncRunningVm(parameters.getVmId());
            VmStatistics vmStatistics = DbFacade.getInstance().getVmStatisticsDao().get(parameters.getVmId());
            VM vm = new VM(null, vmDynamic, vmStatistics);
            ResourceManager.getInstance().InternalSetVmStatus(vm, parameters.getStatus());
            DbFacade.getInstance().getVmStatisticsDao().update(vm.getStatisticsData());
            List<VmNetworkInterface> interfaces = vm.getInterfaces();
            if (interfaces != null && interfaces.size() > 0) {
                for (VmNetworkInterface ifc : interfaces) {
                    VmNetworkStatistics stats = ifc.getStatistics();
                    DbFacade.getInstance().getVmNetworkStatisticsDao().update(stats);
                }
            }

        } else if (parameters.getStatus() == VMStatus.Unknown) {
            ResourceManager.getInstance().RemoveAsyncRunningVm(parameters.getVmId());
        }
        DbFacade.getInstance().getVmDynamicDao().update(vmDynamic);
    }
}
