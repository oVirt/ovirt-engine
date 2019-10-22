/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.AgentConfiguration;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3AgentConfiguration;

public class V3AgentConfigurationOutAdapter implements V3Adapter<AgentConfiguration, V3AgentConfiguration> {
    @Override
    public V3AgentConfiguration adapt(AgentConfiguration from) {
        V3AgentConfiguration to = new V3AgentConfiguration();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetBrokerType()) {
            to.setBrokerType(from.getBrokerType().value());
        }
        if (from.isSetNetworkMappings()) {
            to.setNetworkMappings(from.getNetworkMappings());
        }
        if (from.isSetPassword()) {
            to.setPassword(from.getPassword());
        }
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetUsername()) {
            to.setUsername(from.getUsername());
        }
        return to;
    }
}
