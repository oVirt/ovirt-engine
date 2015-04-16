package org.ovirt.engine.core.common.businessentities.network;

import org.ovirt.engine.core.common.utils.ToStringBuilder;


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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("macAddress", getMacAddress())
                .append("bondName", getBondName())
                .append("speed", getSpeed())
                .append("labels", getLabels());
    }
}
