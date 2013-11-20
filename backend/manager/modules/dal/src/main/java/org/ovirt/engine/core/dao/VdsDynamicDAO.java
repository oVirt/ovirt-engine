package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsDynamicDAO</code> defines a type that performs CRUD operations on instances of {@link VDS}.
 *
 *
 */
public interface VdsDynamicDAO extends GenericDao<VdsDynamic, Guid>, StatusAwareDao<Guid, VDSStatus> {

   /**
     * Update entity net_config_dirty field
     * @param id - entity id
     * @param netConfigDirty - a new value of field
     */
    void updateNetConfigDirty(Guid id, Boolean netConfigDirty);

    /**
     * The following method will add a provided values to current values in DB
     * @param id - id of record to be updated
     * @param vmCount - a new value will be GREATEST(vm_count + vmCount, 0)
     * @param pendingVcpusCount - a new value will be GREATEST(pending_vcpus_count + pendingVcpusCount, 0)
     * @param pendingVmemSize - a new value will be GREATEST(pending_vmem_size + pendingVmemSize, 0)
     * @param memCommited - will decrease or increase value of mem_commited by ABS(memCommited) + guest_overhead
     * @param vmsCoresCount - a new value will be GREATEST(vms_cores_count + v_vmsCoresCount, 0)
     */
    void updatePartialVdsDynamicCalc(Guid id, int vmCount, int pendingVcpusCount, int pendingVmemSize, int memCommited, int vmsCoresCount);
}
