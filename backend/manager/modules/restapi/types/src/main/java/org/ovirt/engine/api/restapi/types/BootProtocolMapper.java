package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;

public class BootProtocolMapper {

    @Mapping(from = NetworkBootProtocol.class, to = BootProtocol.class)
    public static BootProtocol map(NetworkBootProtocol networkBootProtocol, BootProtocol template) {
        if(networkBootProtocol!=null){
            switch (networkBootProtocol) {
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

    @Mapping(from = BootProtocol.class, to = NetworkBootProtocol.class)
    public static NetworkBootProtocol map(BootProtocol bootProtocol, NetworkBootProtocol template) {
        if(bootProtocol!=null){
            switch (bootProtocol) {
            case DHCP:
                return NetworkBootProtocol.DHCP;
            case STATIC:
                return NetworkBootProtocol.STATIC_IP;
            case NONE:
                return NetworkBootProtocol.NONE;
            default:
                return null;
            }
        }
        return null;
    }
}
