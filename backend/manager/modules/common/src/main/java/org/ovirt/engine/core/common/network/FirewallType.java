/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.common.network;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum FirewallType implements Identifiable {

    IPTABLES(0),
    FIREWALLD(1);

    private int value;
    private static Map<Integer, FirewallType> valuesById = new HashMap<>();

    static {
        for (FirewallType type : FirewallType.values()) {
            valuesById.put(type.value, type);
        }
    }

    FirewallType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FirewallType valueOf(int value) {
        FirewallType firewallType = valuesById.get(value);
        if (firewallType != null) {
            return firewallType;
        }

        throw new IllegalArgumentException("FirewallType does not have any mapping for value: " + value);
    }
}
