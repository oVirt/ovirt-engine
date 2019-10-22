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
import org.ovirt.engine.api.model.ExternalHost;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExternalHostsResourceTest
        extends AbstractBackendCollectionResourceTest<
            ExternalHost,
            VDS,
        BackendExternalHostsResource
        > {
    public BackendExternalHostsResourceTest() {
        super(
            new BackendExternalHostsResource(GUIDS[0].toString()),
            null,
            ""
        );
    }

    @Override
    protected List<ExternalHost> getCollection() {
        return collection.list().getExternalHosts();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(
            QueryType.GetHostListFromExternalProvider,
            GetHostListFromExternalProviderParameters.class,
            new String[] { "ProviderId" },
            new Object[] { GUIDS[0] },
            getHosts(),
            failure
        );
    }

    private List<VDS> getHosts() {
        List<VDS> hosts = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            hosts.add(getEntity(i));
        }
        return hosts;
    }

    @Override
    protected VDS getEntity(int index) {
        VDS host = mock(VDS.class);
        when(host.getId()).thenReturn(GUIDS[index]);
        when(host.getName()).thenReturn(NAMES[index]);
        return host;
    }

    @Override
    protected void verifyModel(ExternalHost model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
