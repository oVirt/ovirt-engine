package org.ovirt.engine.core.bll.snapshots;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.vm.VnicProfileHelper;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.ovf.VMStaticOvfLogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Snapshot} manager is used to easily add/update/remove snapshots.
 */
@Singleton
public class SnapshotsManager {
    private static final Logger log = LoggerFactory.getLogger(SnapshotsManager.class);

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    @Inject
    private VmDeviceDao vmDeviceDao;

    @Inject
    private BaseDiskDao baseDiskDao;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VmStaticDao vmStaticDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private DiskVmElementDao diskVmElementDao;

    @Inject
    private DiskDao diskDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private QuotaDao quotaDao;

    @Inject
    private VmHandler vmHandler;

    @Inject
    private OvfManager ovfManager;

    @Inject
    private ImagesHandler imagesHandler;

    @Inject
    private ClusterUtils clusterUtils;

    @Inject
    private VmNicDao vmNicDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private OvfHelper ovfHelper;

    /**
     * Save an active snapshot for the VM, without saving the configuration.<br>
     * The snapshot is created with the given status {@link SnapshotStatus}.
     *
     * @param snapshotId
     *            The ID for the snapshot.
     * @param vm
     *            The VM to save the snapshot for.
     * @param snapshotStatus
     *            The initial status of the snapshot
     * @param memoryDumpDiskId
     *            The memory dump disk ID
     * @param memoryConfDiskId
     *            The memory metadata disk ID
     * @param compensationContext
     *            Context for saving compensation details.
     */
    public Snapshot addActiveSnapshot(Guid snapshotId,
            VM vm,
            SnapshotStatus snapshotStatus,
            Guid memoryDumpDiskId,
            Guid memoryConfDiskId,
            final CompensationContext compensationContext) {
        return addActiveSnapshot(snapshotId,
                vm,
                snapshotStatus,
                memoryDumpDiskId,
                memoryConfDiskId,
                null,
                null,
                compensationContext);
    }

    /**
     * Save an active snapshot for the VM, without saving the configuration.<br>
     * The snapshot is created with the given status {@link SnapshotStatus}.
     *
     * @param snapshotId
     *            The ID for the snapshot.
     * @param vm
     *            The VM to save the snapshot for.
     * @param snapshotStatus
     *            The initial status of the snapshot
     * @param memoryDumpDiskId
     *            The memory dump disk ID
     * @param memoryConfDiskId
     *            The memory metadata disk ID
     * @param creationDate
     *            predefined creation date for the snapshot, null indicates 'now'
     * @param disks
     *            The disks contained in the snapshot
     * @param compensationContext
     *            Context for saving compensation details.
     */
    public Snapshot addActiveSnapshot(Guid snapshotId,
            VM vm,
            SnapshotStatus snapshotStatus,
            Guid memoryDumpDiskId,
            Guid memoryConfDiskId,
            Date creationDate,
            List<DiskImage> disks,
            final CompensationContext compensationContext) {
        return addSnapshot(snapshotId,
                "Active VM",
                snapshotStatus,
                SnapshotType.ACTIVE,
                vm,
                false,
                null,
                memoryDumpDiskId,
                memoryConfDiskId,
                creationDate,
                disks,
                null,
                compensationContext);
    }

    /**addSnapshot
     * Save snapshot to DB with compensation data.
     *
     * @param snapshotId
     *            The snapshot ID.
     * @param description
     *            The snapshot description.
     * @param snapshotStatus
     *            The snapshot status.
     * @param snapshotType
     *            The snapshot type.
     * @param vm
     *            The VM to link to & save configuration for (if necessary).
     * @param saveVmConfiguration
     *            Should VM configuration be generated and saved?
     * @param Set<String> changedFields
     *            Fields changed (applicable for next run)
     * @param memoryDumpDiskId
     *            The memory dump disk ID
     * @param memoryConfDiskId
     *            The memory metadata disk ID
     * @param creationDate
     *            predefined creation date for the snapshot, null indicates 'now'
     * @param disks
     *            The disks contained in the snapshot
     * @param vmDevices
     *            The devices contained in the snapshot
     * @param compensationContext
     *            In case compensation is needed.
     *
     * @return the saved snapshot
     */
    public Snapshot addSnapshot(Guid snapshotId,
            String description,
            SnapshotStatus snapshotStatus,
            SnapshotType snapshotType,
            VM vm,
            boolean saveVmConfiguration,
            Set<String> changedFields,
            Guid memoryDumpDiskId,
            Guid memoryConfDiskId,
            Date creationDate,
            List<DiskImage> disks,
            Map<Guid, VmDevice> vmDevices,
            final CompensationContext compensationContext
            ) {
        final Snapshot snapshot = new Snapshot(snapshotId,
                snapshotStatus,
                vm.getId(),
                saveVmConfiguration ? generateVmConfiguration(vm, disks, vmDevices) : null,
                snapshotType,
                description,
                creationDate != null ? creationDate : new Date(),
                vm.getAppList(),
                memoryDumpDiskId,
                memoryConfDiskId,
                changedFields);

        CompensationUtils.saveEntity(snapshot, snapshotDao, compensationContext);
        return snapshot;
    }

