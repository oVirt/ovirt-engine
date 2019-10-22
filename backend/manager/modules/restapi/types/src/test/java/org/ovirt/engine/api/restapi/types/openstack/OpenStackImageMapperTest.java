/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.openstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.OpenStackImage;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;

public class OpenStackImageMapperTest
        extends AbstractInvertibleMappingTest<OpenStackImage, RepoImage, RepoImage> {
    public OpenStackImageMapperTest() {
        super(OpenStackImage.class, RepoImage.class, RepoImage.class);
    }

    @Override
    protected void verify(OpenStackImage model, OpenStackImage transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
    }
}
