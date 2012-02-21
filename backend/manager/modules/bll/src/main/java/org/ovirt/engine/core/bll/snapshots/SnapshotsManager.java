package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.ImageVmMapDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

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
     *            The VM to link to & save configuration for (if necessary). {@link VM#getImages()} should return the
     *            images to save and {@link VM#getInterfaces()} should return the NICs to save, if configuration saving
     *            is requested.
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

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getSnapshotDao().save(snapshot);
                compensationContext.snapshotNewEntity(snapshot);
                return null;
            }
        });
    }

    /**
     * Generate a string containing the given VM's configuration. The VM needs to contain all images & NICs.
     *
     * @param vm
     *            The VM to generate configuration from.
     * @return A String containing the VM configuration.
     */
    protected String generateVmConfiguration(VM vm) {
        RefObject<String> tempRefObject = new RefObject<String>("");
        new OvfManager().ExportVm(tempRefObject, vm, vm.getImages());
        return tempRefObject.argvalue;
    }

    /**
     * Remove all the snapshots that belong to the given VM.
     *
     * @param vmId
     *            The ID of the VM.
     */
    public void removeSnapshots(Guid vmId) {
        for (Snapshot snapshot : getSnapshotDao().getForVm(vmId)) {
            getSnapshotDao().remove(snapshot.getId());
        }
    }

    /**
     * Mark the given snapshot as {@link SnapshotStatus#BROKEN} if it exists and it's a {@link SnapshotType#REGULAR}
     * snapshot.
     *
     * @param snapshotId
     *            The ID of the snapshot.
     */
    public void markBroken(Guid snapshotId) {
        if (snapshotId == null) {
            return;
        }

        Snapshot snapshot = getSnapshotDao().get(snapshotId);
        if (snapshot != null && SnapshotType.REGULAR == snapshot.getType()) {
            getSnapshotDao().updateStatus(snapshotId, SnapshotStatus.BROKEN);
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
            CompensationContext compensationContext) {
        if (snapshot.getVmConfiguration() == null || !updateVmFromConfiguration(vm, snapshot.getVmConfiguration())) {
            return;
        }

        vm.setapp_list(snapshot.getAppList());
        getVmStaticDao().update(vm.getStaticData());
        getVmDynamicDao().update(vm.getDynamicData());
        synchronizeNics(vm.getId(), vm.getInterfaces(), compensationContext);
        synchronizeDisksFromSnapshot(vm.getId(), snapshot.getId(), vm.getImages());
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
            RefObject<VM> vmRef = new RefObject<VM>();
            RefObject<ArrayList<DiskImage>> imagesRef = new RefObject<ArrayList<DiskImage>>();
            new OvfManager().ImportVm(configuration, vmRef, imagesRef);

            vm.setStaticData(vmRef.argvalue.getStaticData());
            vm.setImages(imagesRef.argvalue);
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
     * @param imagesFromSnapshot
     *            The images that existed in the snapshot.
     */
    protected void synchronizeDisksFromSnapshot(Guid vmId, Guid snapshotId, List<DiskImage> imagesFromSnapshot) {
        List<DiskImage> disksFromSnapshot = new ArrayList<DiskImage>();
        for (DiskImage image : imagesFromSnapshot) {
            if (snapshotId.equals(image.getvm_snapshot_id())) {
                disksFromSnapshot.add(image);
            }
        }

        for (DiskImage diskImage : disksFromSnapshot) {
            Disk disk = diskImage.getDisk();
            if (getDiskDao().exists(disk.getId())) {
                getDiskDao().update(disk);
            } else {
                if (getDiskImageDao().get(diskImage.getId()) == null) {
                    // TODO: Set disk status to broken, for now mark image as illegal for further references.
                    diskImage.setimageStatus(ImageStatus.INVALID);
                } else {
                    VmDeviceUtils.addManagedDevice(new VmDeviceId(disk.getId(), vmId),
                            VmDeviceType.DISK, VmDeviceType.DISK, "", true, false);
                    getImageVmMapDao().save(new image_vm_map(true, diskImage.getId(), vmId));
                }

                getDiskDao().save(disk);
            }
        }
    }

    protected ImageVmMapDAO getImageVmMapDao() {
        return DbFacade.getInstance().getImageVmMapDAO();
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
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
}
