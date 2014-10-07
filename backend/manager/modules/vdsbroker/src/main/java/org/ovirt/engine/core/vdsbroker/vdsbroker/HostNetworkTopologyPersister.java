package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public interface HostNetworkTopologyPersister {

    /**
     * Persist this host network topology to DB. Set the host to non-operational in case its networks don't comply with
     * the cluster rules:
     * <ul>
     * <li>All mandatory networks(optional=false) should be implemented by the host.
     * <li>All VM networks must be implemented with bridges.
     * </ul>
     *
     * @param host
     * @param skipManagementNetwork
     *            if <code>true</code> skip validations for the management network (existence on the host or configured
     *            properly)
     * @param nicsByName
     *            a map of names to their network interfaces. Those nics engine-side properties will not be changed.
     * @return The reason for non-operability of the host or <code>NonOperationalReason.NONE</code>
     */
    NonOperationalReason persistAndEnforceNetworkCompliance(VDS host,
                                                            boolean skipManagementNetwork,
                                                            Map<String, VdsNetworkInterface> nicsByName);

    NonOperationalReason persistAndEnforceNetworkCompliance(VDS host);

}
