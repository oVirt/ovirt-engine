package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.compat.Guid;

/**
 * The snapshot Dao is used to retrieve and manipulate {@link Snapshot} instances in the DB.
 */
public interface SnapshotDao extends GenericDao<Snapshot, Guid>, StatusAwareDao<Guid, SnapshotStatus> {

    /**
     * Update the ID of the snapshot given snapshot.
     *
     * @param snapshotId
     *            The (old) snapshot ID to update.
     * @param newSnapshotId
     *            The new ID to set.
     */
    void updateId(Guid snapshotId, Guid newSnapshotId);

    /**
     * Gets a filtered snapshot (the filter is optional and performed according to user permissions)
     *
     * @param id
     *            If of the snapshot to fetch
     * @param userId
     *            ID of the user that invoked the query
     * @param isFiltered
     *            whether the filtering will be performed
     */
    Snapshot get(Guid id, Guid userId, boolean isFiltered);

    /**
     * Return the {@link Snapshot} <b>first</b> id that matches the given parameters.<br>
     * <b>Note:</b> If more than one snapshot answers to the parameters, only the first will be returned (oldest by
     * creation date).
     *
     * @param vmId
     *            The id of the VM to check for.
     * @param type
     *            The type of snapshot.
     * @return The ID of the snapshot, or {@code null} if it doesn't exist.
     */
    Guid getId(Guid vmId, SnapshotType type);

    /**
     * Return the {@link Snapshot} <b>first</b> id that matches the given parameters.<br>
     * <b>Note:</b> If more than one snapshot answers to the parameters, only the first will be returned (oldest by
     * creation date).
     *
     * @param vmId
     *            The id of the VM to check for.
     * @param type
     *            The type of snapshot.
     * @return The snapshot, or {@code null} if it doesn't exist.
     */
    Snapshot get(Guid vmId, SnapshotType type);

    /**
     * Return the {@link Snapshot} <b>first</b> id that matches the given parameters.<br>
     * <b>Note:</b> If more than one snapshot answers to the parameters, only the first will be returned (oldest by
     * creation date).
     *
     * @param vmId
     *            The id of the VM to check for.
     * @param type
     *            The type of snapshot.
     * @param userId
     *            ID of the user that invoked the query
     * @param isFiltered
     *            whether the filtering will be performed
     * @return The snapshot, or {@code null} if it doesn't exist.
     */
    Snapshot get(Guid vmId, SnapshotType type, Guid userId, boolean isFiltered);

    /**
     * Return the <b>first</b> {@link Snapshot} that matches the given parameters.<br>
     * <b>Note:</b> If more than one snapshot answers to the parameters, only the first will be returned (oldest by
     * creation date).
     *
     * @param vmId
     *            The id of the VM to check for.
     * @param type
     *            The type of snapshot.
     * @param status
     *            The status of the snapshot.
     * @return The snapshot, or {@code null} if it doesn't exist.
     */
    Snapshot get(Guid vmId, SnapshotType type, SnapshotStatus status);

    /**
     * Return the <b>first</b> {@link Snapshot} that matches the given parameters.<br>
     * <b>Note:</b> If more than one snapshot answers to the parameters, only the first will be returned (oldest by
     * creation date).
     *
     * @param vmId
     *            The id of the VM to check for.
     * @param status
     *            The status of the snapshot.
     * @return The snapshot, or {@code null} if it doesn't exist.
     */
    Snapshot get(Guid vmId, SnapshotStatus status);

    /**
     * Return the {@link Snapshot} <b>first</b> id that matches the given parameters.<br>
     * <b>Note:</b> If more than one snapshot answers to the parameters, only the first will be returned (oldest by
     * creation date).
     *
     * @param vmId
     *            The id of the VM to check for.
     * @param type
     *            The type of snapshot.
     * @param status
     *            The status of the snapshot.
     * @return The ID of the snapshot, or {@code null} if it doesn't exist.
     */
    Guid getId(Guid vmId, SnapshotType type, SnapshotStatus status);

