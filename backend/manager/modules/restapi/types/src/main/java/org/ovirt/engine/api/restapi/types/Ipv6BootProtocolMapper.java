package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;

public class Ipv6BootProtocolMapper {

    public static BootProtocol map(Ipv6BootProtocol entity) {
        if (entity == null) {
            return null;
        }
        switch (entity) {
        case AUTOCONF:
            return BootProtocol.AUTOCONF;
        case DHCP:
            return BootProtocol.DHCP;
        case POLY_DHCP_AUTOCONF:
            return BootProtocol.POLY_DHCP_AUTOCONF;
        case NONE:
            return BootProtocol.NONE;
        case STATIC_IP:
            return BootProtocol.STATIC;
        default:
            return null;
        }
    }

    public static Ipv6BootProtocol map(BootProtocol model) {
        if (model == null) {
            return null;
        }
        switch (model) {
        case AUTOCONF:
            return Ipv6BootProtocol.AUTOCONF;
        case DHCP:
            return Ipv6BootProtocol.DHCP;
        case POLY_DHCP_AUTOCONF:
            return Ipv6BootProtocol.POLY_DHCP_AUTOCONF;
        case NONE:
            return Ipv6BootProtocol.NONE;
        case STATIC:
            return Ipv6BootProtocol.STATIC_IP;
        default:
            return null;
        }
    }
}
