/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.openstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
