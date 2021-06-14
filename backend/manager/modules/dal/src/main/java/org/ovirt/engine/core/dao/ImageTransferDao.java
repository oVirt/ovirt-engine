package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.compat.Guid;

public interface ImageTransferDao extends GenericDao<ImageTransfer, Guid>, SearchDao<ImageTransfer> {
    /**
     * Retrieves an ImageTransfer entity based on its disk id
     *
     * @return ImageTransfer entity
     */
    ImageTransfer getByDiskId(Guid diskId);

    /**
     * Retrieves an ImageTransfer entities based on vds id
     *
     * @return ImageTransfer entity
     */
    List<ImageTransfer> getByVdsId(Guid vdsId);

    /**
     *
     * @return ImageTransfer entity
     */
    ImageTransfer get(Guid diskId, Guid userId, boolean isFiltered);

    /**
     * Retrieves an ImageTransfer entities based on storage id
     *
     * @return ImageTransfer entity
     */
    List<ImageTransfer> getByStorageId(Guid storageId);

    /**
     * Deletes completed image transfers.
     * Successful backups have {@link ImageTransferPhase#FINISHED_SUCCESS} or
     * {@link ImageTransferPhase#FINISHED_CLEANUP} statuses.
     * Failed backups have either {@link ImageTransferPhase#FINISHED_FAILURE} status.
     *
     * @param succeededImageTransfers all successful image transfers having older end time than this date will be deleted.
     * @param failedImageTransfers all failed image transfers having older end time than this date will be deleted.
     */
    void deleteCompletedImageTransfers(Date succeededImageTransfers, Date failedImageTransfers);
}
