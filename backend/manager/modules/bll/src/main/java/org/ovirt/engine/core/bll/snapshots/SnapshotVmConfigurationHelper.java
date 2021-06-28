package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SnapshotVmConfigurationHelper {

    private static final Logger log = LoggerFactory.getLogger(SnapshotVmConfigurationHelper.class);

    @Inject
    private SnapshotsManager snapshotsManager;

    @Inject
    private VmDao vmDao;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private VmHandler vmHandler;

    /**
     * Creates a VM by a specified OVF string.
     * If configuration is not specified, creates a VM according to the snapshotId
     * (required for backwards compatibility - @see getVmWithoutConfiguration method).
     *
     * @param snapshot the snapshot for which the configuration is created.
     * @return a VM object based on the specified parameters.
     */
    public VM getVmFromConfiguration(Snapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        VM vm;
        if (snapshot.getVmConfiguration() != null) {
            vm = getVmWithConfiguration(snapshot.getVmConfiguration(), snapshot.getVmId());
            if (snapshot.getType() != Snapshot.SnapshotType.PREVIEW) {
                // No need to mark disks of 'PREVIEW' snapshot as illegal
                // as it represents previous 'Active VM' state and no operations
                // on disks can be done while previewing a snapshot.
                markImagesIllegalIfNotInDb(vm, snapshot.getId());
            }
        } else {
            vm = getVmWithoutConfiguration(snapshot.getVmId(), snapshot.getId());
        }

        vmHandler.updateDisksForVm(vm, vm.getImages());

        return vm;
    }

    /**
     * Retrieves and returns VM static next run configuration if it exists.
     *
     * @param vmId id of the VM
     * @return VM static next run configuration, or null if it doesn't exist
     */
    public VmStatic getVmStaticFromNextRunConfiguration(Guid vmId) {
        final Snapshot snapshot = snapshotDao.get(vmId, Snapshot.SnapshotType.NEXT_RUN);
        if (snapshot == null) {
            return null;
        }
        final VM vm = getVmFromConfiguration(snapshot);
        return vm.getStaticData();
    }

    private VM getVmWithConfiguration(String configuration, Guid vmId) {
        VM result = vmDao.get(vmId);
        snapshotsManager.updateVmFromConfiguration(result, configuration);
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
    private VM getVmWithoutConfiguration(Guid vmId, Guid snapshotId) {
        VM vm = vmDao.get(vmId);
        List<VmNetworkInterface> interfaces = vmNetworkInterfaceDao.getAllForVm(vm.getId());
        vm.setInterfaces(interfaces);
        List<DiskImage> disks = diskImageDao.getAllSnapshotsForVmSnapshot(snapshotId);
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
                diskImageDao.getAllSnapshotsForVmSnapshot(snapshotId);
        // Converts to a map of Id to DiskImage in order to check existence only by Image ID (in case not all
        // image data is written to OVF
        Map<Guid, DiskImage> imagesInDbMap = ImagesHandler.getDiskImagesByIdMap(imagesInDb);
        for (DiskImage fromConfigImg : vm.getImages()) {
            if (!imagesInDbMap.containsKey(fromConfigImg.getImageId())) {
                log.debug("Image '{}' of Disk '{}' cannot be found in database. This image will be returned as ILLEGAL from the query",
                        fromConfigImg.getImageId(),
                        fromConfigImg.getId());
                fromConfigImg.setImageStatus(ImageStatus.ILLEGAL);
            } else {
                // Return image status as appears in DB (needed in case status is ILLEGAL in DB)
                DiskImage imageInDb = imagesInDbMap.get(fromConfigImg.getImageId());
                fromConfigImg.setImageStatus(imageInDb.getImageStatus());
            }
        }
    }
}
