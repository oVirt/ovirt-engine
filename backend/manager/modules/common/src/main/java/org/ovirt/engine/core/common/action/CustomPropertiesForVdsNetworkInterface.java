package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

/**
 * this class holds map of custom properties of multiple nics. Custom properties map may be, or may not be set.
 */
public class CustomPropertiesForVdsNetworkInterface {
    Map<String, Map<String, String>> customProperties;

    public CustomPropertiesForVdsNetworkInterface() {
        customProperties = new HashMap<>();
    }

    public CustomPropertiesForVdsNetworkInterface add(VdsNetworkInterface nic, Map<String, String> customProperties) {
        return add(getKey(nic), customProperties);
    }

    public CustomPropertiesForVdsNetworkInterface add(String name, Map<String, String> customProperties) {
        Objects.requireNonNull(name);

        if (customProperties == null) {
            customProperties = new HashMap<>();
        }

        this.customProperties.put(name, customProperties);
        return this;
    }

    public Map<String, String> getCustomPropertiesFor(VdsNetworkInterface nic) {
        if (hasCustomPropertiesFor(nic)) {
            return customProperties.get(getKey(nic));
        } else {
            return null;
        }
    }

    public boolean hasCustomPropertiesFor(VdsNetworkInterface nic) {
        return this.customProperties.containsKey(getKey(nic));
    }

    private String getKey(VdsNetworkInterface nic) {
        return nic.getName();
    }
}
