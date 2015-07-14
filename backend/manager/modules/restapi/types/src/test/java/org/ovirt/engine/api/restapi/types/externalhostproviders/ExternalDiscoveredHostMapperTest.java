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

package org.ovirt.engine.api.restapi.types.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import org.ovirt.engine.api.model.ExternalDiscoveredHost;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;

public class ExternalDiscoveredHostMapperTest
        extends AbstractInvertibleMappingTest<
            ExternalDiscoveredHost,
            org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost,
            org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost
        > {
    public ExternalDiscoveredHostMapperTest() {
        super(
            ExternalDiscoveredHost.class,
            org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost.class,
            org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost.class
        );
    }

    @Override
    protected ExternalDiscoveredHost postPopulate(ExternalDiscoveredHost model) {
        // The id is expected to be the name encoded in hex:
        model.setId(string2hex(model.getName()));
        return model;
    }

    @Override
    protected void verify(ExternalDiscoveredHost model, ExternalDiscoveredHost transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getIp(), transform.getIp());
        assertEquals(model.getMac(), transform.getMac());
        assertEquals(model.getSubnetName(), transform.getSubnetName());
        assertEquals(model.getLastReport(), transform.getLastReport());
    }
}
