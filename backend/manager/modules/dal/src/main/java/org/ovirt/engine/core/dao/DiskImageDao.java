package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code DiskImageDao} defines a type for performing CRUD operations on instances of {@link DiskImage}.
 */
public interface DiskImageDao extends ReadDao<DiskImage, Guid> {

    /**
     * Retrieves the snapshot with the specified id.
     *
     * @param id
     *            the id
     * @return the snapshot
     */
    DiskImage getSnapshotById(Guid id);

    /**
     * Retrieves all snapshots with the given parent id.
     *
     * @param id
     *            the parent id
     * @return the list of snapshots
     */
    List<DiskImage> getAllSnapshotsForParent(Guid id);

    /**
     * Retrieves all snapshots by a given image id
     * (ordered by the images chain).
     *
     * @param id
     *            the parent id
     * @return the list of snapshots
     */
    List<DiskImage> getAllSnapshotsForLeaf(Guid id);

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
     * Retrieves the disk image with the image information for the given vm snapshot id.
     *
     * @param diskId
     *            the disk id
     * @param vmSnapshotId
     *            the snapshot id
     * @return the list of snapshots
     */
     DiskImage getDiskSnapshotForVmSnapshot(Guid diskId, Guid vmSnapshotId);

    /**
     * Retrieves all snapshots associated with the given image group.
     *
     * @param id
     *            the image group id
     * @return the list of snapshots
     */
    List<DiskImage> getAllSnapshotsForImageGroup(Guid id);

    /**
     * Retrieves the disk snapshots attached to the VM with the specified id.
     *
     * @param vmId
     *            the VM id
     * @param isPlugged
     *            boolean indicating whether to query according to the plugged state,
     *            when null is provided - both plugged and unplugged disk snapshots
     *            would be queried.
     * @return attached disks snapshots to the VM with the specified id.
     */
    List<DiskImage> getAttachedDiskSnapshotsToVm(Guid vmId, Boolean isPlugged);

    /**
     * Retrieves the ancestor of the given image (or the image itself, if it has no ancestors).
     *
     * @param id
     *            The id of the image to get the ancestor for.
     * @return The ancestral image.
     */
    DiskImage getAncestor(Guid id);

    /**
     * Retrieves the ancestor of the given image (or the image itself, if it has no ancestors).
     *
     * @param id
     *            the id of the image to get the ancestor for.
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return The ancestral image.
     */
    DiskImage getAncestor(Guid id, Guid userID, boolean isFiltered);

    /**
     * Return all images that don't have a Disk entity in the DB and are part of a snapshot of the given VM ID.
     *
     * @param vmId
     *            The VM to look up snapshots for.
     * @return List of images (empty if none found).
     */
    List<DiskImage> getImagesWithNoDisk(Guid vmId);

    /**
     * Return all images that resides on the Storage Domain, including all content type (data, memory, OVF etc..)
     *
     * @param storageDomainId
     *            The Storage Domain to be fetched entities from.
     * @return List of DiskImages related to the Storage Domain.
     */
    List<DiskImage> getAllForStorageDomain(Guid storageDomainId);

    /**
     * Return all images that attached to disk profile.
     *
     * @param diskProfileIds
     *            List of Disk Profile Id attached to disks.
     * @return List of DiskImages
     */
    List<DiskImage> getAllForDiskProfiles(Collection<Guid> diskProfileIds);

    /**
     * Return all children snapshots for a list of parents
     *
     * @param parentIds List of parent image guids
     * @return List of child snapshots
     */
    Set<DiskImage> getAllSnapshotsForParents(Collection<Guid> parentIds);

    /**
     * Return all metadata and memory disk images dumps on the given storage domain
     *
     * @param storageDomainId storage domain ID
     * @return List of disk images IDs
     */
    List<Guid> getAllMetadataAndMemoryDisksForStorageDomain(Guid storageDomainId);

    /**
     * Returns all the disks of content type ISO residing on active domain in the specified storage pool
     *
     * @param storagePoolId The ID of the storage pool
     * @return List of the ISO disks as RepoImage
     */
    List<RepoImage> getIsoDisksForStoragePoolAsRepoImages(Guid storagePoolId);

    /**
     * Retrieves the DiskImage with the specified id and image_id.
     *
     * @param diskId
     *          The disk's id
     * @param imageId
     *          The image_id
     * @return The DiskImage
     */
    DiskImage getDiskImageByDiskAndImageIds(Guid diskId, Guid imageId);

    /**
     * Retrieves the DiskImage with the specified id and image_id.
     *
     * @param diskId
     *          The disk's id
     * @param imageId
     *          The image_id
     * @param userId
     *          The id of the user requesting the information
     * @param isFiltered
     *          Whether the results should be filtered according to the user's permissions
     * @return The DiskImage
     */
    DiskImage getDiskImageByDiskAndImageIds(Guid diskId, Guid imageId, Guid userId, boolean isFiltered);
}
