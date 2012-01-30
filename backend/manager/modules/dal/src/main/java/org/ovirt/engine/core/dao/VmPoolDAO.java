package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

/**
 * <code>VmPoolDAO</code> defines a type that performs CRUD operations on instances of {@link vm_pool}.
 *
 */
public interface VmPoolDAO extends DAO, SearchDAO<vm_pools>{
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
     * Gets all VM pools for the specified AD group.
     *
     * @param adGroup
     *            the AD group
     * @return the list of pools
     */
    List<vm_pools> getAllForAdGroup(Guid adGroup);

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

    // TODO APIS to be moved to hibernate relationships

    vm_pool_map getVmPoolMapByVmGuid(Guid vmId);

    void addVmToPool(vm_pool_map map);

    List<vm_pool_map> getVmPoolsMapByVmPoolId(NGuid vmPoolId);

    time_lease_vm_pool_map getTimeLeasedVmPoolMapByIdForVmPool(Guid id, NGuid vmPoolId);

    void addTimeLeasedVmPoolMap(time_lease_vm_pool_map map);

    void updateTimeLeasedVmPoolMap(time_lease_vm_pool_map map);

    void removeTimeLeasedVmPoolMap(Guid id, Guid vmPoolId);

    List<time_lease_vm_pool_map> getAllTimeLeasedVmPoolMaps();
}
