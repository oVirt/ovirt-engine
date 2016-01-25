/*
Copyright (c) 2016 Red Hat, Inc.

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