    /**
     * Generate a string containing the given VM's configuration.
     *
     * @param vm
     *            The VM to generate configuration from.
     * @return A String containing the VM configuration.
     */
    private String generateVmConfiguration(VM vm, List<DiskImage> disks, Map<Guid, VmDevice> vmDevices) {
        if (vm.getInterfaces() == null || vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(vmNetworkInterfaceDao.getAllForVm(vm.getId()));
        }

        if (StringUtils.isEmpty(vm.getVmtName())) {
            VmTemplate t = vmTemplateDao.get(vm.getVmtGuid());
            vm.setVmtName(t.getName());
        }

        if (vmDevices == null) {
            vmDeviceUtils.setVmDevices(vm.getStaticData());
        } else {
            vm.getStaticData().setManagedDeviceMap(vmDevices);
        }

        if (disks == null) {
            disks = DisksFilter.filterImageDisks(diskDao.getAllForVm(vm.getId()), ONLY_SNAPABLE, ONLY_ACTIVE);
            disks.addAll(imagesHandler.getCinderLeafImages(diskDao.getAllForVm(vm.getId())));
            disks.addAll(imagesHandler.getManagedBlockStorageSnapshots(diskDao.getAllForVm(vm.getId())));
        }
        populateDisksWithVmData(disks, vm.getId());
        disks.forEach(image -> image.setStorageIds(null));
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(vm);
        fullEntityOvfData.setDiskImages(disks);
        addVmExternalData(fullEntityOvfData.getVmExternalData(), vm);

        if (vm.getStaticData().getVmInit() == null) {
            vmHandler.updateVmInitFromDB(vm.getStaticData(), true);
        }
        return ovfManager.exportVm(vm,
                fullEntityOvfData,
                clusterUtils.getCompatibilityVersion(vm));
    }

    private void populateDisksWithVmData(List<? extends Disk> disks, Guid vmId) {
        for (Disk disk : disks) {
            DiskVmElement dve = diskVmElementDao.get(new VmDeviceId(disk.getId(), vmId));
            disk.setDiskVmElements(Collections.singletonList(dve));
        }
    }

    private void addVmExternalData(Map<VmExternalDataKind, String> vmExternalData, VM vm) {
        if (VmDeviceCommonUtils.isVmDeviceExists(vm.getStaticData().getManagedDeviceMap(), VmDeviceType.TPM)) {
            String tpmData = vmDao.getTpmData(vm.getId()).getFirst();
            if (tpmData != null && !tpmData.equals("")) {
                vmExternalData.put(VmExternalDataKind.TPM, tpmData);
            }
        }
        if (vm.getBiosType() == BiosType.Q35_SECURE_BOOT) {
            String nvramData = vmDao.getNvramData(vm.getId()).getFirst();
            if (nvramData != null && !nvramData.equals("")) {
                vmExternalData.put(VmExternalDataKind.NVRAM, nvramData);
            }
        }
    }

    /**
     * Remove all the snapshots that belong to the given VM.
     *
     * @param vmId
     *            The ID of the VM.
     * @return Set of the snapshots that were removed
     */
    public List<Snapshot> removeSnapshots(Guid vmId) {
        final List<Snapshot> vmSnapshots = snapshotDao.getAll(vmId);
        for (Snapshot snapshot : vmSnapshots) {
            snapshotDao.remove(snapshot.getId());
        }
        return vmSnapshots;
    }

