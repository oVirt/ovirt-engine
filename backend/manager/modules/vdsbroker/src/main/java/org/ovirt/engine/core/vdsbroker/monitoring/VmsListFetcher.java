package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * in-charge of fetching vms list together with the db counter-part
 * and store for analysis on VdsManager
 */
public class VmsListFetcher {

    protected VdsManager vdsManager;
    protected List<Pair<VmDynamic, VdsmVm>> changedVms;
    protected Map<Guid, VdsmVm> vdsmVms;
    private Map<Guid, VmDynamic> dbVms;
    private StringBuilder logBuilder;

    // dependencies
    private ResourceManager resourceManager;
    private VmDynamicDao vmDynamicDao;

    private static final Logger log = LoggerFactory.getLogger(VmsListFetcher.class);
    private static final Map<Guid, Integer> vdsIdToNumOfVms = new HashMap<>();

    public VmsListFetcher(VdsManager vdsManager) {
        this.vdsManager = vdsManager;
        this.resourceManager = Injector.get(ResourceManager.class);
        this.vmDynamicDao = Injector.get(VmDynamicDao.class);
    }

    @SuppressWarnings("unchecked")
    public boolean fetch() {
        VDSReturnValue pollReturnValue = poll();
        if (pollReturnValue.getSucceeded()) {
            vdsmVms = (Map<Guid, VdsmVm>) pollReturnValue.getReturnValue();
            onFetchVms();
            return true;
        } else {
            onError();
            return false;
        }
    }

    protected VDSReturnValue poll() {
        return getResourceManager().runVdsCommand(
                VDSCommandType.GetAllVmStats,
                new VdsIdVDSCommandParametersBase(vdsManager.getVdsId()));
    }

    private void logNumOfVmsIfChanged() {
        int numOfVms = vdsmVms.size();
        Guid vdsId = vdsManager.getVdsId();
        Integer prevNumOfVms = vdsIdToNumOfVms.put(vdsId, numOfVms);
        if (prevNumOfVms == null || prevNumOfVms.intValue() != numOfVms) {
            log.info("Fetched {} VMs from VDS '{}'", numOfVms, vdsId);
        }
    }

    protected void onFetchVms() {
        if (log.isDebugEnabled()) {
            logBuilder = new StringBuilder();
        }
        dbVms = getVmDynamicDao().getAllRunningForVds(vdsManager.getVdsId()).stream()
                .collect(Collectors.toMap(VmDynamic::getId, Function.identity()));
        changedVms = new ArrayList<>();
        filterVms();
        gatherNonRunningVms(dbVms);
        saveLastVmsList(vdsmVms);
        logNumOfVmsIfChanged();
        if (log.isDebugEnabled()) {
            log.debug(logBuilder.toString());
        }
    }

    private void saveLastVmsList(Map<Guid, VdsmVm> vdsmVms) {
        List<VmDynamic> vms = new ArrayList<>(vdsmVms.size());
        for (VdsmVm vmInternalData : this.vdsmVms.values()) {
            if (dbVms.containsKey(vmInternalData.getVmDynamic().getId())) {
                vms.add(vmInternalData.getVmDynamic());
            }
        }
        vdsManager.setLastVmsList(vms);
    }

    protected void onError() {
        dbVms = Collections.emptyMap();
        vdsmVms = Collections.emptyMap();
    }

    protected void filterVms() {
        for (VdsmVm vdsmVm : vdsmVms.values()) {
            VmDynamic dbVm = dbVms.get(vdsmVm.getVmDynamic().getId());

            gatherChangedVms(dbVm, vdsmVm);
        }
    }

    protected void gatherChangedVms(VmDynamic dbVm, VdsmVm vdsmVm) {
        changedVms.add(new Pair<>(dbVm, vdsmVm));
        if (log.isDebugEnabled()) {
            logBuilder.append(String.format("%s:%s ",
                    vdsmVm.getVmDynamic().getId().toString().substring(0, 8),
                    vdsmVm.getVmDynamic().getStatus()));
        }
    }

    private void gatherNonRunningVms(Map<Guid, VmDynamic> dbVms) {
        for (VmDynamic dbVm : dbVms.values()) {
            if (!vdsmVms.containsKey(dbVm.getId())) {
                // non running vms are also treated as changed VMs
                changedVms.add(new Pair<>(dbVm, null));
            }
        }
    }

    public VmDynamicDao getVmDynamicDao() {
        return vmDynamicDao;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public List<Pair<VmDynamic, VdsmVm>> getChangedVms() {
        return changedVms;
    }

}
