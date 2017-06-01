package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code VdsDynamicDao} defines a type that performs CRUD operations on instances of {@link VdsDynamic}.
 */
public interface VdsDynamicDao extends GenericDao<VdsDynamic, Guid>, StatusAwareDao<Guid, VDSStatus>, ExternalStatusAwareDao<Guid, ExternalStatus>, MassOperationsDao<VdsDynamic, Guid>, CheckedUpdate<VdsDynamic> {

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

    void updateCpuFlags(Guid id, String cpuFlags);

    /**
     * Retrieves all host ids of hosts that are in given status
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

    /**
     * Updates the status and the reasons (maintenance and non-operational) for the given host
     *
     * @param host
     *            the host to be updated
     */
     void updateStatusAndReasons(VdsDynamic host);

    /**
     * Checks if exists a host with the given status in the given cluster.
     *
     * @param clusterId
     *            cluster id
     * @param hostStatus
     *            hosts status
     * @return <code>true</code> if such a host exists, otherwise <code>false</code>.
     */
    boolean checkIfExistsHostWithStatusInCluster(Guid clusterId, VDSStatus hostStatus);

    /**
     * Updates {@link DnsResolverConfiguration} belonging to host of given {@code vdsId}
     * @param vdsId id of host
     * @param reportedDnsResolverConfiguration dns resolver configuration to update existing {@link VdsDynamic} record.
     */
    void updateDnsResolverConfiguration(Guid vdsId, DnsResolverConfiguration reportedDnsResolverConfiguration);

}
