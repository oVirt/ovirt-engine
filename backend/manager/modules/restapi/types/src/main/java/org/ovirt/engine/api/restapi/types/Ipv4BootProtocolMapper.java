package org.ovirt.engine.api.restapi.types;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;

public class Ipv4BootProtocolMapper {

    public static BootProtocol map(Ipv4BootProtocol ipv4BootProtocol) {
        if (ipv4BootProtocol == null) {
            return null;
        }
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

    public static Ipv4BootProtocol map(BootProtocol bootProtocol) {
        if (bootProtocol == null) {
            return null;
        }
        switch (bootProtocol) {
        case DHCP:
            return Ipv4BootProtocol.DHCP;
        case STATIC:
            return Ipv4BootProtocol.STATIC_IP;
        case NONE:
            return Ipv4BootProtocol.NONE;
        default:
            throw new WebApplicationException(
                    Response.status(Status.BAD_REQUEST)
                            .entity(
                                    fault("Invalid value",
                                            String.format("Boot protocol = '%s' is not applicable for IPv4.",
                                                    bootProtocol)))
                            .build());
        }
    }

    private static Fault fault(String reason, String detail) {
        Fault fault = new Fault();
        fault.setReason(reason);
        fault.setDetail(detail);
        return fault;
    }
}
