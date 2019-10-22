/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.OpenStackImageProvider;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.interfaces.SearchType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendOpenStackImageProvidersResourceTest extends
        AbstractBackendCollectionResourceTest<OpenStackImageProvider, Provider, BackendOpenStackImageProvidersResource> {

    public BackendOpenStackImageProvidersResourceTest() {
        super(
            new BackendOpenStackImageProvidersResource(),
            SearchType.Provider,
            "Providers: type=" + ProviderType.OPENSTACK_IMAGE.name()
        );
    }

    @Override
    protected List<OpenStackImageProvider> getCollection() {
        return collection.list().getOpenStackImageProviders();
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        if (StringUtils.isNotBlank(query)) {
            query = " AND " + query;
        }
        super.setUpQueryExpectations(query);
    }

    @Override
    protected Provider getEntity(int index) {
        Provider provider = mock(Provider.class);
        when(provider.getId()).thenReturn(GUIDS[index]);
        when(provider.getName()).thenReturn(NAMES[index]);
        when(provider.getDescription()).thenReturn(DESCRIPTIONS[index]);
        return provider;
    }
}
