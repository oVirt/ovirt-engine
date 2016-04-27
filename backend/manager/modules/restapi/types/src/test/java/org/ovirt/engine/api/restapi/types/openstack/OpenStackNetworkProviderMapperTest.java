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

package org.ovirt.engine.api.restapi.types.openstack;

import org.ovirt.engine.api.model.AgentConfiguration;
import org.ovirt.engine.api.model.MessageBrokerType;
import org.ovirt.engine.api.model.NetworkPluginType;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackNetworkProviderType;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;
import org.ovirt.engine.api.restapi.types.MappingTestHelper;
import org.ovirt.engine.core.common.businessentities.Provider;

public class OpenStackNetworkProviderMapperTest
        extends AbstractInvertibleMappingTest<OpenStackNetworkProvider, Provider, Provider> {
    public OpenStackNetworkProviderMapperTest() {
        super(OpenStackNetworkProvider.class, Provider.class, Provider.class);
    }

    @Override
    protected OpenStackNetworkProvider postPopulate(OpenStackNetworkProvider model) {
        model.setType(OpenStackNetworkProviderType.NEUTRON);
        model.setPluginType(MappingTestHelper.shuffle(NetworkPluginType.class));
        AgentConfiguration agentConfiguration = model.getAgentConfiguration();
        agentConfiguration.setBrokerType(MappingTestHelper.shuffle(MessageBrokerType.class));
        return model;
    }

    @Override
    protected void verify(OpenStackNetworkProvider model, OpenStackNetworkProvider transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.isRequiresAuthentication(), transform.isRequiresAuthentication());
        assertEquals(model.getUrl(), transform.getUrl());
        assertEquals(model.getUsername(), transform.getUsername());
        // The password isn't mapped for security reasons.
        assertNull(transform.getPassword());
        assertEquals(model.getTenantName(), transform.getTenantName());
        assertEquals(model.getPluginType(), transform.getPluginType());
        assertEquals(model.getType(), transform.getType());
        verify(model.getAgentConfiguration(), transform.getAgentConfiguration());
    }

    private void verify(AgentConfiguration model, AgentConfiguration transform) {
        assertEquals(model.getNetworkMappings(), transform.getNetworkMappings());
        assertEquals(model.getBrokerType(), transform.getBrokerType());
        assertEquals(model.getAddress(), transform.getAddress());
        assertEquals(model.getPort(), transform.getPort());
        assertEquals(model.getUsername(), transform.getUsername());
        // The password isn't mapped for security reasons.
        assertNull(transform.getPassword());
    }
}
