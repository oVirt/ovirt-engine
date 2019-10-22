/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import org.ovirt.engine.api.model.ExternalHostGroup;
import org.ovirt.engine.api.restapi.types.AbstractInvertibleMappingTest;

public class ExternalHostGroupMapperTest
        extends AbstractInvertibleMappingTest<
            ExternalHostGroup,
            org.ovirt.engine.core.common.businessentities.ExternalHostGroup,
            org.ovirt.engine.core.common.businessentities.ExternalHostGroup
        > {
    public ExternalHostGroupMapperTest() {
        super(
            ExternalHostGroup.class,
            org.ovirt.engine.core.common.businessentities.ExternalHostGroup.class,
            org.ovirt.engine.core.common.businessentities.ExternalHostGroup.class
        );
    }

    @Override
    protected ExternalHostGroup postPopulate(ExternalHostGroup model) {
        // The id is expected to be the name encoded in hex:
        model.setId(string2hex(model.getName()));
        return model;
    }

    @Override
    protected void verify(ExternalHostGroup model, ExternalHostGroup transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getArchitectureName(), transform.getArchitectureName());
        assertEquals(model.getOperatingSystemName(), transform.getOperatingSystemName());
        assertEquals(model.getDomainName(), transform.getDomainName());
        assertEquals(model.getSubnetName(), transform.getSubnetName());
    }
}