    /**
     * Remove all illegal disks which were associated with the given snapshot. This is done in order to be able to
     * switch correctly between snapshots where illegal images might be present.
     *
     * @param vmId
     *            The vm ID the disk is associated with.
     * @param snapshotId
     *            The ID of the snapshot for who to remove illegal images for.
     */
    public void removeAllIllegalDisks(Guid snapshotId, Guid vmId) {
        for (DiskImage diskImage : diskImageDao.getAllSnapshotsForVmSnapshot(snapshotId)) {
            if (diskImage.getImageStatus() == ImageStatus.ILLEGAL) {
                imagesHandler.removeDiskImage(diskImage, vmId);
            }
        }
    }

    public boolean canRestoreVmConfigurationFromSnapshot(VM vm,
           Snapshot snapshot,
           VmInterfaceManager vmInterfaceManager) {
        if (snapshot.getVmConfiguration() == null) {
            return false;
        }

        VM tempVM = new VM();
        if (vm.getDynamicData() != null) {
            tempVM.setDynamicData(vm.getDynamicData());
        }
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(tempVM);
        try {
            ovfManager.importVm(snapshot.getVmConfiguration(), tempVM, fullEntityOvfData);
        } catch (OvfReaderException e) {
            log.error("Failed to import VM from the configuration '{}': {}",
                    snapshot.getVmConfiguration(),
                    e.getMessage());
            log.debug("Exception", e);
            return false;
        }
        boolean macsInSnapshotAreExpectedToBeAlreadyAllocated = SnapshotType.STATELESS.equals(snapshot.getType());
        return canSynchronizeNics(vm,
                vmInterfaceManager,
                fullEntityOvfData.getInterfaces(),
                macsInSnapshotAreExpectedToBeAlreadyAllocated);
    }

    /**
     * Attempt to read the configuration that is stored in the snapshot, and restore the VM from it.<br>
     * The NICs and Disks will be restored from the configuration (if available).<br>
     * <br>
     * <b>Note:</b>If the configuration is <code>null</code> or can't be decoded, then the VM configuration will remain
     * as it was but the underlying storage would still have changed..
     *
     * @param snapshot
     *            The snapshot containing the configuration.
     * @param user
     *            The user that performs the action
     * @param vmInterfaceManager vmInterfaceManager instance
     */
    public void attemptToRestoreVmConfigurationFromSnapshot(VM vm,
            Snapshot snapshot,
            Guid activeSnapshotId,
            List<DiskImage> images,
            CompensationContext compensationContext,
            DbUser user,
            VmInterfaceManager vmInterfaceManager,
            boolean withMemory) {
        boolean vmUpdatedFromConfiguration = false;
        if (snapshot.getVmConfiguration() != null) {
            vmUpdatedFromConfiguration = updateVmFromConfiguration(vm, snapshot.getVmConfiguration());

            if (images != null) {
                vmUpdatedFromConfiguration &= updateImagesByConfiguration(vm, images);
            }
        }

        if (!vmUpdatedFromConfiguration) {
            if (images == null) {
                images = diskImageDao.getAllSnapshotsForVmSnapshot(snapshot.getId());
            }
            vm.setImages(new ArrayList<>(images));
        }

        vm.setAppList(snapshot.getAppList());
        vmDynamicDao.update(vm.getDynamicData());

        List<DiskImage> imagesToExclude = diskImageDao.getAttachedDiskSnapshotsToVm(vm.getId(), Boolean.TRUE);

        List<DiskImage> diskImagesToSyncFromSnapshot = (images == null || imagesToExclude.isEmpty()) ?
                vm.getImages() : images;
        synchronizeDisksFromSnapshot(vm.getId(), snapshot.getId(), activeSnapshotId,
                diskImagesToSyncFromSnapshot, vm.getName());

        if (vmUpdatedFromConfiguration) {
            vmStaticDao.update(vm.getStaticData());
            boolean macsInSnapshotAreExpectedToBeAlreadyAllocated = SnapshotType.STATELESS.equals(snapshot.getType());
            synchronizeNics(vm,
                    compensationContext,
                    user,
                    vmInterfaceManager,
                    macsInSnapshotAreExpectedToBeAlreadyAllocated);

            for (VmDevice vmDevice : vmDeviceDao.getVmDeviceByVmId(vm.getId())) {
                if (deviceCanBeRemoved(vmDevice)) {
                    vmDeviceDao.remove(vmDevice.getId());
                }
            }

            vmDeviceUtils.addImportedDevices(vm.getStaticData(), false, withMemory);
            vmDeviceUtils.updateVmExternalData(vm);
        }
    }

