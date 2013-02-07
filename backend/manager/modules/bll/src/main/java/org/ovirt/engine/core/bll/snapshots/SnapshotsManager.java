package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.ovf.VMStaticOvfLogHandler;

/**
 * The {@link Snapshot} manager is used to easily add/update/remove snapshots.
 */
public class SnapshotsManager {
    private final static Log log = LogFactory.getLog(SnapshotsManager.class);

    /**
     * Save an active snapshot for the VM, without saving the configuration.<br>
     * The snapshot is created in status {@link SnapshotStatus#OK} by default.
     *
     * @see #addActiveSnapshot(Guid, VM, SnapshotStatus, CompensationContext)
     * @param snapshotId
     *            The ID for the snapshot.
     * @param vm
     *            The VM to save the snapshot for.
     * @param compensationContext
     *            Context for saving compensation details.
     */
    public Snapshot addActiveSnapshot(Guid snapshotId,
            VM vm,
            final CompensationContext compensationContext) {
        return addActiveSnapshot(snapshotId,
                vm,
                SnapshotStatus.OK,
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
            final CompensationContext compensationContext) {
        return addSnapshot(snapshotId,
                "Active VM",
                snapshotStatus,
                SnapshotType.ACTIVE,
                vm,
                false,
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
     * @param compensationContext
     *            Context for saving compensation details.
     * @return The new snapshot's ID.
     */
    public void addSnapshot(Guid snapshotId,
            String description,
            SnapshotType snapshotType,
            VM vm,
            final CompensationContext compensationContext) {
        addSnapshot(snapshotId, description, SnapshotStatus.LOCKED, snapshotType, vm, true, compensationContext);
    }

    /**
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
     */
    public Snapshot addSnapshot(Guid snapshotId,
            String description,
            SnapshotStatus snapshotStatus,
            SnapshotType snapshotType,
            VM vm,
            boolean saveVmConfiguration,
            final CompensationContext compensationContext) {
        final Snapshot snapshot = new Snapshot(snapshotId,
                snapshotStatus,
                vm.getId(),
                saveVmConfiguration ? generateVmConfiguration(vm) : null,
                snapshotType,
                description,
                new Date(),
                vm.getAppList());

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
    protected String generateVmConfiguration(VM vm) {
        if (vm.getInterfaces() == null || vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(getVmNetworkInterfaceDao().getAllForVm(vm.getId()));
        }

        if (StringUtils.isEmpty(vm.getVmtName())) {
            VmTemplate t = getVmTemplateDao().get(vm.getVmtGuid());
            vm.setVmtName(t.getname());
        }

        VmDeviceUtils.setVmDevices(vm.getStaticData());
        ArrayList<DiskImage> images =
                new ArrayList<DiskImage>(ImagesHandler.filterImageDisks(getDiskDao().getAllForVm(vm.getId()),
                        false, true));
        for (DiskImage image : images) {
            image.setstorage_ids(null);
        }
        return new OvfManager().ExportVm(vm, images);
    }

    /**
     * Remove all the snapshots that belong to the given VM.
     *
     * @param vmId
     *            The ID of the VM.
     */
    public void removeSnapshots(Guid vmId) {
        for (Snapshot snapshot : getSnapshotDao().getAll(vmId)) {
            getSnapshotDao().remove(snapshot.getId());
        }
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
     */
    public void attempToRestoreVmConfigurationFromSnapshot(VM vm,
            Snapshot snapshot,
            Guid activeSnapshotId,
            CompensationContext compensationContext) {
        boolean vmUpdatedFromConfiguration = false;
        if (snapshot.getVmConfiguration() != null) {
            vmUpdatedFromConfiguration = updateVmFromConfiguration(vm, snapshot.getVmConfiguration());
        }

        if (!vmUpdatedFromConfiguration) {
            vm.setImages(new ArrayList<DiskImage>(getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshot.getId())));
        }

        vm.setAppList(snapshot.getAppList());
        getVmDynamicDao().update(vm.getDynamicData());
        synchronizeDisksFromSnapshot(vm.getId(), snapshot.getId(), activeSnapshotId, vm.getImages(), vm.getVmName());

        if (vmUpdatedFromConfiguration) {
            getVmStaticDao().update(vm.getStaticData());
            synchronizeNics(vm.getId(), vm.getInterfaces(), compensationContext);

            for (VmDevice vmDevice : getVmDeviceDao().getVmDeviceByVmId(vm.getId())) {
                if (deviceCanBeRemoved(vmDevice)) {
                    getVmDeviceDao().remove(vmDevice.getId());
                }
            }

            VmDeviceUtils.addImportedDevices(vm.getStaticData(), false);
        }
    }

    /**
     * @param vmDevice
     * @return true if the device can be removed (disk which allows snapshot can be removed as it is part
     * of the snapshot. Other disks shouldn't be removed as they are not part of the snapshot).
     */
    private boolean deviceCanBeRemoved(VmDevice vmDevice) {
        if (!vmDevice.getDevice().equals(VmDeviceType.DISK.getName())) {
            return true;
        }
        return getDiskDao().get(vmDevice.getDeviceId()).isAllowSnapshot();
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
            ArrayList<DiskImage> images = new ArrayList<DiskImage>();
            ArrayList<VmNetworkInterface> interfaces = new ArrayList<VmNetworkInterface>();
            new OvfManager().ImportVm(configuration, tempVM, images, interfaces);
            for (DiskImage diskImage : images) {
                DiskImage dbImage = getDiskImageDao().getSnapshotById(diskImage.getImageId());
                if (dbImage != null) {
                    diskImage.setstorage_ids(dbImage.getstorage_ids());
                }
            }
            new VMStaticOvfLogHandler(tempVM.getStaticData()).resetDefaults(oldVmStatic);

            vm.setStaticData(tempVM.getStaticData());
            vm.setImages(images);
            vm.setInterfaces(interfaces);

            // These fields are not saved in the OVF, so get them from the current VM.
            vm.setDedicatedVmForVds(oldVmStatic.getDedicatedVmForVds());
            vm.setIsoPath(oldVmStatic.getIsoPath());
            vm.setVdsGroupId(oldVmStatic.getVdsGroupId());
            // The VM configuration does not hold the vds group Id.
            // It is necessary to fetch the vm static from the Db, in order to get this information
            VmStatic vmStaticFromDb = getVmStaticDao().get(vm.getId());
            if (vmStaticFromDb != null) {
                VDSGroup vdsGroup = getVdsGroupDao().get(vmStaticFromDb.getVdsGroupId());
                if (vdsGroup != null) {
                    vm.setStoragePoolId(vdsGroup.getStoragePoolId().getValue());
                    vm.setVdsGroupCompatibilityVersion(vdsGroup.getcompatibility_version());
                    vm.setVdsGroupName(vdsGroup.getname());
                    vm.setVdsGroupCpuName(vdsGroup.getcpu_name());
                }
            }
            return true;
        } catch (OvfReaderException e) {
            log.errorFormat("Failed to update VM from the configuration: {0}).", configuration, e);
            return false;
        }
    }

    /**
     * Synchronize the VM's {@link VmNetworkInterface}s with the ones from the snapshot.<br>
     * All existing NICs will be deleted, and the ones from the snapshot re-added.<br>
     * In case a MAC address is already in use, the user will be issued a warning in the audit log.
     *
     * @param nics
     *            The nics from snapshot.
     */
    protected void synchronizeNics(Guid vmId, List<VmNetworkInterface> nics, CompensationContext compensationContext) {
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager();

        vmInterfaceManager.removeAll(vmId);
        for (VmNetworkInterface vmInterface : nics) {
            // These fields might not be saved in the OVF, so fill them with reasonable values.
            if (vmInterface.getId() == null) {
                vmInterface.setId(Guid.NewGuid());
            }
            vmInterface.setVmId(vmId);

            vmInterfaceManager.add(vmInterface, compensationContext, false);
        }
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
        List<Guid> diskIdsFromSnapshot = new ArrayList<Guid>();

        // Sync disks that exist or existed in the snapshot.
        int count = 1;
        for (DiskImage diskImage : disksFromSnapshot) {
            diskIdsFromSnapshot.add(diskImage.getId());
            if (getBaseDiskDao().exists(diskImage.getId())) {
                getBaseDiskDao().update(diskImage);
            } else {
                // If can't find the image, insert it as illegal so that it can't be used and make the device unplugged.
                if (getDiskImageDao().getSnapshotById(diskImage.getImageId()) == null) {
                    diskImage.setImageStatus(ImageStatus.ILLEGAL);
                    diskImage.setvm_snapshot_id(activeSnapshotId);

                    ImagesHandler.addImage(diskImage, true, (diskImage.getstorage_ids() == null) ? null :
                            new image_storage_domain_map(diskImage.getImageId(), diskImage.getstorage_ids().get(0)));
                }
                ImagesHandler.addDiskToVm(diskImage, vmId);
            }
            diskImage.setDiskAlias(ImagesHandler.getSuggestedDiskAlias(diskImage, vmName, count));
            count++;
        }
        removeDisksNotInSnapshot(vmId, diskIdsFromSnapshot);
    }

    /**
     * Remove all the disks which are allowed to be snapshot but not exist in the snapshot.
     * @param vmId - The vm id which is being snapshot.
     * @param diskIdsFromSnapshot - An image group id list for images which are part of the VM.
     */
    private void removeDisksNotInSnapshot(Guid vmId, List<Guid> diskIdsFromSnapshot) {
        for (VmDevice vmDevice : getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(
                vmId, VmDeviceType.DISK.getName(), VmDeviceType.DISK.getName())) {
            if (!diskIdsFromSnapshot.contains(vmDevice.getDeviceId())) {
                Disk disk = getDiskDao().get(vmDevice.getDeviceId());
                if (disk.isAllowSnapshot()) {
                    getBaseDiskDao().remove(vmDevice.getDeviceId());
                    getVmDeviceDao().remove(vmDevice.getId());
                }
            }
        }
    }

    protected VmDeviceDAO getVmDeviceDao() {
        return DbFacade.getInstance().getVmDeviceDao();
    }

    protected BaseDiskDao getBaseDiskDao() {
        return DbFacade.getInstance().getBaseDiskDao();
    }

    protected VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    protected VmDynamicDAO getVmDynamicDao() {
        return DbFacade.getInstance().getVmDynamicDao();
    }

    protected VmStaticDAO getVmStaticDao() {
        return DbFacade.getInstance().getVmStaticDao();
    }

    protected DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDao();
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    protected VdsGroupDAO getVdsGroupDao() {
        return DbFacade.getInstance().getVdsGroupDao();
    }

    protected VmTemplateDAO getVmTemplateDao() {
        return DbFacade.getInstance().getVmTemplateDao();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }
}
