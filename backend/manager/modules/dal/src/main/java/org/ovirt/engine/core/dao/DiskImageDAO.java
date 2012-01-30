package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.image_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.stateless_vm_image_map;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>DiskImageDAO</code> defines a type for performing CRUD operations on instances of {@link DiskImage}.
 *
 *
 */
public interface DiskImageDAO extends GenericDao<DiskImage, Guid> {

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

    // TODO mapping methods moved out of DbFacade that will be removed when we have Hibernate

    image_vm_pool_map getImageVmPoolMapByImageId(Guid imageId);

    void addImageVmPoolMap(image_vm_pool_map map);

    void removeImageVmPoolMap(Guid imageId);

    List<image_vm_pool_map> getImageVmPoolMapByVmId(Guid vmId);

    stateless_vm_image_map getStatelessVmImageMapForImageId(Guid imageId);

    void addStatelessVmImageMap(stateless_vm_image_map map);

    void removeStatelessVmImageMap(Guid imageId);

    List<stateless_vm_image_map> getAllStatelessVmImageMapsForVm(Guid vmId);

}
