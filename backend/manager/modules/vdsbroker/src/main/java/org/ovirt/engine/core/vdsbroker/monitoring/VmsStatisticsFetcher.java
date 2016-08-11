package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.VdsManager;
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
    protected VDSReturnValue poll() {
        return getResourceManager().runVdsCommand(
                VDSCommandType.GetAllVmStats,
                new VdsIdVDSCommandParametersBase(vdsManager.getVdsId()));
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
    protected void gatherChangedVms(VmDynamic dbVm, VdsmVm vdsmVm) {
        changedVms.add(new Pair<>(dbVm, vdsmVm));
        if (log.isDebugEnabled()) {
            logBuilder.append(String.format("%s:%s ",
                    vdsmVm.getVmDynamic().getId().toString().substring(0, 8),
                    vdsmVm.getVmDynamic().getStatus()));
        }
    }
}
