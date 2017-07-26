/*
Copyright (c) 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
