package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsDynamicDAO</code> defines a type that performs CRUD operations on instances of {@link VDS}.
 *
 *
 */
public interface VdsDynamicDAO extends GenericDao<VdsDynamic, Guid>, StatusAwareDao<Guid, VDSStatus>, MassOperationsDao<VdsDynamic, Guid>, CheckedUpdate<VdsDynamic> {

   /**
     * Update entity net_config_dirty field
     * @param id - entity id
     * @param netConfigDirty - a new value of field
     */
    void updateNetConfigDirty(Guid id, Boolean netConfigDirty);

    /**
     * This method will update the controlled_by_pm_policy flag in DB.
     * @param id - id or record to be updated
     * @param controlledByPmPolicy - a new value for the flag
     */
    void updateVdsDynamicPowerManagementPolicyFlag(Guid id, boolean controlledByPmPolicy);

    /**
     * @param id
     * @param cpuFlags
     */
    void updateCpuFlags(Guid id, String cpuFlags);

    /**
     * Retrieves all host ids of hosts that are in given status
     * @param status
     * @return list of host ids
     */
    List<Guid> getIdsOfHostsWithStatus(VDSStatus status);

    /**
     * Updates the updateAvaiable flag of the given host
     *
     * @param id
     *            the ID of the updates host
     * @param updateAvailable
     *            the new value to be updated
     */
    void updateUpdateAvailable(Guid id, boolean updateAvailable);
}
