/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.openstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.api.model.IpVersion;
import org.ovirt.engine.api.model.OpenStackSubnet;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;

public class OpenStackSubnetMapperTest
        extends AbstractInvertibleMappingTest<OpenStackSubnet, ExternalSubnet, ExternalSubnet> {
    public OpenStackSubnetMapperTest() {
        super(OpenStackSubnet.class, ExternalSubnet.class, ExternalSubnet.class);
    }

    @Override
    protected OpenStackSubnet postPopulate(OpenStackSubnet model) {
        model.setIpVersion(IpVersion.V4.value());
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
