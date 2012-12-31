package org.ovirt.engine.core.common.businessentities.network;


public class Nic extends VdsNetworkInterface {

    private static final long serialVersionUID = 1674504258368214225L;

    public Nic() {
    }

    public Nic(String macAddress, Integer speed, String bondName) {
        setMacAddress(macAddress);
        setSpeed(speed);
        setBondName(bondName);
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
                .append(", macAddress=")
                .append(getMacAddress())
                .append(", networkName=")
                .append(getNetworkName())
                .append(", bondName=")
                .append(getBondName())
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
                .append(", speed=")
                .append(getSpeed())
                .append(", type=")
                .append(getType())
                .append(", networkImplementationDetails=")
                .append(getNetworkImplementationDetails())
                .append("}");
        return builder.toString();
    }
}
