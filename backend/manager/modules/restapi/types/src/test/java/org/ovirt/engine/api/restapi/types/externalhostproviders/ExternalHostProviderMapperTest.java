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
