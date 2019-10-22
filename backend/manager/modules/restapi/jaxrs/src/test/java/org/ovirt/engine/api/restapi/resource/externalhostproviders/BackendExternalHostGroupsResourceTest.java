/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.ExternalHostGroup;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExternalHostGroupsResourceTest
        extends AbstractBackendCollectionResourceTest<
            ExternalHostGroup,
            org.ovirt.engine.core.common.businessentities.ExternalHostGroup,
        BackendExternalHostGroupsResource
        > {
    public BackendExternalHostGroupsResourceTest() {
        super(
            new BackendExternalHostGroupsResource(GUIDS[0].toString()),
            null,
            ""
        );
    }

    @Override
    protected List<ExternalHostGroup> getCollection() {
        return collection.list().getExternalHostGroups();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(
            QueryType.GetProviderById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getProvider()
        );
        setUpEntityQueryExpectations(
            QueryType.GetHostGroupsFromExternalProvider,
            ProviderQueryParameters.class,
            new String[] { "Provider.Id" },
            new Object[] { GUIDS[0] },
            getGroups(),
            failure
        );
    }

    private Provider getProvider() {
        Provider provider = mock(Provider.class);
        when(provider.getId()).thenReturn(GUIDS[0]);
        when(provider.getName()).thenReturn(NAMES[0]);
        return provider;
    }

    private List<org.ovirt.engine.core.common.businessentities.ExternalHostGroup> getGroups() {
        List<org.ovirt.engine.core.common.businessentities.ExternalHostGroup> groups = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            groups.add(getEntity(i));
        }
        return groups;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.ExternalHostGroup getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.ExternalHostGroup group =
                mock(org.ovirt.engine.core.common.businessentities.ExternalHostGroup.class);
        when(group.getName()).thenReturn(NAMES[index]);
        return group;
    }

    @Override
    protected void verifyModel(ExternalHostGroup model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
