package org.ovirt.engine.core.common.businessentities.network;

import java.util.Map;

import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;

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
    @Pattern(regexp = BusinessEntitiesDefinitions.BOND_NAME_PATTERN, message = "NETWORK_BOND_NAME_BAD_FORMAT")
    public String getName() {
        return super.getName();
    }

    @Override
    protected Map<String, Object> constructStringAttributes() {
        Map<String, Object> attributes = super.constructStringAttributes();
        attributes.put("macAddress", getMacAddress());
        attributes.put("bondOptions", getBondOptions());
        attributes.put("labels", getLabels());
        return attributes;
    }
}
