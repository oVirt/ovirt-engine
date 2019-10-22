/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.FirewallType;

public class FirewallTypeMapper {
    public static org.ovirt.engine.core.common.network.FirewallType mapFromModel(FirewallType firewallType) {
        switch (firewallType) {
            case IPTABLES:
                return org.ovirt.engine.core.common.network.FirewallType.IPTABLES;
            case FIREWALLD:
                return org.ovirt.engine.core.common.network.FirewallType.FIREWALLD;
            default:
                throw new IllegalArgumentException("Unknown firewall type value: " + firewallType);
        }
    }

    public static FirewallType mapToModel(org.ovirt.engine.core.common.network.FirewallType firewallType) {
        if (firewallType == null) {
            return null;
        }

        switch (firewallType) {
            case IPTABLES:
                return FirewallType.IPTABLES;
            case FIREWALLD:
                return FirewallType.FIREWALLD;
            default:
                throw new IllegalArgumentException("Unknown firewall type value: " + firewallType);
        }
    }
}
