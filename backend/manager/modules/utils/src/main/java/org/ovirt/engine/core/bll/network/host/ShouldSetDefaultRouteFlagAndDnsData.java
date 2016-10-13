package org.ovirt.engine.core.bll.network.host;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;

public class ShouldSetDefaultRouteFlagAndDnsData {

    private static final Set<Ipv6BootProtocol> IPV6_AUTO_BOOT_PROTOCOL =
            EnumSet.of(Ipv6BootProtocol.DHCP, Ipv6BootProtocol.AUTOCONF);

    /**
     * @param isDefaultRoute network related to param {@code networkAttachment} has defaultRoute role.
     * @param networkAttachment {@link NetworkAttachment} related to some {@link Network}, same as in first parameter.
     * @return true if this network is the one, which should be used as default route, and dns data should be obtained
     * from it.
     */
    public boolean test(boolean isDefaultRoute, NetworkAttachment networkAttachment) {
        IpConfiguration ipConfiguration = networkAttachment.getIpConfiguration();
        return isDefaultRoute
                && ipConfiguration != null
                && (isIpv4GatewaySet(ipConfiguration) || isIpv6GatewaySet(ipConfiguration));
    }

    private boolean isIpv4GatewaySet(IpConfiguration ipConfiguration) {
        return ipConfiguration.hasIpv4PrimaryAddressSet()
                && (ipConfiguration.getIpv4PrimaryAddress().getBootProtocol() == Ipv4BootProtocol.DHCP
                || ipConfiguration.getIpv4PrimaryAddress().getBootProtocol() == Ipv4BootProtocol.STATIC_IP
                && StringUtils.isNotEmpty(ipConfiguration.getIpv4PrimaryAddress().getGateway()));
    }

    private boolean isIpv6GatewaySet(IpConfiguration ipConfiguration) {
        return ipConfiguration.hasIpv6PrimaryAddressSet()
                && (IPV6_AUTO_BOOT_PROTOCOL.contains(ipConfiguration.getIpv6PrimaryAddress().getBootProtocol())
                || ipConfiguration.getIpv6PrimaryAddress().getBootProtocol() == Ipv6BootProtocol.STATIC_IP
                && StringUtils.isNotEmpty(ipConfiguration.getIpv6PrimaryAddress().getGateway()));
    }
}