    /**
     * Get all the snapshots of the given VM. The {@link Snapshot#getVmConfiguration()} field will contain the
     * configuration (if it is available).
     *
     * @param vmId
     *            The VM id.
     * @return A list of snapshots that exist for the VM, ordered by creation date (earliest to latest), or empty list
     *         if no snapshots exist.
     */
    List<Snapshot> getAllWithConfiguration(Guid vmId);

    /**
     * Get all the snapshots of the given VM. The {@link Snapshot#getVmConfiguration()} field will always be null, and
     * instead the {@link Snapshot#isVmConfigurationAvailable()} field will specify if configuration is available or
     * not.
     *
     * @param vmId
     *            The VM id.
     * @return A list of snapshots that exist for the VM, ordered by date (earliest to latest), or empty list if no
     *         snapshots exist.
     */
    List<Snapshot> getAll(Guid vmId);

    /**
     * Get all the snapshots of the given VM. The {@link Snapshot#getVmConfiguration()} field will always be null, and
     * instead the {@link Snapshot#isVmConfigurationAvailable()} field will specify if configuration is available or
     * not.
     *
     * @param vmId
     *            The VM id.
     * @param userId
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return A list of snapshots that exist for the VM, ordered by date (earliest to latest), or empty list if no
     *         snapshots exist.
     */
    List<Snapshot> getAll(Guid vmId, Guid userId, boolean isFiltered);

    /**
     * Get all the snapshots with disks on the specified storage domain.
     *
     * @param storageId
     *            The Storage Domain ID.
     * @return the list of snapshots.
     */
    List<Snapshot> getAllByStorageDomain(Guid storageId);

    /**
     * Check if there exists a snapshot of the given type for the VM.
     *
     * @param vmId
     *            The id of the VM to check for.
     * @param type
     *            The type to look for.
     * @return Whether a snapshot of the given type exists for the VM or not.
     */
    boolean exists(Guid vmId, SnapshotType type);

    /**
     * Check if there exists a snapshot for the VM with the given status.
     *
     * @param vmId
     *            The id of the VM to check for.
     * @param status
     *            The status to look for.
     * @return Whether a snapshot of the given status exists for the VM or not.
     */
    boolean exists(Guid vmId, SnapshotStatus status);

    /**
     * Check if there exists a snapshot for the VM with the given ID.
     *
     * @param vmId
     *            The ID of the VM to check for.
     * @param snapshotId
     *            The ID of the snapshot to look for.
     * @return Whether a snapshot of the given ID exists for the VM or not.
     */
    boolean exists(Guid vmId, Guid snapshotId);

    /**
     * Get the number of snapshots that contain the given memory
     *
     * @param snapshot
     *           The snapshot with memory disks that should be used to filter the snapshots
     * @return Number of snapshots containing the given memory
     */
    int getNumOfSnapshotsByDisks(Snapshot snapshot);

    /**
     * Clear the memory from the active snapshot of the VM with the given id
     *
     * @param vmId
     *          The ID of the VM to remove the memory from.
     */
    void removeMemoryFromActiveSnapshot(Guid vmId);

    /**
     * Clear the memory from the snapshot with the given id
     *
     * @param snapshotId
     *          The id of the snapshot that its memory should be cleared
     */
    void removeMemoryFromSnapshot(Guid snapshotId);

    /**
     * Update the memory for the given VM
     * The memory of the active snapshot will be updated
     *
     * @param vmId
     *          The ID of the VM which we are going to update the memory for
     * @param memoryDumpDiskId
     *          The ID of the disk that contains the memory dump
     * @param memoryMetadataDiskId
     *          The ID of the disk that contains the memory metadata
     */
    void updateHibernationMemory(Guid vmId, Guid memoryDumpDiskId, Guid memoryMetadataDiskId);

    /**
     * Returns the snapshots containing the specified memory disk ID (metadata disk or memory disk)
     *
     * @param memoryDiskId
     *          The ID of the disk
     * @return The snapshots containing the specified memory disk ID (metadata disk or memory disk)
     */
    List<Snapshot> getSnapshotsByMemoryDiskId(Guid memoryDiskId);
}
