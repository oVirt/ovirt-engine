package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmsStatisticsFetcher extends VmsListFetcher {

    private static final Logger log = LoggerFactory.getLogger(VmsStatisticsFetcher.class);
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
        log.info("Fetched {} VMs from VDS '{}'",
                vdsmVms.size(),
                vdsManager.getVdsId());
        if (log.isDebugEnabled()) {
            log.debug(logBuilder.toString());
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
