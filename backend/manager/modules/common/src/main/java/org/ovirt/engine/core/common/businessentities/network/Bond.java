package org.ovirt.engine.core.common.businessentities.network;

public class Bond extends VdsNetworkInterface {

    private static final long serialVersionUID = 268337006285648461L;

    public Bond() {
        setBonded(true);
    }

    public Bond(String macAddress, String bondOptions, Integer bondType) {
        this();
        setMacAddress(macAddress);
        setBondOptions(bondOptions);
        setBondType(bondType);
    }

    public Bond(String name) {
        this();
        setName(name);
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
                .append(", bondOptions=")
                .append(getBondOptions())
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
