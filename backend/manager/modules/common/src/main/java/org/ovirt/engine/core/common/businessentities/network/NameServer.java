package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.Ipv4OrIpv6;

public class NameServer implements Serializable {
    @NotNull
    @Ipv4OrIpv6
    private String address;

    //hide me!
    //required due to current static analysis settings.
    private NameServer() {
    }

    public NameServer(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NameServer)) {
            return false;
        }
        NameServer that = (NameServer) o;
        return Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress());
    }

    @Override
    public String toString() {
        return ToStringBuilder.forClass(NameServer.class)
                .append("address", address)
                .build();
    }

}
