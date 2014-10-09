/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.AgentConfiguration;
import org.ovirt.engine.api.model.MessageBrokerType;
import org.ovirt.engine.api.model.NetworkPluginType;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;

@ValidatedClass(clazz = OpenStackNetworkProvider.class)
public class OpenStackNetworkProviderValidator implements Validator<OpenStackNetworkProvider> {
    @Override
    public void validateEnums(OpenStackNetworkProvider provider) {
        if (provider.isSetPluginType()) {
            validateEnum(NetworkPluginType.class, provider.getPluginType());
        }
        AgentConfiguration agentConfiguration = provider.getAgentConfiguration();
        if (agentConfiguration != null) {
            if (agentConfiguration.isSetBrokerType()) {
                validateEnum(MessageBrokerType.class, agentConfiguration.getBrokerType(), true);
            }
        }
    }
}
