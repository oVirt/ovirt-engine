package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.ovf.VMStaticOvfLogHandler;

/**
 * The {@link Snapshot} manager is used to easily add/update/remove snapshots.
 */
public class SnapshotsManager {
    private Log log = LogFactory.getLog(getClass());

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
     */
    public void addActiveSnapshot(Guid snapshotId,
            VM vm,
            final CompensationContext compensationContext) {
        addSnapshot(snapshotId, "Active VM", SnapshotStatus.OK, SnapshotType.ACTIVE, vm, false, compensationContext);
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
    protected void addSnapshot(Guid snapshotId,
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
                vm.getapp_list());

        getSnapshotDao().save(snapshot);
        compensationContext.snapshotNewEntity(snapshot);
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

        if (StringHelper.isNullOrEmpty(vm.getvmt_name())) {
            VmTemplate t = getVmTemplateDao().get(vm.getvmt_guid());
            vm.setvmt_name(t.getname());
        }

        RefObject<String> tempRefObject = new RefObject<String>("");
        new OvfManager().ExportVm(tempRefObject,
                vm,
                new ArrayList<DiskImage>(getDiskImageDao().getAllForVm(vm.getId())));
        return tempRefObject.argvalue;
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
     * @param snapshotId
     *            The ID of the snapshot for who to remove illegal images for.
     */
    public void removeAllIllegalDisks(Guid snapshotId) {
        for (DiskImage diskImage : getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshotId)) {
            if (diskImage.getimageStatus() == ImageStatus.ILLEGAL) {
                ImagesHandler.removeDiskImage(diskImage);
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
        if (snapshot.getVmConfiguration() == null || !updateVmFromConfiguration(vm, snapshot.getVmConfiguration())) {
            vm.setImages(new ArrayList<DiskImage>(getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshot.getId())));
            vm.setInterfaces(DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForVm(vm.getId()));
        }

        vm.setapp_list(snapshot.getAppList());
        getVmStaticDao().update(vm.getStaticData());
        getVmDynamicDao().update(vm.getDynamicData());
        synchronizeNics(vm.getId(), vm.getInterfaces(), compensationContext);
        synchronizeDisksFromSnapshot(vm.getId(), snapshot.getId(), activeSnapshotId, vm.getImages());
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
            RefObject<VM> vmRef = new RefObject<VM>();
            RefObject<ArrayList<DiskImage>> imagesRef = new RefObject<ArrayList<DiskImage>>();
            RefObject<ArrayList<VmNetworkInterface>> interfacesRef = new RefObject<ArrayList<VmNetworkInterface>>();
            new OvfManager().ImportVm(configuration, vmRef, imagesRef, interfacesRef);
            new VMStaticOvfLogHandler(vmRef.argvalue.getStaticData()).resetDefaults(oldVmStatic);

            vm.setStaticData(vmRef.argvalue.getStaticData());
            vm.setImages(imagesRef.argvalue);
            vm.setInterfaces(interfacesRef.argvalue);

            // These fields are not saved in the OVF, so get them from the current VM.
            vm.setdedicated_vm_for_vds(oldVmStatic.getdedicated_vm_for_vds());
            vm.setiso_path(oldVmStatic.getiso_path());
            vm.setvds_group_id(oldVmStatic.getvds_group_id());
            // The VM configuration does not hold the vds group Id.
            // It is necessary to fetch the vm static from the Db, in order to get this information
            VmStatic vmStaticFromDb = getVmStaticDao().get(vm.getId());
            if (vmStaticFromDb != null) {
                VDSGroup vdsGroup = getVdsGroupDao().get(vmStaticFromDb.getvds_group_id());
                if (vdsGroup != null) {
                    vm.setstorage_pool_id(vdsGroup.getstorage_pool_id().getValue());
                    vm.setvds_group_compatibility_version(vdsGroup.getcompatibility_version());
                    vm.setvds_group_name(vdsGroup.getname());
                    vm.setvds_group_cpu_name(vdsGroup.getcpu_name());
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

        vmInterfaceManager.removeAll(true, vmId);
        for (VmNetworkInterface vmInterface : nics) {
            // These fields are not saved in the OVF, so fill them with reasonable values.
            vmInterface.setId(Guid.NewGuid());
            vmInterface.setVmId(vmId);

            vmInterfaceManager.add(vmInterface, compensationContext);
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
            List<DiskImage> disksFromSnapshot) {
        List<Guid> diskIdsFromSnapshot = new ArrayList<Guid>();

        // Sync disks that exist or existed in the snapshot.
        for (DiskImage diskImage : disksFromSnapshot) {
            diskIdsFromSnapshot.add(diskImage.getimage_group_id());
            Disk disk = diskImage.getDisk();
            if (getDiskDao().exists(disk.getId())) {
                getDiskDao().update(disk);
            } else {

                // If can't find the image, insert it as illegal so that it can't be used and make the device unplugged.
                if (getDiskImageDao().getSnapshotById(diskImage.getId()) == null) {
                    diskImage.setimageStatus(ImageStatus.ILLEGAL);
                    diskImage.setvm_snapshot_id(activeSnapshotId);

                    ImagesHandler.addImage(diskImage, true, new image_storage_domain_map(diskImage.getId(),
                            diskImage.getstorage_ids().get(0)));
                }

                ImagesHandler.addDiskToVm(disk, vmId);
            }
        }

        // Remove all disks that didn't exist in the snapshot.
        for (VmDevice vmDevice : getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(
                vmId, VmDeviceType.DISK.getName(), VmDeviceType.DISK.getName())) {
            if (!diskIdsFromSnapshot.contains(vmDevice.getDeviceId())) {
                getDiskDao().remove(vmDevice.getDeviceId());
                getVmDeviceDao().remove(vmDevice.getId());
            }
        }
    }

    protected VmDeviceDAO getVmDeviceDao() {
        return DbFacade.getInstance().getVmDeviceDAO();
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    protected VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDAO();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    protected VmDynamicDAO getVmDynamicDao() {
        return DbFacade.getInstance().getVmDynamicDAO();
    }

    protected VmStaticDAO getVmStaticDao() {
        return DbFacade.getInstance().getVmStaticDAO();
    }

    protected DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDAO();
    }

    protected VdsGroupDAO getVdsGroupDao() {
        return DbFacade.getInstance().getVdsGroupDAO();
    }

    protected VmTemplateDAO getVmTemplateDao() {
        return DbFacade.getInstance().getVmTemplateDAO();
    }

    protected VmNetworkInterfaceDAO getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDAO();
    }
}
