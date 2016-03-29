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

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.api.model.IpVersion;
import org.ovirt.engine.api.model.OpenStackSubnet;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;
import org.ovirt.engine.api.restapi.types.MappingTestHelper;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;

public class OpenStackSubnetMapperTest
        extends AbstractInvertibleMappingTest<OpenStackSubnet, ExternalSubnet, ExternalSubnet> {
    public OpenStackSubnetMapperTest() {
        super(OpenStackSubnet.class, ExternalSubnet.class, ExternalSubnet.class);
    }

    @Override
    protected OpenStackSubnet postPopulate(OpenStackSubnet model) {
        model.setIpVersion(MappingTestHelper.shuffle(IpVersion.class).value());
        return model;
    }

    @Override
    protected void verify(OpenStackSubnet model, OpenStackSubnet transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getCidr(), transform.getCidr());
        assertEquals(model.getIpVersion(), transform.getIpVersion());
        assertEquals(model.getGateway(), transform.getGateway());
        Set<String> modelDnsServers = new HashSet<>(model.getDnsServers().getDnsServers());
        Set<String> transformDnsServers = new HashSet<>(transform.getDnsServers().getDnsServers());
        assertEquals(modelDnsServers, transformDnsServers);
    }
}
