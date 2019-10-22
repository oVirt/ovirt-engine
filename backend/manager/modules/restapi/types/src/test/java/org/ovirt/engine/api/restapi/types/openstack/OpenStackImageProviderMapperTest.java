/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.openstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.ovirt.engine.api.model.OpenStackImageProvider;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;
import org.ovirt.engine.core.common.businessentities.Provider;

public class OpenStackImageProviderMapperTest
        extends AbstractInvertibleMappingTest<OpenStackImageProvider, Provider, Provider> {
    public OpenStackImageProviderMapperTest() {
        super(OpenStackImageProvider.class, Provider.class, Provider.class);
    }

    @Override
    protected void verify(OpenStackImageProvider model, OpenStackImageProvider transform) {
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
    }
}
