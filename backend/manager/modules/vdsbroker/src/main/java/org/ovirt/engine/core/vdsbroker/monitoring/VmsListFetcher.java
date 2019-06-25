package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.ArrayList;
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

    private VdsManager vdsManager;
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
    public List<Pair<VmDynamic, VdsmVm>> fetch() {
        VDSReturnValue pollReturnValue = poll();
        if (!pollReturnValue.getSucceeded()) {
            return null;
        }

        List<VdsmVm> vdsmVms = (List<VdsmVm>) pollReturnValue.getReturnValue();
        return onFetchVms(vdsmVms);
    }

    protected VDSReturnValue poll() {
        return getResourceManager().runVdsCommand(
                VDSCommandType.GetAllVmStats,
                new VdsIdVDSCommandParametersBase(vdsManager.getVdsId()));
    }

    private void logNumOfVmsIfChanged(List<VdsmVm> vdsmVms) {
        int numOfVms = vdsmVms.size();
        Guid vdsId = vdsManager.getVdsId();
        Integer prevNumOfVms = vdsIdToNumOfVms.put(vdsId, numOfVms);
        if (prevNumOfVms == null || prevNumOfVms.intValue() != numOfVms) {
            log.info("Fetched {} VMs from VDS '{}'", numOfVms, vdsId);
        }
    }

    protected List<Pair<VmDynamic, VdsmVm>> onFetchVms(List<VdsmVm> vdsmVms) {
        if (log.isDebugEnabled()) {
            logBuilder = new StringBuilder();
        }
        Map<Guid, VmDynamic> dbVms = getVmDynamicDao().getAllRunningForVds(vdsManager.getVdsId()).stream()
                .collect(Collectors.toMap(VmDynamic::getId, Function.identity()));
        List<Pair<VmDynamic, VdsmVm>> pairs = matchVms(dbVms, vdsmVms);
        saveLastVmsList(vdsmVms, dbVms);
        logNumOfVmsIfChanged(vdsmVms);
        if (log.isDebugEnabled()) {
            log.debug(logBuilder.toString());
        }
        return pairs;
    }

    private void saveLastVmsList(List<VdsmVm> vdsmVms, Map<Guid, VmDynamic> dbVms) {
        List<VmDynamic> vms = new ArrayList<>(vdsmVms.size());
        for (VdsmVm vmInternalData : vdsmVms) {
            if (dbVms.containsKey(vmInternalData.getVmDynamic().getId())) {
                vms.add(vmInternalData.getVmDynamic());
            }
        }
        vdsManager.setLastVmsList(vms);
    }

    protected List<Pair<VmDynamic, VdsmVm>> matchVms(Map<Guid, VmDynamic> dbVms, List<VdsmVm> vdsmVms) {
        List<Pair<VmDynamic, VdsmVm>> pairs = new ArrayList<>(vdsmVms.size());
        for (VdsmVm vdsmVm : vdsmVms) {
            VmDynamic dbVm = dbVms.remove(vdsmVm.getId());
            pairs.add(new Pair<>(dbVm, vdsmVm));

            if (log.isDebugEnabled()) {
                logBuilder.append(String.format("%s:%s ",
                        vdsmVm.getVmDynamic().getId().toString().substring(0, 8),
                        vdsmVm.getVmDynamic().getStatus()));
            }
        }
        for (VmDynamic dbVm : dbVms.values()) {
            // non running vms are also treated as changed VMs
            pairs.add(new Pair<>(dbVm, null));
        }
        return pairs;
    }

    public VmDynamicDao getVmDynamicDao() {
        return vmDynamicDao;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

}