    private boolean updateImagesByConfiguration(VM vm, List<DiskImage> images) {
        Map<Guid, VM> snapshotVmConfigurations = new HashMap<>();
        ArrayList<DiskImage> imagesFromVmConf = new ArrayList<>();

        for (DiskImage image : images) {
            Guid vmSnapshotId = image.getVmSnapshotId();
            VM vmFromConf = snapshotVmConfigurations.get(vmSnapshotId);
            if (vmFromConf == null) {
                vmFromConf = new VM();
                Snapshot snapshot = snapshotDao.get(image.getVmSnapshotId());
                if (!updateVmFromConfiguration(vmFromConf, snapshot.getVmConfiguration())) {
                    return false;
                }
                snapshotVmConfigurations.put(vmSnapshotId, vmFromConf);
            }

            for (DiskImage imageFromVmConf : vmFromConf.getImages()) {
                if (imageFromVmConf.getId().equals(image.getId())) {
                    imageFromVmConf.setStorageIds(image.getStorageIds());
                    imagesFromVmConf.add(imageFromVmConf);
                    break;
                }
            }
        }

        vm.setImages(imagesFromVmConf);

        return true;
    }

    /**
     * @return true if the device can be removed (disk which allows snapshot can be removed as it is part
     * of the snapshot. Other disks shouldn't be removed as they are not part of the snapshot).
     */
    private boolean deviceCanBeRemoved(VmDevice vmDevice) {
        if (!vmDevice.getDevice().equals(VmDeviceType.DISK.getName()) || !vmDevice.isManaged()) {
            return true;
        }

        return vmDevice.getSnapshotId() == null && diskDao.get(vmDevice.getDeviceId()).isAllowSnapshot();
    }

    /**
     * Update the given VM with the (static) data that is contained in the configuration. The {@link VM#getImages()}
     * will contain the images that were read from the configuration.
     *
     * @param vm
     *            The VM to update.
     * @param configuration
     *            The configuration to update from.
     * @return In case of a problem reading the configuration, <code>false</code>. Otherwise, <code>true</code>.
     */
    public boolean updateVmFromConfiguration(VM vm, String configuration) {

        try {
            VmStatic oldVmStatic = vm.getStaticData();
            VM tempVM = new VM();
            if (vm.getDynamicData() != null) {
                tempVM.setDynamicData(vm.getDynamicData());
            }
            FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(tempVM);
            ovfManager.importVm(configuration, tempVM, fullEntityOvfData);
            for (DiskImage diskImage : fullEntityOvfData.getDiskImages()) {
                DiskImage dbImage = diskImageDao.getSnapshotById(diskImage.getImageId());
                if (dbImage != null) {
                    diskImage.setStorageIds(dbImage.getStorageIds());
                }
            }
            new VMStaticOvfLogHandler(tempVM.getStaticData()).resetDefaults(oldVmStatic);

            vm.setStaticData(tempVM.getStaticData());
            IconUtils.preserveIcons(vm.getStaticData(), oldVmStatic);
            vm.setImages((ArrayList<DiskImage>) fullEntityOvfData.getDiskImages());
            vm.setInterfaces(fullEntityOvfData.getInterfaces());
            vm.setVmExternalData(fullEntityOvfData.getVmExternalData());

            // These fields are not saved in the OVF, so get them from the current VM.
            vm.setIsoPath(oldVmStatic.getIsoPath());
            vm.setCpuProfileId(oldVmStatic.getCpuProfileId());
            vm.setClusterId(oldVmStatic.getClusterId());

            // The VM configuration does not hold the vds group Id.
            // It is necessary to fetch the vm static from the Db, in order to get this information
            VmStatic vmStaticFromDb = vmStaticDao.get(vm.getId());
            if (vmStaticFromDb != null) {
                Cluster cluster = clusterDao.get(vmStaticFromDb.getClusterId());
                if (cluster != null) {
                    vm.setStoragePoolId(cluster.getStoragePoolId());
                    vm.setClusterCompatibilityVersion(cluster.getCompatibilityVersion());
                    vm.setClusterName(cluster.getName());
                    vm.setClusterCpuName(cluster.getCpuName());
                    vm.setClusterBiosType(cluster.getBiosType());
                }
            }
            // if the required dedicated host is invalid -> use current VM dedicated host
            if (!vmHandler.validateDedicatedVdsExistOnSameCluster(vm.getStaticData()).isValid()) {
                vm.setDedicatedVmForVdsList(oldVmStatic.getDedicatedVmForVdsList());
            }
            // The snapshot may have unsupported compatibility version.
            // In that case, using the lowest supported version,
            // otherwise the line below would try to access non-existing
            // config value and throw an exception.
            Version supportedVersion = Version.getLowest().greater(vm.getCompatibilityVersion()) ?
                    Version.getLowest() :
                    vm.getCompatibilityVersion();

            vmHandler.updateMaxMemorySize(vm.getStaticData(), supportedVersion);

            validateQuota(vm);
            return true;
        } catch (OvfReaderException e) {
            log.error("Failed to update VM from the configuration '{}': {}",
                    configuration,
                    e.getMessage());
            log.debug("Exception", e);
            return false;
        }
    }

