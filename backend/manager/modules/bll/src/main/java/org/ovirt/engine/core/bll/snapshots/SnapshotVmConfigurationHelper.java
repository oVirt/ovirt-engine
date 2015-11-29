package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotVmConfigurationHelper {

    private static final Logger log = LoggerFactory.getLogger(SnapshotVmConfigurationHelper.class);

    /**
     * Creates a VM by a specified OVF string.
     * If configuration is not specified, creates a VM according to the snapshotId
     * (required for backwards compatibility - @see getVmWithoutConfiguration method).
     *
     * @param configuration The OVF String
     * @param vmId The VM ID
     * @param snapshotId The snapshot ID
     * @return a VM object based on the specified parameters.
     */
    public VM getVmFromConfiguration(String configuration, Guid vmId, Guid snapshotId) {
        VM vm;
        if (configuration != null) {
            vm = getVmWithConfiguration(configuration, vmId);
            Snapshot snapshot = getSnapshotDao().get(snapshotId);
            if (snapshot != null && snapshot.getType() != Snapshot.SnapshotType.PREVIEW) {
                // No need to mark disks of 'PREVIEW' snapshot as illegal
                // as it represents previous 'Active VM' state and no operations
                // on disks can be done while previewing a snapshot.
                markImagesIllegalIfNotInDb(vm, snapshotId);
            }
        } else {
            vm = getVmWithoutConfiguration(vmId, snapshotId);
        }

        VmHandler.updateDisksForVm(vm, vm.getImages());

        return vm;
    }

    protected VM getVmWithConfiguration(String configuration, Guid vmId) {
        VM result = getVmDao().get(vmId);
        getSnapshotManager().updateVmFromConfiguration(result, configuration);
        return result;
    }

    /**
     * Build a VM entity when configuration is not available, This method is required to create a VM entity for
     * snapshots which were taken in old engine where full OVF snapshot metadata was not supported.
     *
     * See also {@link VmHandler#updateDisksForVm(VM, java.util.List)}
     *
     * @return a VM
     */
    protected VM getVmWithoutConfiguration(Guid vmId, Guid snapshotId) {
        VM vm = getVmDao().get(vmId);
        List<VmNetworkInterface> interfaces = getVmNetworkInterfaceDao().getAllForVm(vm.getId());
        vm.setInterfaces(interfaces);
        List<DiskImage> disks = getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshotId);
        vm.setImages(new ArrayList<>(disks));

        // OvfReader sets disks as active during import which is required by VmHandler.updateDisksForVm to prepare the
        // VM disks.
        for (DiskImage currDisk : disks) {
            currDisk.setActive(true);
        }

        return vm;
    }

    /**
     * Gets all images for the VM as stored in DB. Checks if the images stored in the configuration are stored in DB by
     * comparing Guids. If an image exists in the configuration but does not exist in DB, mark it as illegal (This
     * scenario might happen when one erases a disk after performing a snapshot).
     *
     * @param vm
     *            VM to mark illegal images for
     * @param snapshotId
     *            The relevant snapshot ID
     */
    protected void markImagesIllegalIfNotInDb(VM vm, Guid snapshotId) {
        List<DiskImage> imagesInDb =
                getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshotId);
        // Converts to a map of Id to DiskImage in order to check existence only by Image ID (in case not all
        // image data is written to OVF
        Map<Guid, DiskImage> imagesInDbMap = ImagesHandler.getDiskImagesByIdMap(imagesInDb);
        for (DiskImage fromConfigImg : vm.getImages()) {
            if (fromConfigImg.getDiskStorageType() == DiskStorageType.IMAGE
                    && !imagesInDbMap.containsKey(fromConfigImg.getImageId())) {
                log.debug("Image '{}' of Disk '{}' cannot be found in database. This image will be returned as ILLEGAL from the query",
                        fromConfigImg.getImageId(),
                        fromConfigImg.getId());
                fromConfigImg.setImageStatus(ImageStatus.ILLEGAL);
            }
            else {
                // Return image status as appears in DB (needed in case status is ILLEGAL in DB)
                DiskImage imageInDb = imagesInDbMap.get(fromConfigImg.getImageId());
                fromConfigImg.setImageStatus(imageInDb.getImageStatus());
            }
        }
    }

    public SnapshotsManager getSnapshotManager() {
        return new SnapshotsManager();
    }

    protected VmDao getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    protected DiskImageDao getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDao();
    }

}
