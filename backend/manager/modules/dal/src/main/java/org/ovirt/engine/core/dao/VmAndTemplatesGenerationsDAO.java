package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public interface VmAndTemplatesGenerationsDAO extends DAO {

    /**
     * Updates the vms/templates ovf update version to the given value
     *
     * @param ids
     *            - vm/template ids
     * @param values
     *            - new ovf generations
     * @return
     */
    public void updateOvfGenerations(List<Guid> ids, List<Long> values);

    /**
     * Get the current ovf generation of the vm/template with the given guid.
     *
     * @param id
     */

    public Long getOvfGeneration(Guid id);

    /**
     * Remove the records of the given id from the ovf generations table
     *
     * @param ids
     */
    public void deleteOvfGenerations(List<Guid> ids);

    /**
     * Get vm templates ids for ovf update
     *
     * @param storagePoolId
     * @return
     */
    public List<Guid> getVmTemplatesIdsForOvfUpdate(Guid storagePoolId);

    /**
     * Get ids for ovf deletion from storage
     *
     * @param storagePoolId
     * @return
     */
    public List<Guid> getIdsForOvfDeletion(Guid storagePoolId);

    /**
     * Get ids of vms which were updated in db since last ovf update in a specific storage pool.
     *
     * @param storagePoolId
     * @return
     */
    public List<Guid> getVmsIdsForOvfUpdate(Guid storagePoolId);

}