    public Optional<VM> getVmConfigurationInStatelessSnapshotOfVm(Guid vmId) {
        Snapshot snapshot = snapshotDao.get(vmId, SnapshotType.STATELESS);

        if (snapshot == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(ovfHelper.readVmFromOvf(snapshot.getVmConfiguration()).getVm());
        } catch (OvfReaderException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validate whether the quota supplied in snapshot configuration exists in<br>
     * current setup, if not reset to null.<br>
     *
     * @param vm
     *            imported vm
     */
    private void validateQuota(VM vm) {
        if (vm.getQuotaId() != null) {
            Quota quota = quotaDao.getById(vm.getQuotaId());
            if (quota == null) {
                vm.setQuotaId(null);
            }
        }
    }

    private boolean canSynchronizeNics(VM snapshotedVm,
           VmInterfaceManager vmInterfaceManager,
           List<VmNetworkInterface> interfaces,
           boolean macsInSnapshotAreExpectedToBeAlreadyAllocated) {
        MacPool macPool = vmInterfaceManager.getMacPool();
        List<VmNic> dbNics = vmNicDao.getAllForVm(snapshotedVm.getId());
        return new SyncMacsOfDbNicsWithSnapshot(macPool, auditLogDirector, macsInSnapshotAreExpectedToBeAlreadyAllocated)
                .canSyncNics(dbNics, interfaces);
    }

    /**
     * Synchronize the VM's {@link VmNetworkInterface}s with the ones from the snapshot.<br>
     * All existing NICs will be deleted, and the ones from the snapshot re-added.<br>
     * In case a MAC address is already in use, the user will be issued a warning in the audit log.
     *
     * @param user
     *            The user that performs the action
     * @param vmInterfaceManager vmInterfaceManager instance
     */
    private void synchronizeNics(VM snapshotedVm,
            CompensationContext compensationContext,
            DbUser user,
            VmInterfaceManager vmInterfaceManager,
            boolean macsInSnapshotAreExpectedToBeAlreadyAllocated) {
        VnicProfileHelper vnicProfileHelper =
                new VnicProfileHelper(snapshotedVm.getClusterId(),
                        snapshotedVm.getStoragePoolId(),
                        AuditLogType.IMPORTEXPORT_SNAPSHOT_VM_INVALID_INTERFACES);

        MacPool macPool = vmInterfaceManager.getMacPool();

        /*what is at moment of calling this in DB are data related to (stateless) VM being updated/overwritten by
         * snapshot data.
         */
        List<VmNic> dbNics = vmNicDao.getAllForVm(snapshotedVm.getId());

        /*
         * while snapshotedVm.getInterfaces() are interfaces taken from VM passed into here via parameter. This instance originates from same DB
         * record, but it was updated with ovf snapshot, so at the moment of calling this, VM is filled with data to
         * which we need to revert for example stateless VM being stopped.
         */
        new SyncMacsOfDbNicsWithSnapshot(macPool, auditLogDirector, macsInSnapshotAreExpectedToBeAlreadyAllocated)
                .sync(dbNics, snapshotedVm.getInterfaces());

        vmInterfaceManager.removeAll(dbNics);
        for (VmNetworkInterface vmInterface : snapshotedVm.getInterfaces()) {
            vmInterface.setVmId(snapshotedVm.getId());
            // These fields might not be saved in the OVF, so fill them with reasonable values.
            if (vmInterface.getId() == null) {
                vmInterface.setId(Guid.newGuid());
            }

            vnicProfileHelper.updateNicWithVnicProfileForUser(vmInterface, user);

            vmInterfaceManager.persistIface(vmInterface, compensationContext);
        }

        vnicProfileHelper.auditInvalidInterfaces(snapshotedVm.getName());
    }

    /**
     * Synchronize the VM's Disks with the images from the snapshot:<br>
     * <ul>
     * <li>Existing disks are updated.</li>
     * <li>Disks that don't exist anymore get re-added.</li>
     * <ul>
     * <li>If the image is still in the DB, the disk is linked to it.</li>
     * <li>If the image is not in the DB anymore, the disk will be marked as "broken"</li>
     * </ul>
     * </ul>
     *
     * @param vmId
     *            The VM ID is needed to re-add disks.
     * @param snapshotId
     *            The snapshot ID is used to find only the VM disks at the time.
     * @param disksFromSnapshot
     *            The disks that existed in the snapshot.
     */
    private void synchronizeDisksFromSnapshot(Guid vmId,
            Guid snapshotId,
            Guid activeSnapshotId,
            List<DiskImage> disksFromSnapshot,
            String vmName) {
        List<Guid> diskIdsFromSnapshot = new ArrayList<>();

        // Sync disks that exist or existed in the snapshot.
        int count = 1;
        for (DiskImage diskImage : disksFromSnapshot) {
            diskIdsFromSnapshot.add(diskImage.getId());
            if (baseDiskDao.exists(diskImage.getId())) {
                baseDiskDao.update(diskImage);
                DiskVmElement dve = diskVmElementDao.get(diskImage.getDiskVmElementForVm(vmId).getId());
                if (dve != null && !dve.equals(diskImage.getDiskVmElementForVm(vmId))) {
                    diskVmElementDao.update(diskImage.getDiskVmElementForVm(vmId));
                }

            } else {
                // If can't find the image, insert it as illegal so that it can't be used and make the device unplugged.
                if (diskImageDao.getSnapshotById(diskImage.getImageId()) == null) {
                    diskImage.setImageStatus(ImageStatus.ILLEGAL);
                    diskImage.setVmSnapshotId(activeSnapshotId);

                    imagesHandler.addImage(diskImage, true, (diskImage.getStorageIds() == null) ? null :
                            new ImageStorageDomainMap(diskImage.getImageId(),
                                    diskImage.getStorageIds().get(0),
                                    diskImage.getQuotaId(),
                                    diskImage.getDiskProfileId()));
                }
                imagesHandler.addDiskToVm(diskImage, vmId);
            }
            diskImage.setDiskAlias(imagesHandler.getSuggestedDiskAlias(diskImage, vmName, count));
            count++;
        }
        removeDisksNotInSnapshot(vmId, diskIdsFromSnapshot);
    }

    /**
     * Remove all the disks which are allowed to be snapshot but not exist in the snapshot and are not disk snapshots
     * @param vmId - The vm id which is being snapshot.
     * @param diskIdsFromSnapshot - An image group id list for images which are part of the VM.
     */
    private void removeDisksNotInSnapshot(Guid vmId, List<Guid> diskIdsFromSnapshot) {
        for (VmDevice vmDevice : vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId, VmDeviceGeneralType.DISK, VmDeviceType.DISK)) {
            if (!diskIdsFromSnapshot.contains(vmDevice.getDeviceId()) && vmDevice.getSnapshotId() == null) {
                Disk disk = diskDao.get(vmDevice.getDeviceId());
                if (disk != null && disk.isAllowSnapshot()) {
                    baseDiskDao.remove(vmDevice.getDeviceId());
                    vmDeviceDao.remove(vmDevice.getId());
                }
            }
        }
    }

    /**
     *
     * @param vmId id of VM
     * @return Stream of MACs used in template if such template exist, otherwise empty stream.
     */
    public Stream<String> macsInStatelessSnapshot(Guid vmId) {
        Optional<VM> originalSnapshot = getVmConfigurationInStatelessSnapshotOfVm(vmId);
        Optional<List<VmNetworkInterface>> originalSnapshotNetworkInterfaces = originalSnapshot.map(VM::getInterfaces);
        return originalSnapshotNetworkInterfaces.orElse(Collections.emptyList()).stream().map(VmNic::getMacAddress);
    }
}
