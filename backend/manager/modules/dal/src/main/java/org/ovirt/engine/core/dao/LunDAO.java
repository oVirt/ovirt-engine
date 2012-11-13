package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.LUNs;

/**
 * <code>LunDAO</code> defines a type for performing CRUD operations on instances of {@link LUNs}.
 *
 *
 */
public interface LunDAO extends DAO {
    /**
     * Gets the LUN with the specified id.
     *
     * @param id
     *            the LUN id
     * @return the LUN
     */
    LUNs get(String id);

    /**
     * Retrieves the list of LUNs for the given storage server connection.
     *
     * @param id
     *            the storage server connection id
     * @return the list of LUNs
     */
    List<LUNs> getAllForStorageServerConnection(String id);

    /**
     * Retrieves the list of LUNs for the given VG id.
     *
     * @param id
     *            the VG id
     * @return the list of LUNs
     */
    List<LUNs> getAllForVolumeGroup(String id);

    /**
     * Retrieves all LUNs.
     *
     * @return the list of LUNs
     */
    List<LUNs> getAll();

    /**
     * Updates the lun volume group id
     */
    void updateLUNsVolumeGroupId(String id, String volumeGroupId);

    /**
     * Saves the supplied LUN instance.
     *
     * @param lun
     *            the LUN
     */
    void save(LUNs lun);

    /**
     * Removes the specified LUN.
     *
     * @param id
     *            the LUN id
     */
    void remove(String id);
}
