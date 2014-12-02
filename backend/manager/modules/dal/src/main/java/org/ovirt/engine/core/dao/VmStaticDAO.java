package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public interface VmStaticDAO extends GenericDao<VmStatic, Guid> {
    /**
     * Gets all static VMs by name.
     *
     * @param name
     *            the vm name
     * @return the list of vms
     */
    List<VmStatic> getAllByName(String name);

    /**
     * Gets all static VMs by Storage Pool Id.
     *
     * @param spId
     *            storage pool id
     * @return the list of vms
     */
    List<VmStatic> getAllByStoragePoolId(Guid spId);

    /**
     * Retrieves all static VMs for the specified VDS group.
     *
     * @param vdsGroup
     *            the VDS group
     * @return the list of VMs
     */
    List<VmStatic> getAllByVdsGroup(Guid vdsGroup);

    /**
     * Not really sure what this method's doing.
     *
     * @param vds
     *            the VDS id
     * @return the list of VMs
     */
    List<VmStatic> getAllWithFailbackByVds(Guid vds);

    /**
     * Retrieves all static VMs for the specified group and name.
     *
     * @param group
     *            the group
     * @param name
     *            the name
     * @return the list of vms
     */
    List<VmStatic> getAllByGroupAndNetworkName(Guid group, String name);

    /**
     * Get the names of VMs pinned to the specified host.
     *
     * @param host
     *            The host's id.
     * @return The names of the VMs which are pinned to the host, or empty if none.
     */
    List<String> getAllNamesPinnedToHost(Guid host);

    /**
     * get the db generation for vm/template with the given guid
     *
     * @param id - vm/template id
     * @return
     */
    public Long getDbGeneration(Guid id);

    /**
     * Increment the db version for all vms/templates in a specific storage pool.
     * @param storagePoolId
     */
    public void incrementDbGenerationForAllInStoragePool(Guid storagePoolId);

    /**
     * increment by 1 the generation of the vm/template with the given guid.
     *
     * @param id - vm/template id
     * @return
     */
    public void incrementDbGeneration(Guid id);

    List<Guid> getOrderedVmGuidsForRunMultipleActions(List<Guid> guids);

    /**
     * remove with optionally remove/keep vm permissions
     * @param id vm to remove
     * @param removePermissions flag to indicate if to remove the permissions or keep them
     */
    public void remove(Guid id, boolean removePermissions);

    /**
     * Retrieves all the ids of the vms and templates that have no attached disks matching the provided criteria.
     * @param shareableDisks  check for attached shareable disks
     */
    public List<Guid> getVmAndTemplatesIdsWithoutAttachedImageDisks(boolean shareableDisks);

    /**
     * update vm_static.cpu_profile_id for cluster
     *
     * @param clusterId
     * @param cpuProfileId
     */
    void updateVmCpuProfileIdForClusterId(Guid clusterId, Guid cpuProfileId);
}
