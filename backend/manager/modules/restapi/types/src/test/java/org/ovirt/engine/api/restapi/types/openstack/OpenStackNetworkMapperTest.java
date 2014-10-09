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

import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;
import org.ovirt.engine.core.common.businessentities.network.Network;

public class OpenStackNetworkMapperTest
        extends AbstractInvertibleMappingTest<OpenStackNetwork, Network, Network> {
    public OpenStackNetworkMapperTest() {
        super(OpenStackNetwork.class, Network.class, Network.class);
    }

    @Override
    protected void verify(OpenStackNetwork model, OpenStackNetwork transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
    }
}
