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

package org.ovirt.engine.api.restapi.types.externalhostproviders;

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
