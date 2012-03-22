package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmConfigurationBySnapshotQueryParams;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDAO;

/**
 * This class implements the logic of the query responsible for
 * getting a VM configuration by snapshot.
 *
 * @param <P>
 */
public class GetVmConfigurationBySnapshotQuery<P extends GetVmConfigurationBySnapshotQueryParams> extends QueriesCommandBase<P> {

    private Snapshot snapshot;

    public GetVmConfigurationBySnapshotQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String configuration =
                getConfigurationFromDb(getParameters().getSnapshotId(), getUserID(), getParameters().isFiltered());
        VM result = null;
        if (configuration != null) {
            result = getVmFromConfiguration(configuration);
            markImagesIllegalIfNotInDb(result);
            VmHandler.updateDisksForVm(result, result.getImages());
        }

        getQueryReturnValue().setReturnValue(result);
    }

    protected VM getVmFromConfiguration(String configuration) {
        VM result = getVmDao().get(snapshot.getVmId());
        getSnapshotManager().updateVmFromConfiguration(result,configuration);
        return result;
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
                getDiskImageDao().getAllSnapshotsForVmSnapshot(getParameters().getSnapshotId());
        // Converts to a map of Id to DiskImage in order to check existence only by Image ID (in case not all
        // image data is written to OVF
        Map<Guid, DiskImage> imagesInDbMap = ImagesHandler.getDiskImagesByIdMap(imagesInDb);
        for (DiskImage fromConfigImg : vm.getImages()) {
            if (!imagesInDbMap.containsKey(fromConfigImg.getId())) {
                fromConfigImg.setimageStatus(ImageStatus.ILLEGAL);
            }
        }
    }

    protected DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDAO();
    }

    protected VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDAO();
    }


    protected SnapshotsManager getSnapshotManager() {
        return new SnapshotsManager();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    private String getConfigurationFromDb(Guid snapshotSourceId, Guid userId, boolean isFiltered) {
        snapshot = getSnapshotDao().get(snapshotSourceId, userId, isFiltered);
        if (snapshot == null) {
            log.error(String.format("Snapshot %1$s does not exist",snapshotSourceId));
            return null;
        }
        return snapshot.getVmConfiguration();
    }

}
