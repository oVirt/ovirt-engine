package org.ovirt.engine.core.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VmsStatisticsFetcher extends VmsListFetcher {
    private static final Logger log = LoggerFactory.getLogger(VmsStatisticsFetcher.class);
    private static final Map<Guid, Integer> vdsIdToNumOfVms = new HashMap<>();
    private StringBuilder logBuilder;


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
    protected void onFetchVms() {
        if (log.isDebugEnabled()) {
            logBuilder = new StringBuilder();
        }
        super.onFetchVms();
        logNumOfVmsIfChanged();
        if (log.isDebugEnabled()) {
            log.debug(logBuilder.toString());
        }
    }

    private void logNumOfVmsIfChanged() {
        int numOfVms = vdsmVms.size();
        Guid vdsId = vdsManager.getVdsId();
        Integer prevNumOfVms = vdsIdToNumOfVms.put(vdsId, numOfVms);
        if (prevNumOfVms == null || prevNumOfVms.intValue() != numOfVms) {
            log.info("Fetched {} VMs from VDS '{}'", numOfVms, vdsId);
        }
    }

    @Override
    protected void gatherChangedVms(VM dbVm, VmInternalData vdsmVm) {
        changedVms.add(new Pair<>(dbVm, vdsmVm));
        if (log.isDebugEnabled()) {
            logBuilder.append(String.format("%s:%s ",
                    vdsmVm.getVmDynamic().getId().toString().substring(0, 8),
                    vdsmVm.getVmDynamic().getStatus()));
        }
    }
}
