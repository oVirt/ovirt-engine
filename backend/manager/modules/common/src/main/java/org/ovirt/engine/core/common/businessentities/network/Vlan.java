package org.ovirt.engine.core.common.businessentities.network;


public class Vlan extends VdsNetworkInterface {

    private static final long serialVersionUID = -2458958954004227402L;

    public Vlan() {
    }

    public Vlan(int vlanId) {
        setVlanId(vlanId);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName())
                .append(" {id=")
                .append(getId())
                .append(", vdsId=")
                .append(getVdsId())
                .append(", name=")
                .append(getName())
                .append(", networkName=")
                .append(getNetworkName())
                .append(", vlanId=")
                .append(getVlanId())
                .append(", bootProtocol=")
                .append(getBootProtocol())
                .append(", address=")
                .append(getAddress())
                .append(", subnet=")
                .append(getSubnet())
                .append(", gateway=")
                .append(getGateway())
                .append(", mtu=")
                .append(getMtu())
                .append(", bridged=")
                .append(isBridged())
                .append(", type=")
                .append(getType())
                .append(", networkImplementationDetails=")
                .append(getNetworkImplementationDetails())
                .append("}");
        return builder.toString();
    }
}
