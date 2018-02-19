package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code VmPoolDao} defines a type that performs CRUD operations on instances of {@link VmPool}.
 */
public interface VmPoolDao extends Dao, SearchDao<VmPool> {
    /**
     * Removes the specified VM from the pool.
     *
     * @param vm
     *            the VM id
     */
    void removeVmFromVmPool(Guid vm);

    /**
     * Retrieves the VM pool with the specified ID.
     *
     * @param id
     *            the pool id
     * @return the VM pool
     */
    VmPool get(Guid id);

    /**
    * Retrieves the VM pool with the specified ID with optional filtering
    *
    * @param id
    *            the pool id
    * @param userID
    *            the ID of the user requesting the information
    * @param isFiltered
    *            Whether the results should be filtered according to the user's permissions
    * @return the VM pool
    */
    VmPool get(Guid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves the VM pool with the given name.
     *
     * @param name
     *            the pool name
     * @return the VM pool
     */
    VmPool getByName(String name);

    /**
     * Gets all defined VM pools.
     *
     * @return the list of VM pools
     */
    List<VmPool> getAll();

    /**
     * Gets all pools for the specified user.
     *
     * @param user
     *            the user id
     * @return the list of VM pools
     */
    List<VmPool> getAllForUser(Guid user);

    /**
     * Saves the specified pool.
     *
     * @param pool
     *            the VM pool
     */
    void save(VmPool pool);

    /**
     * Updates the specified pool.
     *
     * @param pool
     *            the VM pool
     */
    void update(VmPool pool);

    /**
     * Removes the VM pool with the specified id.
     *
     * @param vmPool
     *            the pool id
     */
    void remove(Guid vmPool);

    /**
     * Sets beingDestroyed flag of the given pool.
     */
    void setBeingDestroyed(Guid vmPoolId, boolean beingDestroyed);

    void addVmToPool(VmPoolMap map);

    List<VmPoolMap> getVmPoolsMapByVmPoolId(Guid vmPoolId);

    /**
     * Gets the maps of the given pool, for the Vms that are in the given status
     */
    List<VmPoolMap> getVmMapsInVmPoolByVmPoolIdAndStatus(Guid vmPoolId, VMStatus vmStatus);

    /**
     * Returns a single VM from the vm pool with the specified id, with optional filtering.
     *
     * @param vmPoolId
     *            the vm pool id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return a single VM from the pool
     */
    VM getVmDataFromPoolByPoolGuid(Guid vmPoolId, Guid userID, boolean isFiltered);

    /**
     * If number of prestarted VMs in the pool is greater than total number of VMs, set
     * the number of prestarted VMs to be equal to the total number of VMs. Otherwise, leave
     * the number of prestarted VMs untouched.
     *
     * @param vmPoolId
     *            the VM pool ID
     */
    void boundVmPoolPrestartedVms(Guid vmPoolId);

    /**
     * Specific use-case in the User-Portal - Get vm-pools:
     *
     *   1) filtered by user
     *   2) sorted in ascending order
     *   3) with max # of results specified.
     *   4) potentially with page number (paging)
     *
     * (https://bugzilla.redhat.com/1537735)
     *
     * @param userID
     *            the ID of the user requesting the information
     * @param offset
     *            the beginning index of the result-set
     * @param limit
     *            max number of results to return
     * @return the list of VM-pools matching this criteria
     */
    List<VmPool> getAllVmPoolsFilteredAndSorted(Guid userID, int offset, int limit);
}
