package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

/**
 * This class implements the logic of the query responsible for getting a VM configuration by snapshot.
 *
 * @param <P>
 */
public class GetVmConfigurationBySnapshotQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    private Snapshot snapshot;

    public GetVmConfigurationBySnapshotQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String configuration =
                getConfigurationFromDb(getParameters().getId(), getUserID(), getParameters().isFiltered());
        VM result = null;
        if (configuration != null) {
            result = getVmFromConfiguration(configuration);
            markImagesIllegalIfNotInDb(result);
        } else {
            result = getVmWithoutConfiguration();
        }

        VmHandler.updateDisksForVm(result, result.getImages());

        getQueryReturnValue().setReturnValue(result);
    }

    protected VM getVmFromConfiguration(String configuration) {
        VM result = getVmDao().get(snapshot.getVmId());
        getSnapshotManager().updateVmFromConfiguration(result, configuration);
        return result;
    }

    /**
     * Build a VM entity when configuration is not available, This method is required to create a VM entity for
     * snapshots which were taken in old engine where full OVF snapshot metadata was not supported.
     *
     * See also {@link VmHandler#updateDisksForVm(VM, List)}
     *
     * @return a VM model
     */
    protected VM getVmWithoutConfiguration() {
        VM vm = getVmDao().get(snapshot.getVmId());
        List<VmNetworkInterface> interfaces = getVmNetworkInterfaceDao().getAllForVm(vm.getId());
        vm.setInterfaces(interfaces);
        List<DiskImage> disks = getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshot.getId());
        vm.setImages(new ArrayList<DiskImage>(disks));

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
     */
    protected void markImagesIllegalIfNotInDb(VM vm) {
        List<DiskImage> imagesInDb =
                getDbFacade().getDiskImageDao().getAllSnapshotsForVmSnapshot(getParameters().getId());
        // Converts to a map of Id to DiskImage in order to check existence only by Image ID (in case not all
        // image data is written to OVF
        Map<Guid, DiskImage> imagesInDbMap = ImagesHandler.getDiskImagesByIdMap(imagesInDb);
        for (DiskImage fromConfigImg : vm.getImages()) {
            if (fromConfigImg.getDiskStorageType() == DiskStorageType.IMAGE
                    && !imagesInDbMap.containsKey(fromConfigImg.getImageId())) {
                log.debugFormat("Image {0} of Disk {1} cannot be found in database. This image will be returned as ILLEGAL from the query",
                        fromConfigImg.getImageId(),
                        fromConfigImg.getId());
                fromConfigImg.setImageStatus(ImageStatus.ILLEGAL);
            }
        }
    }

    protected VmDAO getVmDao() {
        return getDbFacade().getVmDao();
    }

    protected SnapshotsManager getSnapshotManager() {
        return new SnapshotsManager();
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return getDbFacade().getVmNetworkInterfaceDao();
    }

    protected DiskImageDAO getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
    }

    private String getConfigurationFromDb(Guid snapshotSourceId, Guid userId, boolean isFiltered) {
        snapshot = getSnapshotDao().get(snapshotSourceId, userId, isFiltered);
        if (snapshot == null) {
            log.error(String.format("Snapshot %1$s does not exist", snapshotSourceId));
            return null;
        }
        return snapshot.getVmConfiguration();
    }

}
