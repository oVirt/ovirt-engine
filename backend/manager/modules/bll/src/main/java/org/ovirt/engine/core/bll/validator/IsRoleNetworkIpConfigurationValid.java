package org.ovirt.engine.core.bll.validator;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.utils.NetworkUtils;

public class IsRoleNetworkIpConfigurationValid {
    private final NetworkCluster networkCluster;

    public IsRoleNetworkIpConfigurationValid(NetworkCluster networkCluster) {
        this.networkCluster = networkCluster;
    }

    /*
     * must be in sync with {@link #isIpAddressMissingForRole(VdsNetworkInterface)} and
     * {@link #validateAddressesOnExistingNic(VdsNetworkInterface)}
     */
    public boolean validate(NetworkAttachment attachment) {
        if (!NetworkUtils.isRoleNetwork(networkCluster)) {
            return true;
        }

        IpConfiguration ipConfiguration = attachment.getIpConfiguration();
        if (ipConfiguration == null) {
            return false;
        }

        if (networkCluster.isMigration() || networkCluster.isDefaultRoute() || networkCluster.isDisplay() || networkCluster.isGluster()) {
            return hasBootProtocolOtherThanNoneOnIpConfigurationOfAnyIpVersion(ipConfiguration);
        }

        return hasBootProtocolOtherThanNoneOnIpv4(ipConfiguration);
    }

    private boolean hasBootProtocolOtherThanNoneOnIpConfigurationOfAnyIpVersion(IpConfiguration ipConfiguration) {
        return hasBootProtocolOtherThanNoneOnIpv4(ipConfiguration)
                || hasBootProtocolOtherThanNoneOnIpv6(ipConfiguration);
    }

    private boolean hasBootProtocolOtherThanNoneOnIpv4(IpConfiguration ipConfiguration) {
        return ipConfiguration.hasIpv4PrimaryAddressSet()
                && (ipConfiguration.getIpv4PrimaryAddress().getBootProtocol() != Ipv4BootProtocol.NONE);
    }

    private boolean hasBootProtocolOtherThanNoneOnIpv6(IpConfiguration ipConfiguration) {
        return ipConfiguration.hasIpv6PrimaryAddressSet()
                && (ipConfiguration.getIpv6PrimaryAddress().getBootProtocol() != Ipv6BootProtocol.NONE);
    }

    /*
     * must be in sync with {@link #validate(NetworkAttachment)}
     */
    public boolean validateAddressesOnExistingNic(VdsNetworkInterface nic) {
        if (networkCluster.isMigration() || networkCluster.isDefaultRoute()  || networkCluster.isDisplay() || networkCluster.isGluster()) {
            return StringUtils.isNotEmpty(nic.getIpv4Address()) || StringUtils.isNotEmpty(nic.getIpv6Address());
        }
        return StringUtils.isNotEmpty(nic.getIpv4Address());
    }

    public boolean isIpAddressMissingForRole(VdsNetworkInterface nic) {
        return !validateAddressesOnExistingNic(nic);
    }
}
