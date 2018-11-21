package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * The protocol of the input of the network configuration to cloud-init.
 * Cloud-init supports several source protocols as described in
 * http://cloudinit.readthedocs.io/en/latest/topics/network-config.html#network-configuration-sources
 *
 * ENI has become a legacy protocol that cannot support IPv6.
 * The Openstack Metadata protocol is a successor. It is described in
 * https://specs.openstack.org/openstack/nova-specs/specs/liberty/implemented/metadata-service-network-info.html
 * </pre>
 */
public enum CloudInitNetworkProtocol {
    OPENSTACK_METADATA(0),
    ENI(1);

    private static Map<Integer, CloudInitNetworkProtocol> mappings;

    static {
        mappings = new HashMap<>();
        for (CloudInitNetworkProtocol value : values()) {
            mappings.put(value.getValue(), value);
        }
    }

    private int intValue;
    private String displayName;

    CloudInitNetworkProtocol(int intValue) {
        init(intValue, name().toLowerCase());
    }

    public static CloudInitNetworkProtocol forValue(int value) {
        return mappings.get(value);
    }

    private void init(int intValue, String displayName) {
        this.intValue = intValue;
        this.displayName = displayName;
    }

    public int getValue() {
        return intValue;
    }

    public String getDisplayName() {
        return displayName;
    }
}
