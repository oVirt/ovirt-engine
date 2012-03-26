package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>DiskImageDAO</code> defines a type for performing CRUD operations on instances of {@link DiskImage}.
 *
 *
 */
public interface DiskImageDAO extends GenericDao<DiskImage, Guid>, SearchDAO<DiskImage>, StatusAwareDao<Guid, ImageStatus> {

    /**
     * Retrieves the snapshot with the specified id.
     *
     * @param id
     *            the id
     * @return the snapshot
     */
    DiskImage getSnapshotById(Guid id);

    /**
     * Retrieves all disk images for the specified virtual machine id.
     *
     * @param id
     *            the VM id
     * @return the list of disk images
     */
    List<DiskImage> getAllForVm(Guid id);

    /**
     * Retrieves all disk images for the specified virtual machine id,
     * with optional filtering
     *
     * @param id
     *            the VM id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions

     * @return the list of disk images
     */
    List<DiskImage> getAllForVm(Guid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves all snapshots with the given parent id.
     *
     * @param id
     *            the parent id
     * @return the list of snapshots
     */
    List<DiskImage> getAllSnapshotsForParent(Guid id);

    /**
     * Retrieves all snapshots associated with the given storage domain.
     *
     * @param id
     *            the storage domain id
     * @return the list of snapshots
     */
    List<DiskImage> getAllSnapshotsForStorageDomain(Guid id);

    /**
     * Retrieves all snapshots associated with given snapshot id.
     *
     * @param id
     *            the snapshot id
     * @return the list of snapshots
     */
    List<DiskImage> getAllSnapshotsForVmSnapshot(Guid id);

    /**
     * Retrieves all snapshots associated with the given image group.
     *
     * @param id
     *            the image group id
     * @return the list of snapshots
     */
    List<DiskImage> getAllSnapshotsForImageGroup(Guid id);
    /**
     * Removes all disk images for the specified virtual machine id.
     *
     * @param id
     *            the virtual machine id
     */
    void removeAllForVmId(Guid id);

    /**
     * Retrieves the ancestor of the given image (or the image itself, if it has no ancestors).
     *
     * @param id
     *            The id of the image to get the ancestor for.
     * @return The ancestral image.
     */
    DiskImage getAncestor(Guid id);

    /**
     * Return all images that don't have a Disk entity in the DB and are part of a snapshot of the given VM ID.
     *
     * @param vmId
     *            The VM to look up snapshots for.
     * @return List of images (empty if none found).
     */
    List<DiskImage> getImagesWithNoDisk(Guid vmId);

    // TODO mapping methods moved out of DbFacade that will be removed when we have Hibernate
    List<DiskImage> getAllForQuotaId(Guid quotaId);

    List<DiskImage> getImagesByStorageIdAndTemplateId(Guid storageId, Guid templateId);

    List<DiskImage> getAllAttachableDisksByPoolId(Guid poolId, Guid userId, boolean isFiltered);
}
