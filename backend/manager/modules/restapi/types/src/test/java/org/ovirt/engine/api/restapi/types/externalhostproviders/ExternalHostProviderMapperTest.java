/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;
import org.ovirt.engine.core.common.businessentities.Provider;

public class ExternalHostProviderMapperTest
        extends AbstractInvertibleMappingTest<ExternalHostProvider, Provider, Provider> {
    public ExternalHostProviderMapperTest() {
        super(ExternalHostProvider.class, Provider.class, Provider.class);
    }

    @Override
    protected void verify(ExternalHostProvider model, ExternalHostProvider transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.isRequiresAuthentication(), transform.isRequiresAuthentication());
        assertEquals(model.getUrl(), transform.getUrl());
        assertEquals(model.getUsername(), transform.getUsername());
        // The password isn't mapped for security reasons.
        assertNull(transform.getPassword());
    }
}
