package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.vm.VnicProfileHelper;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
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
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.ovf.VMStaticOvfLogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Snapshot} manager is used to easily add/update/remove snapshots.
 */
public class SnapshotsManager {
    private static final Logger log = LoggerFactory.getLogger(SnapshotsManager.class);

    /**
     * Save an active snapshot for the VM, without saving the configuration.<br>
     * The snapshot is created in status {@link SnapshotStatus#OK} by default.
     *
     * @param snapshotId
     *            The ID for the snapshot.
     * @param vm
     *            The VM to save the snapshot for.
     * @param compensationContext
     *            Context for saving compensation details.
     * @return the newly created snapshot
     * @see #addActiveSnapshot(Guid, VM, SnapshotStatus, CompensationContext)
     */
    public Snapshot addActiveSnapshot(Guid snapshotId,
            VM vm,
            final CompensationContext compensationContext) {
        return addActiveSnapshot(snapshotId,
                vm,
                SnapshotStatus.OK,
                "",
                null,
                compensationContext);
    }

    /**
     * Save an active snapshot for the VM, without saving the configuration.<br>
     * The snapshot is created in status {@link SnapshotStatus#OK} by default.
     *
     * @param snapshotId
     *            The ID for the snapshot.
     * @param vm
     *            The VM to save the snapshot for.
     * @param snapshotStatus
     *            The initial status of the created snapshot
     * @param compensationContext
     *            Context for saving compensation details.
     * @return the newly created snapshot
     * @see #addActiveSnapshot(Guid, VM, SnapshotStatus, CompensationContext)
     */
    public Snapshot addActiveSnapshot(Guid snapshotId,
            VM vm,
            SnapshotStatus snapshotStatus,
            final CompensationContext compensationContext) {
        return addActiveSnapshot(snapshotId,
                vm,
                snapshotStatus,
                "",
                null,
                compensationContext);
    }

    /**
     * Save an active snapshot for the VM, without saving the configuration.<br>
     * The snapshot is created in status {@link SnapshotStatus#OK} by default.
     *
     * @param snapshotId
     *            The ID for the snapshot.
     * @param vm
     *            The VM to save the snapshot for.
     * @param memoryVolume
     *            The memory state for the created snapshot
     * @param compensationContext
     *            Context for saving compensation details.
     * @return the newly created snapshot
     * @see #addActiveSnapshot(Guid, VM, SnapshotStatus, CompensationContext)
     */
    public Snapshot addActiveSnapshot(Guid snapshotId,
            VM vm,
            String memoryVolume,
            final CompensationContext compensationContext) {
        return addActiveSnapshot(snapshotId,
                vm,
                SnapshotStatus.OK,
                memoryVolume,
                null,
                compensationContext);
    }

