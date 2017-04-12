package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVmStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDynamicDao;
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

    // dependencies
    private DbFacade dbFacade;
    private ResourceManager resourceManager;

    private static final Logger log = LoggerFactory.getLogger(VmsListFetcher.class);

    public VmsListFetcher(VdsManager vdsManager) {
        this.vdsManager = vdsManager;
        this.dbFacade = DbFacade.getInstance();
        this.resourceManager = ResourceManager.getInstance();
    }

    public VmsListFetcher(VdsManager vdsManager, DbFacade dbFacade, ResourceManager resourceManager) {
        this.vdsManager = vdsManager;
        this.dbFacade = dbFacade;
        this.resourceManager = resourceManager;
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
                VDSCommandType.List,
                new VdsIdAndVdsVDSCommandParametersBase(vdsManager.getCopyVds()));
    }

    protected void onFetchVms() {
        dbVms = getVmDynamicDao().getAllRunningForVds(vdsManager.getVdsId()).stream()
                .collect(Collectors.toMap(VmDynamic::getId, Function.identity()));
        changedVms = new ArrayList<>();
        filterVms();
        gatherNonRunningVms(dbVms);
        saveLastVmsList(vdsmVms);
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
        if (statusChanged(dbVm, vdsmVm.getVmDynamic())) {
            VDSReturnValue vmStats =
                    getResourceManager().runVdsCommand(
                            VDSCommandType.GetVmStats,
                            new GetVmStatsVDSCommandParameters(vdsManager.getVdsId(), vdsmVm.getVmDynamic().getId()));
            if (vmStats.getSucceeded()) {
                changedVms.add(new Pair<>(dbVm, (VdsmVm) vmStats.getReturnValue()));
            } else {
                if (dbVm != null) {
                    log.error(
                            "failed to fetch VM '{}' stats. status remain unchanged ({})",
                            dbVm.getId(),
                            dbVm.getStatus());
                }
            }
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

    private boolean statusChanged(VmDynamic dbVm, VmDynamic vdsmVm) {
        return dbVm == null || (dbVm.getStatus() != vdsmVm.getStatus());
    }

    public VmDynamicDao getVmDynamicDao() {
        return dbFacade.getVmDynamicDao();
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public Collection<VdsmVm> getVdsmVms() {
        return vdsmVms.values();
    }

    public List<Pair<VmDynamic, VdsmVm>> getChangedVms() {
        return changedVms;
    }

}
