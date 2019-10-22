/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