    /**
     * Save an active snapshot for the VM, without saving the configuration.<br>
     * The snapshot is created in status {@link SnapshotStatus#OK} by default.
     *
     * @param snapshotId
     *            The ID for the snapshot.
     * @param vm
     *            The VM to save the snapshot for.
     * @param memoryVolume
     *            The memory state for the created snapshot
     * @param disks
     *            The disks contained in the snapshot
     * @param compensationContext
     *            Context for saving compensation details.
     * @return the newly created snapshot
     */
    public Snapshot addActiveSnapshot(Guid snapshotId,
                                      VM vm,
                                      String memoryVolume,
                                      List<DiskImage> disks,
                                      final CompensationContext compensationContext) {
        return addActiveSnapshot(snapshotId,
                vm,
                SnapshotStatus.OK,
                memoryVolume,
                disks,
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
     * @param compensationContext
     *            Context for saving compensation details.
     */
    public Snapshot addActiveSnapshot(Guid snapshotId,
                                      VM vm,
                                      SnapshotStatus snapshotStatus,
                                      String memoryVolume,
                                      final CompensationContext compensationContext) {
        return addActiveSnapshot(snapshotId,
                vm,
                snapshotStatus,
                memoryVolume,
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
     * @param disks
     *            The disks contained in the snapshot
     * @param compensationContext
     *            Context for saving compensation details.
     */
    public Snapshot addActiveSnapshot(Guid snapshotId,
            VM vm,
            SnapshotStatus snapshotStatus,
            String memoryVolume,
            List<DiskImage> disks,
            final CompensationContext compensationContext) {
        return addSnapshot(snapshotId,
                "Active VM",
                snapshotStatus,
                SnapshotType.ACTIVE,
                vm,
                false,
                memoryVolume,
                disks,
                compensationContext);
    }

    /**
     * Add a new snapshot, saving it to the DB (with compensation). The VM's current configuration (including Disks &
     * NICs) will be saved in the snapshot.<br>
     * The snapshot is created in status {@link SnapshotStatus#LOCKED} by default.
     *
     * @param snapshotId
     *            The ID for the snapshot.
     * @param description
     *            The snapshot description.
     * @param snapshotType
     *            The snapshot type.
     * @param vm
     *            The VM to save in configuration.
     * @param memoryVolume
     *            the volume in which the snapshot's memory is stored
     * @param compensationContext
     *            Context for saving compensation details.
     * @return the added snapshot
     */
    public Snapshot addSnapshot(Guid snapshotId,
            String description,
            SnapshotType snapshotType,
            VM vm,
            String memoryVolume,
            final CompensationContext compensationContext) {
        return addSnapshot(snapshotId, description, SnapshotStatus.LOCKED,
                snapshotType, vm, true, memoryVolume, null, compensationContext);
    }

    /**
     * Add a new snapshot, saving it to the DB (with compensation). The VM's current configuration (including Disks &
     * NICs) will be saved in the snapshot.<br>
     * The snapshot is created in status {@link SnapshotStatus#LOCKED} by default.
     *
     * @param snapshotId
     *            The ID for the snapshot.
     * @param description
     *            The snapshot description.
     * @param snapshotType
     *            The snapshot type.
     * @param vm
     *            The VM to save in configuration.
     * @param memoryVolume
     *            the volume in which the snapshot's memory is stored
     * @param disks
     *            The disks contained in the snapshot
     * @param compensationContext
     *            Context for saving compensation details.
     * @return the added snapshot
     */
    public Snapshot addSnapshot(Guid snapshotId,
                                String description,
                                SnapshotType snapshotType,
                                VM vm,
                                String memoryVolume,
                                List<DiskImage> disks,
                                final CompensationContext compensationContext) {
        return addSnapshot(snapshotId, description, SnapshotStatus.LOCKED,
                snapshotType, vm, true, memoryVolume, disks, compensationContext);
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
     * @param compensationContext
     *            In case compensation is needed.
     * @return the saved snapshot
     */
    public Snapshot addSnapshot(Guid snapshotId,
            String description,
            SnapshotStatus snapshotStatus,
            SnapshotType snapshotType,
            VM vm,
            boolean saveVmConfiguration,
            String memoryVolume,
            List<DiskImage> disks,
            final CompensationContext compensationContext) {
        return addSnapshot(snapshotId,
                description,
                snapshotStatus,
                snapshotType,
                vm,
                saveVmConfiguration,
                memoryVolume,
                disks,
                null,
                compensationContext);
    }

    public Snapshot addSnapshot(Guid snapshotId,
                                String description,
                                SnapshotStatus snapshotStatus,
                                SnapshotType snapshotType,
                                VM vm,
                                boolean saveVmConfiguration,
                                String memoryVolume,
                                List<DiskImage> disks,
                                Map<Guid, VmDevice> vmDevices,
                                final CompensationContext compensationContext) {
        final Snapshot snapshot = new Snapshot(snapshotId,
                snapshotStatus,
                vm.getId(),
                saveVmConfiguration ? generateVmConfiguration(vm, disks, vmDevices) : null,
                snapshotType,
                description,
                new Date(),
                vm.getAppList(),
                memoryVolume,
                MemoryUtils.getMemoryDiskId(memoryVolume),
                MemoryUtils.getMetadataDiskId(memoryVolume));

        getSnapshotDao().save(snapshot);
        compensationContext.snapshotNewEntity(snapshot);
        return snapshot;
    }

    /**
     * Generate a string containing the given VM's configuration.
     *
     * @param vm
     *            The VM to generate configuration from.
     * @return A String containing the VM configuration.
     */
    protected String generateVmConfiguration(VM vm, List<DiskImage> disks, Map<Guid, VmDevice> vmDevices) {
        if (vm.getInterfaces() == null || vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(getVmNetworkInterfaceDao().getAllForVm(vm.getId()));
        }

        if (StringUtils.isEmpty(vm.getVmtName())) {
            VmTemplate t = getVmTemplateDao().get(vm.getVmtGuid());
            vm.setVmtName(t.getName());
        }

        if (vmDevices == null) {
            VmDeviceUtils.setVmDevices(vm.getStaticData());
        } else {
            vm.getStaticData().setManagedDeviceMap(vmDevices);
        }

        if (disks == null) {
            disks = ImagesHandler.filterImageDisks(getDiskDao().getAllForVm(vm.getId()), false, true, true);
            disks.addAll(ImagesHandler.getCinderLeafImages(getDiskDao().getAllForVm(vm.getId()), false));
        }
        populateDisksWithVmData(disks, vm.getId());
        for (DiskImage image : disks) {
            image.setStorageIds(null);
        }
        return new OvfManager().exportVm(vm, new ArrayList<>(disks), ClusterUtils.getCompatibilityVersion(vm));
    }

    private void populateDisksWithVmData(List<? extends Disk> disks, Guid vmId) {
        for (Disk disk : disks) {
            DiskVmElement dve = getDiskVmElementDao().get(new VmDeviceId(disk.getId(), vmId));
            disk.setDiskVmElements(Collections.singletonList(dve));
        }
    }

    /**
     * Remove all the snapshots that belong to the given VM.
     *
     * @param vmId
     *            The ID of the VM.
     * @return Set of memoryVolumes of the removed snapshots
     */
    public Set<String> removeSnapshots(Guid vmId) {
        final List<Snapshot> vmSnapshots = getSnapshotDao().getAll(vmId);
        for (Snapshot snapshot : vmSnapshots) {
            getSnapshotDao().remove(snapshot.getId());
        }
        return MemoryUtils.getMemoryVolumesFromSnapshots(vmSnapshots);
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
        for (DiskImage diskImage : getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshotId)) {
            if (diskImage.getImageStatus() == ImageStatus.ILLEGAL) {
                ImagesHandler.removeDiskImage(diskImage, vmId);
            }
        }
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
    public void attempToRestoreVmConfigurationFromSnapshot(VM vm,
            Snapshot snapshot,
            Guid activeSnapshotId,
            List<DiskImage> images,
            CompensationContext compensationContext,
            DbUser user,
            VmInterfaceManager vmInterfaceManager) {
        boolean vmUpdatedFromConfiguration = false;
        if (snapshot.getVmConfiguration() != null) {
            vmUpdatedFromConfiguration = updateVmFromConfiguration(vm, snapshot.getVmConfiguration());

            if (images != null) {
                vmUpdatedFromConfiguration &= updateImagesByConfiguration(vm, images);
            }
        }

        if (!vmUpdatedFromConfiguration) {
            if (images == null) {
                images = getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshot.getId());
            }
            vm.setImages(new ArrayList<>(images));
        }

        vm.setAppList(snapshot.getAppList());
        getVmDynamicDao().update(vm.getDynamicData());
        synchronizeDisksFromSnapshot(vm.getId(), snapshot.getId(), activeSnapshotId, vm.getImages(), vm.getName());

        if (vmUpdatedFromConfiguration) {
            getVmStaticDao().update(vm.getStaticData());
            synchronizeNics(vm, compensationContext, user, vmInterfaceManager);

            for (VmDevice vmDevice : getVmDeviceDao().getVmDeviceByVmId(vm.getId())) {
                if (deviceCanBeRemoved(vmDevice)) {
                    getVmDeviceDao().remove(vmDevice.getId());
                }
            }

            VmDeviceUtils.addImportedDevices(vm.getStaticData(), false);
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
                Snapshot snapshot = getSnapshotDao().get(image.getVmSnapshotId());
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
        if (!vmDevice.getDevice().equals(VmDeviceType.DISK.getName()) || !vmDevice.getIsManaged()) {
            return true;
        }

        return vmDevice.getSnapshotId() == null && getDiskDao().get(vmDevice.getDeviceId()).isAllowSnapshot();
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
            ArrayList<DiskImage> images = new ArrayList<>();
            ArrayList<VmNetworkInterface> interfaces = new ArrayList<>();
            new OvfManager().importVm(configuration, tempVM, images, interfaces);
            for (DiskImage diskImage : images) {
                DiskImage dbImage = getDiskImageDao().getSnapshotById(diskImage.getImageId());
                if (dbImage != null) {
                    diskImage.setStorageIds(dbImage.getStorageIds());
                }
            }
            new VMStaticOvfLogHandler(tempVM.getStaticData()).resetDefaults(oldVmStatic);

            vm.setStaticData(tempVM.getStaticData());
            IconUtils.preserveIcons(vm.getStaticData(), oldVmStatic);
            vm.setImages(images);
            vm.setInterfaces(interfaces);

            // These fields are not saved in the OVF, so get them from the current VM.
            vm.setIsoPath(oldVmStatic.getIsoPath());
            vm.setCpuProfileId(oldVmStatic.getCpuProfileId());
            vm.setClusterId(oldVmStatic.getClusterId());
            vm.setClusterCompatibilityVersionOrigin(oldVmStatic.getClusterCompatibilityVersionOrigin());
            // The VM configuration does not hold the vds group Id.
            // It is necessary to fetch the vm static from the Db, in order to get this information
            VmStatic vmStaticFromDb = getVmStaticDao().get(vm.getId());
            if (vmStaticFromDb != null) {
                Cluster cluster = getClusterDao().get(vmStaticFromDb.getClusterId());
                if (cluster != null) {
                    vm.setStoragePoolId(cluster.getStoragePoolId());
                    vm.setClusterCompatibilityVersion(cluster.getCompatibilityVersion());
                    vm.setClusterName(cluster.getName());
                    vm.setClusterCpuName(cluster.getCpuName());
                }
            }
            // if the required dedicated host is invalid -> use current VM dedicated host
            if (!VmHandler.validateDedicatedVdsExistOnSameCluster(vm.getStaticData(), null)) {
                vm.setDedicatedVmForVdsList(oldVmStatic.getDedicatedVmForVdsList());
            }
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

    /**
     * Validate whether the quota supplied in snapshot configuration exists in<br>
     * current setup, if not reset to null.<br>
     *
     * @param vm
     *            imported vm
     */
    private void validateQuota(VM vm) {
        if (vm.getQuotaId() != null) {
            Quota quota = getQuotaDao().getById(vm.getQuotaId());
            if (quota == null) {
                vm.setQuotaId(null);
            }
        }
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
    private void synchronizeNics(VM vm,
            CompensationContext compensationContext,
            DbUser user,
            VmInterfaceManager vmInterfaceManager) {
        VnicProfileHelper vnicProfileHelper =
                new VnicProfileHelper(vm.getClusterId(),
                        vm.getStoragePoolId(),
                        AuditLogType.IMPORTEXPORT_SNAPSHOT_VM_INVALID_INTERFACES);

        vmInterfaceManager.removeAll(vm.getId());
        for (VmNetworkInterface vmInterface : vm.getInterfaces()) {
            vmInterface.setVmId(vm.getId());
            // These fields might not be saved in the OVF, so fill them with reasonable values.
            if (vmInterface.getId() == null) {
                vmInterface.setId(Guid.newGuid());
            }

            vnicProfileHelper.updateNicWithVnicProfileForUser(vmInterface, user);
            vmInterfaceManager.add(vmInterface, compensationContext, true, vm.getOs(), vm.getCompatibilityVersion());
        }

        vnicProfileHelper.auditInvalidInterfaces(vm.getName());
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
    protected void synchronizeDisksFromSnapshot(Guid vmId,
            Guid snapshotId,
            Guid activeSnapshotId,
            List<DiskImage> disksFromSnapshot,
            String vmName) {
        List<Guid> diskIdsFromSnapshot = new ArrayList<>();

        // Sync disks that exist or existed in the snapshot.
        int count = 1;
        for (DiskImage diskImage : disksFromSnapshot) {
            diskIdsFromSnapshot.add(diskImage.getId());
            if (getBaseDiskDao().exists(diskImage.getId())) {
                getBaseDiskDao().update(diskImage);
                DiskVmElement dve = getDiskVmElementDao().get(diskImage.getDiskVmElementForVm(vmId).getId());
                if (dve != null && !dve.equals(diskImage.getDiskVmElementForVm(vmId))) {
                    getDiskVmElementDao().update(diskImage.getDiskVmElementForVm(vmId));
                }

            } else {
                // If can't find the image, insert it as illegal so that it can't be used and make the device unplugged.
                if (getDiskImageDao().getSnapshotById(diskImage.getImageId()) == null) {
                    diskImage.setImageStatus(ImageStatus.ILLEGAL);
                    diskImage.setVmSnapshotId(activeSnapshotId);

                    ImagesHandler.addImage(diskImage, true, (diskImage.getStorageIds() == null) ? null :
                            new ImageStorageDomainMap(diskImage.getImageId(),
                                    diskImage.getStorageIds().get(0),
                                    diskImage.getQuotaId(),
                                    diskImage.getDiskProfileId()));
                }
                ImagesHandler.addDiskToVm(diskImage, vmId);
            }
            diskImage.setDiskAlias(ImagesHandler.getSuggestedDiskAlias(diskImage, vmName, count));
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
        for (VmDevice vmDevice : getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(
                vmId, VmDeviceGeneralType.DISK, VmDeviceType.DISK.getName())) {
            if (!diskIdsFromSnapshot.contains(vmDevice.getDeviceId()) && vmDevice.getSnapshotId() == null) {
                Disk disk = getDiskDao().get(vmDevice.getDeviceId());
                if (disk != null && disk.isAllowSnapshot()) {
                    getBaseDiskDao().remove(vmDevice.getDeviceId());
                    getVmDeviceDao().remove(vmDevice.getId());
                }
            }
        }
    }

    protected VmDeviceDao getVmDeviceDao() {
        return DbFacade.getInstance().getVmDeviceDao();
    }

    protected BaseDiskDao getBaseDiskDao() {
        return DbFacade.getInstance().getBaseDiskDao();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    protected VmDynamicDao getVmDynamicDao() {
        return DbFacade.getInstance().getVmDynamicDao();
    }

    protected VmStaticDao getVmStaticDao() {
        return DbFacade.getInstance().getVmStaticDao();
    }

    protected DiskImageDao getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDao();
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    protected DiskVmElementDao getDiskVmElementDao() {
        return DbFacade.getInstance().getDiskVmElementDao();
    }

    protected ClusterDao getClusterDao() {
        return DbFacade.getInstance().getClusterDao();
    }

    protected VmTemplateDao getVmTemplateDao() {
        return DbFacade.getInstance().getVmTemplateDao();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    protected QuotaDao getQuotaDao() {
        return DbFacade.getInstance().getQuotaDao();
    }
}
