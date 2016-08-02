package org.ovirt.engine.core.bll.host;

import java.util.EnumSet;

import org.ovirt.engine.core.common.HostUpgradeManagerResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;


/**
 * {@code UpdateAvailable} represents the ability of its implementing class to examine if updates are available for a
 * the given host
 */
public interface UpdateAvailable {

    /**
     * Checks if a host has an available updates
     *
     * @param host
     *            The examined host
     * @return {@code HostUpgradeManagerResult} the result of host upgrade check
     */
    HostUpgradeManagerResult checkForUpdates(VDS host);

    /**
     * @return the host types
     */
    EnumSet<VDSType> getHostTypes();
}
