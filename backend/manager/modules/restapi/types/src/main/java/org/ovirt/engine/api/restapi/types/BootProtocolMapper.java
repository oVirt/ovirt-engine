package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;

public class BootProtocolMapper {

    @Mapping(from = Ipv4BootProtocol.class, to = BootProtocol.class)
    public static BootProtocol map(Ipv4BootProtocol ipv4BootProtocol, BootProtocol template) {
        if(ipv4BootProtocol !=null){
            switch (ipv4BootProtocol) {
            case DHCP:
                return BootProtocol.DHCP;
            case STATIC_IP:
                return BootProtocol.STATIC;
            case NONE:
                return BootProtocol.NONE;
            default:
                return null;
            }
        }
        return null;
    }

    @Mapping(from = BootProtocol.class, to = Ipv4BootProtocol.class)
    public static Ipv4BootProtocol map(BootProtocol bootProtocol, Ipv4BootProtocol template) {
        if(bootProtocol!=null){
            switch (bootProtocol) {
            case DHCP:
                return Ipv4BootProtocol.DHCP;
            case STATIC:
                return Ipv4BootProtocol.STATIC_IP;
            case NONE:
                return Ipv4BootProtocol.NONE;
            default:
                return null;
            }
        }
        return null;
    }
}
