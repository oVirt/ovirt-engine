package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVmStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * in-charge of fetching vms list together with the db counter-part
 * and store for analysis on VdsManager
 */
public class VmsListFetcher {

    protected VdsManager vdsManager;
    protected List<Pair<VM, VmInternalData>> changedVms;
    private List<Pair<VM, VmInternalData>> devicesChangedVms;
    protected Map<Guid, VmInternalData> vdsmVms;
    private Map<Guid, VM> dbVms;

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

    public void fetch() {
        VDSReturnValue getList =
                getResourceManager().runVdsCommand(
                        VDSCommandType.List,
                        new VdsIdAndVdsVDSCommandParametersBase(vdsManager.getCopyVds()));
        if (getList.getSucceeded()) {
            vdsmVms = (Map<Guid, VmInternalData>) getList.getReturnValue();
            onFetchVms();
        } else {
            onError();
        }
    }

    protected void onFetchVms() {
        dbVms = getVmDao().getAllRunningByVds(vdsManager.getVdsId());
        changedVms = new ArrayList<>();
        devicesChangedVms = new ArrayList<>();
        filterVms();
        gatherNonRunningVms(dbVms);
        vdsManager.setLastVmsList(changedVms);
    }

    protected void onError() {
        dbVms = Collections.emptyMap();
        vdsmVms = Collections.emptyMap();
    }

    protected void filterVms() {
        for (VmInternalData vdsmVm : vdsmVms.values()) {
            VM dbVm = dbVms.get(vdsmVm.getVmDynamic().getId());

            gatherChangedVms(dbVm, vdsmVm);
            gatherVmsWithChangedDevices(dbVm, vdsmVm);
        }
    }

    protected void gatherChangedVms(VM dbVm, VmInternalData vdsmVm) {
        if (statusChanged(dbVm, vdsmVm.getVmDynamic())) {
            VDSReturnValue vmStats =
                    getResourceManager().runVdsCommand(
                            VDSCommandType.GetVmStats,
                            new GetVmStatsVDSCommandParameters(vdsManager.getCopyVds(), vdsmVm.getVmDynamic().getId()));
            if (vmStats.getSucceeded()) {
                changedVms.add(new Pair<>(dbVm, (VmInternalData) vmStats.getReturnValue()));
            } else {
                if (dbVm != null) {
                    log.error(
                            "failed to fetch VM '{}' stats. status remain unchanged ({})",
                            dbVm.getName(),
                            dbVm.getStatus());
                }
            }
        }
    }

    private void gatherVmsWithChangedDevices(VM dbVm, VmInternalData vdsmVm) {
        if (isDevicesChanged(dbVm, vdsmVm)) {
            devicesChangedVms.add(new Pair<>(dbVm, vdsmVm));
        }
    }


    private void gatherNonRunningVms(Map<Guid, VM> dbVms) {
        for (VM dbVm : dbVms.values()) {
            if (!vdsmVms.containsKey(dbVm.getId())) {
                // non running vms are also treated as changed VMs
                changedVms.add(new Pair<VM, VmInternalData>(dbVm, null));
            }
        }
    }

    private boolean statusChanged(VM dbVm, VmDynamic vdsmVm) {
        return dbVm == null || (dbVm.getStatus() != vdsmVm.getStatus());
    }

    private boolean isDevicesChanged(VM dbVm, VmInternalData vdsmVm) {
        // Update only running VMs
        VmDynamic vdsmVmDynamic = vdsmVm.getVmDynamic();
        return
                vdsmVmDynamic != null &&
                vdsmVmDynamic.getStatus() != VMStatus.MigratingTo &&
                vdsmVmDynamic.getHash() != null &&
                dbVm != null &&
                !Objects.equals(dbVm.getHash(), vdsmVmDynamic.getHash());
    }

    public VmDAO getVmDao() {
        return dbFacade.getVmDao();
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public List<Pair<VM, VmInternalData>> getChangedVms() {
        return changedVms;
    }

    public List<Pair<VM, VmInternalData>> getVmsWithChangedDevices() {
        return devicesChangedVms;
    }
}
