package org.ovirt.engine.core.bll.hostedengine;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.SetHaMaintenanceModeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsSpmIdMapDao;
import org.ovirt.engine.core.dao.VmDao;

public class HostedEngineHelper {
    private VM hostedEngineVm;
    private StorageDomainStatic storageDomainStatic;

    @Inject
    private VmDao vmDao;

    @Inject
    private VdsSpmIdMapDao vdsSpmIdMapDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private VDSBrokerFrontend vdsBroker;

    @Inject
    private VmHandler vmHandler;

    @PostConstruct
    private void init() {
        List<OriginType> hostedEngineOriginTypes = Arrays.asList(
                OriginType.HOSTED_ENGINE,
                OriginType.MANAGED_HOSTED_ENGINE);
        List<VM> hostedEngineVms = vmDao.getVmsByOrigins(hostedEngineOriginTypes);
        hostedEngineVms.stream().findFirst().ifPresent(this::setHostedEngineVm);

        initHostedEngineStorageDomain();
    }

    private void setHostedEngineVm(VM hostedEngineVm) {
        vmHandler.updateDisksFromDb(hostedEngineVm);
        this.hostedEngineVm = hostedEngineVm;
    }

    public boolean isVmManaged() {
        return hostedEngineVm != null && hostedEngineVm.isManagedVm();
    }

    /**
     * Offer the host id this data center allocated for this host in vds_spm_map. This effectively syncs between the
     * hosted engine HA identifier and vdsm's host ids that are used when locking storage domain for monitoring.
     *
     * @return a numeric host id which identifies this host as part of hosted engine cluster
     */
    public int offerHostId(Guid vdsId) {
        return vdsSpmIdMapDao.get(vdsId).getVdsSpmId();
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

    public Guid getClusterId() {
        return hostedEngineVm.getClusterId();
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
    public static boolean haveHostsAvailableForHE(Collection<VDS> clusterVdses, final Iterable<Guid> vdses) {
        // It is really hard to query Iterable
        // especially when you have old commons-collections
        // So let's convert it to the set.
        Set<Guid> vdsIds = new HashSet<>();
        vdses.forEach(vdsIds::add);
        return clusterVdses.stream()
                .filter(v -> !vdsIds.contains(v.getId())) // Remove other hosts in batch
                .filter(VDS::getHighlyAvailableIsConfigured) // Remove non HE hosts
                .filter(VDS::getHighlyAvailableIsActive) // Remove non-active HE hosts
                .filter(v -> !v.getHighlyAvailableLocalMaintenance()) // Remove HE hosts under maintenance
                .anyMatch(v -> v.getHighlyAvailableScore() > 0);
    }
}
