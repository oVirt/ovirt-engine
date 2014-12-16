package org.ovirt.engine.core.common.businessentities.network;

import java.util.Map;


public class Vlan extends VdsNetworkInterface {

    private static final long serialVersionUID = -2458958954004227402L;

    public Vlan() {
    }

    public Vlan(int vlanId, String baseInterface) {
        setVlanId(vlanId);
        setBaseInterface(baseInterface);
        setName(baseInterface + "." + vlanId);
    }

    @Override
    protected Map<String, Object> constructStringAttributes() {
        Map<String, Object> attributes = super.constructStringAttributes();
        attributes.put("baseInterface", getBaseInterface());
        attributes.put("vlanId", getVlanId());
        return attributes;
    }
}
