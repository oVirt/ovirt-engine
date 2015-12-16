package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public interface VmAndTemplatesGenerationsDao extends Dao {

    /**
     * Updates the vms/templates ovf update version to the given value
     *
     *
     * @param ids
     *            - vm/template ids
     * @param values
     *            - new ovf generations
     */
    public void updateOvfGenerations(List<Guid> ids, List<Long> values, List<String> ovfData);

    /**
     * Get the current ovf generation of the vm/template with the given guid.
     */
    public Long getOvfGeneration(Guid id);

    /**
     * Remove the records of the given id from the ovf generations table
     */
    public void deleteOvfGenerations(List<Guid> ids);

    /**
     * Get vm templates ids for ovf update
     */
    public List<Guid> getVmTemplatesIdsForOvfUpdate(Guid storagePoolId);

    /**
     * Get ovf data for the given ids
     */
    public List<Pair<Guid, String>> loadOvfDataForIds(List<Guid> ids);

    /**
     * Get ids for ovf deletion from storage
     */
    public List<Guid> getIdsForOvfDeletion(Guid storagePoolId);

    /**
     * Get ids of vms which were updated in db since last ovf update in a specific storage pool.
     */
    public List<Guid> getVmsIdsForOvfUpdate(Guid storagePoolId);

}
