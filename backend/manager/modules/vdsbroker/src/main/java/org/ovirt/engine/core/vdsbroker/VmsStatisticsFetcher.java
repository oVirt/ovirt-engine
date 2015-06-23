package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

import java.util.Map;

public class VmsStatisticsFetcher extends VmsListFetcher {

    public VmsStatisticsFetcher(VdsManager vdsManager) {
        super(vdsManager);
    }

    @Override
    public boolean fetch() {
        VDSReturnValue getStats = ResourceManager.getInstance()
                .runVdsCommand(
                        VDSCommandType.GetAllVmStats,
                        new VdsIdAndVdsVDSCommandParametersBase(vdsManager.getCopyVds()));
        if (getStats.getSucceeded()) {
            vdsmVms = (Map<Guid, VmInternalData>) getStats.getReturnValue();
            onFetchVms();
            return true;
        } else {
            onError();
            return false;
        }
    }

    @Override
    protected void gatherChangedVms(VM dbVm, VmInternalData vdsmVm) {
        changedVms.add(new Pair<>(dbVm, vdsmVm));
    }
}
