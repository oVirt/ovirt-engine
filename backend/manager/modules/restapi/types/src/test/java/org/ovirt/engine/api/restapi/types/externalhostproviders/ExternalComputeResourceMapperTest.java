/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import org.ovirt.engine.api.model.ExternalComputeResource;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;

public class ExternalComputeResourceMapperTest
        extends AbstractInvertibleMappingTest<
            ExternalComputeResource,
            org.ovirt.engine.core.common.businessentities.ExternalComputeResource,
            org.ovirt.engine.core.common.businessentities.ExternalComputeResource
        > {
    public ExternalComputeResourceMapperTest() {
        super(
            ExternalComputeResource.class,
            org.ovirt.engine.core.common.businessentities.ExternalComputeResource.class,
            org.ovirt.engine.core.common.businessentities.ExternalComputeResource.class
        );
    }

    @Override
    protected ExternalComputeResource postPopulate(ExternalComputeResource model) {
        // The id is expected to be the name encoded in hex:
        model.setId(string2hex(model.getName()));
        return model;
    }

    @Override
    protected void verify(ExternalComputeResource model, ExternalComputeResource transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getProvider(), transform.getProvider());
        assertEquals(model.getUser(), transform.getUser());
        assertEquals(model.getUrl(), transform.getUrl());
    }
}
