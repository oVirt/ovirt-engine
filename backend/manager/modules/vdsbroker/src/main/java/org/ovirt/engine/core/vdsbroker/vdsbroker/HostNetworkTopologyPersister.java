package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.UserConfiguredNetworkData;

public interface HostNetworkTopologyPersister {

    /**
     * Persist this host network topology to DB. Set the host to non-operational in case its networks don't comply with
     * the cluster rules:
     * <ul>
     * <li>All mandatory networks(optional=false) should be implemented by the host.</li>
     * <li>All VM networks must be implemented with bridges.</li>
     * </ul>
     *
     * @param skipManagementNetwork
     *            if <code>true</code> skip validations for the management network (existence on the host or configured
     *            properly)
     * @param userConfiguredNetworkData
     *            The network configuration as provided by the user, for which engine managed data will be preserved.
     * @return The reason for non-operability of the host or <code>NonOperationalReason.NONE</code>
     */
    NonOperationalReason persistAndEnforceNetworkCompliance(VDS host,
            boolean skipManagementNetwork,
            UserConfiguredNetworkData userConfiguredNetworkData);

    NonOperationalReason persistAndEnforceNetworkCompliance(VDS host);

}
