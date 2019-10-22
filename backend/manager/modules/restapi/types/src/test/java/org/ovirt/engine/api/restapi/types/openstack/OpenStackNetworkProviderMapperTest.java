/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.openstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.ovirt.engine.api.model.NetworkPluginType;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackNetworkProviderType;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;
import org.ovirt.engine.core.common.businessentities.Provider;

public class OpenStackNetworkProviderMapperTest
        extends AbstractInvertibleMappingTest<OpenStackNetworkProvider, Provider, Provider> {
    public OpenStackNetworkProviderMapperTest() {
        super(OpenStackNetworkProvider.class, Provider.class, Provider.class);
    }

    @Override
    protected OpenStackNetworkProvider postPopulate(OpenStackNetworkProvider model) {
        model.setType(OpenStackNetworkProviderType.NEUTRON);
        model.setPluginType(NetworkPluginType.OPEN_VSWITCH);
        model.setAutoSync(true);
        model.setReadOnly(false);
        model.setUnmanaged(true);
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
        assertEquals(model.getExternalPluginType(), transform.getExternalPluginType());
        assertEquals(model.getType(), transform.getType());
        assertEquals(model.isAutoSync(), transform.isAutoSync());
        assertEquals(model.isReadOnly(), transform.isReadOnly());
        assertEquals(model.isUnmanaged(), transform.isUnmanaged());
    }
}
