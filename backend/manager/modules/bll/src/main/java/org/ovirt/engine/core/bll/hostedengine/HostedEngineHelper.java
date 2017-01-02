package org.ovirt.engine.core.bll.hostedengine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.SetHaMaintenanceModeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsSpmIdMapDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.ovirt_host_deploy.constants.Const;
import org.ovirt.ovirt_host_deploy.constants.HostedEngineEnv;

public class HostedEngineHelper {

    private static final String HE_CONF_HOST_ID = "host_id";

    private VM hostedEngineVm;
    private StorageDomainStatic storageDomainStatic;

    @Inject
    private VmDao vmDao;

    @Inject
    private VmStaticDao vmStaticDao;

    @Inject
    private VdsSpmIdMapDao vdsSpmIdMapDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private HostedEngineConfigFetcher hostedEngineConfigFetcher;

    @Inject
    private VDSBrokerFrontend vdsBroker;

    @Inject
    private VmHandler vmHandler;

    @PostConstruct
    private void init() {
        List<VmStatic> byName = vmStaticDao.getAllByName(Config.getValue(ConfigValues.HostedEngineVmName));
        if (byName != null && !byName.isEmpty()) {
            VmStatic vmStatic = byName.get(0);
            hostedEngineVm = vmDao.get(vmStatic.getId());
            vmHandler.updateDisksFromDb(hostedEngineVm);
        }

        initHostedEngineStorageDomain();
    }

    public Map<String, String> createVdsDeployParams(Guid vdsId, HostedEngineDeployConfiguration.Action deployAction) {
        if (hostedEngineVm == null) {
            return Collections.emptyMap();
        }

        Map<String, String> params = new HashMap<>();
        params.put(HostedEngineEnv.ACTION, fromDeployAction(deployAction));
        if (HostedEngineDeployConfiguration.Action.DEPLOY == deployAction) {
            params.putAll(hostedEngineConfigFetcher.fetch());
            // This installation method will generate the host id for this HE host. This MUST be
            // set in the configuration other wise the agent won't be able to register itself to
            // the whiteboard and become a part of the cluster.
            params.put(HE_CONF_HOST_ID, String.valueOf(offerHostId(vdsId)));
        }
        return params;
    }

    protected String fromDeployAction(HostedEngineDeployConfiguration.Action deployAction) {
        switch (deployAction) {
        case DEPLOY:
            return Const.HOSTED_ENGINE_ACTION_DEPLOY;
        case UNDEPLOY:
            return Const.HOSTED_ENGINE_ACTION_REMOVE;
        }
        return Const.HOSTED_ENGINE_ACTION_NONE;
    }

    public boolean isVmManaged() {
        return hostedEngineVm != null && hostedEngineVm.isManagedVm();
    }

    /**
     * Offer the host id this data center allocated for this host in vds_spm_map. This effectively syncs
     * between the hosted engine HA identifier and vdsm's host ids that are used when locking storage domain for
     * monitoring.
     *
     * @return a numeric host id which identifies this host as part of hosted engine cluster
     */
    private int offerHostId(Guid vdsId) {
            return  vdsSpmIdMapDao.get(vdsId).getVdsSpmId();
    }

    public StorageDomainStatic getStorageDomain() {
        return storageDomainStatic;
    }

    private void initHostedEngineStorageDomain(){
        if(hostedEngineVm == null){
            return;
        }
        List<DiskImage> diskList = hostedEngineVm.getDiskList();
        if(diskList == null || diskList.isEmpty()){
            return;
        }
        DiskImage disk = diskList.get(0);
        List<StorageDomain> allStorageDomainsByImageId = storageDomainDao.getAllStorageDomainsByImageId(disk.getImageId());
        if(allStorageDomainsByImageId == null || allStorageDomainsByImageId.isEmpty()){
            return;
        }
        StorageDomain storageDomain = allStorageDomainsByImageId.get(0);
        storageDomainStatic = storageDomain == null ? null : storageDomain.getStorageStaticData();
    }

    /*
     * @return The Guid of the DC the engine VM is running under
     */
    public Guid getStoragePoolId() {
        return hostedEngineVm.getStoragePoolId();
    }

    /**
     * @return The Guid of Storage Domain of the engine VM
     */
    public Guid getStorageDomainId() {
        return getStorageDomain().getId();
    }

    /**
     * @return The Guid of the host running the engine VM
     */
    public Guid getRunningHostId() {
        return hostedEngineVm.getRunOnVds();
    }

    public boolean updateHaLocalMaintenanceMode(VDS vds, boolean localMaintenance){
        SetHaMaintenanceModeVDSCommandParameters param
                = new SetHaMaintenanceModeVDSCommandParameters(vds, HaMaintenanceMode.LOCAL, localMaintenance);
        return vdsBroker.runVdsCommand(VDSCommandType.SetHaMaintenanceMode, param).getSucceeded();
    }


    /**
     * Checks, if there are hosts in the cluster
     * capable to run HE VM.
     *
     * @param clusterVdses candidates to select from.
     * @param vdses Guids of VDSes to be excluded from the candidates list.
     * @return True if there are any hosts for HE VM, False otherwise
     */
    public static boolean haveHostsAvailableforHE(Collection<VDS> clusterVdses,  final Iterable<Guid> vdses) {
        // It is really hard to query Iterable
        // especially when you have old commons-collections
        // So let's convert it to the set.
        Set<Guid> vdsIds = new HashSet<>();
        vdses.forEach(vdsIds::add);
        return  clusterVdses.stream()
                .filter(v -> !vdsIds.contains(v.getId())) // Remove other hosts in batch
                .filter(VDS::getHighlyAvailableIsConfigured) // Remove non HE hosts
                .filter(VDS::getHighlyAvailableIsActive) // Remove non-active HE hosts
                .filter(v -> !v.getHighlyAvailableLocalMaintenance()) // Remove HE hosts under maintenance
                .filter(v -> v.getHighlyAvailableScore() > 0) // Remove HE hosts not suitable for the engine VM
                .findAny()
                .isPresent();
    }
}
