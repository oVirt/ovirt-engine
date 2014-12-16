package org.ovirt.engine.core.common.businessentities.network;

import java.util.Map;


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
    protected Map<String, Object> constructStringAttributes() {
        Map<String, Object> attributes = super.constructStringAttributes();
        attributes.put("macAddress", getMacAddress());
        attributes.put("bondName", getBondName());
        attributes.put("speed", getSpeed());
        attributes.put("labels", getLabels());
        return attributes;
    }
}
