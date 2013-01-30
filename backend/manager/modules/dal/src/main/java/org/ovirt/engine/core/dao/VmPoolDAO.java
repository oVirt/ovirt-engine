package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

/**
 * <code>VmPoolDAO</code> defines a type that performs CRUD operations on instances of {@link vm_pool}.
 *
 */
public interface VmPoolDAO extends DAO, SearchDAO<vm_pools> {
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
    vm_pools get(NGuid id);

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
    vm_pools get(NGuid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves the VM pool with the given name.
     *
     * @param name
     *            the pool name
     * @return the VM pool
     */
    vm_pools getByName(String name);

    /**
     * Gets all defined VM pools.
     *
     * @return the list of VM pools
     */
    List<vm_pools> getAll();

    /**
     * Gets all pools for the specified user.
     *
     * @param user
     *            the user id
     * @return the list of VM pools
     */
    List<vm_pools> getAllForUser(Guid user);

    /**
     * Saves the specified pool.
     *
     * @param pool
     *            the VM pool
     */
    void save(vm_pools pool);

    /**
     * Updates the specified pool.
     *
     * @param pool
     *            the VM pool
     */
    void update(vm_pools pool);

    /**
     * Removes the VM pool with the specified id.
     *
     * @param vmPool
     *            the pool id
     */
    void remove(NGuid vmPool);

    VmPoolMap getVmPoolMapByVmGuid(Guid vmId);

    void addVmToPool(VmPoolMap map);

    List<VmPoolMap> getVmPoolsMapByVmPoolId(NGuid vmPoolId);

    /**
     * Gets the maps of the given pool, for the Vms that are in the given status
     * @param vmPoolId
     * @return
     */
    List<VmPoolMap> getVmMapsInVmPoolByVmPoolIdAndStatus(NGuid vmPoolId, VMStatus vmStatus);

    /**
     * Returns a single VM from the vm pool with the specified id, with optional filtering.
     *
     * @param id
     *            the vm pool id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return a single VM from the pool
     */
    VM getVmDataFromPoolByPoolGuid(Guid vmPoolId, Guid userID, boolean isFiltered);
}
