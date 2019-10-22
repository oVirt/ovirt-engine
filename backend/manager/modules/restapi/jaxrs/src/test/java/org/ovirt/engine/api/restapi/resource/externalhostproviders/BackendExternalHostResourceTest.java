/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.ExternalHost;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExternalHostResourceTest
        extends AbstractBackendSubResourceTest<
            ExternalHost,
            VDS,
        BackendExternalHostResource
        > {
    public BackendExternalHostResourceTest() {
        super(new BackendExternalHostResource(string2hex(NAMES[1]), GUIDS[0].toString()));
    }

    @Test
    public void testBadId() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendExternalHostProviderResource("foo")));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);
        verifyModel(resource.get(), 1);
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
        VDS provider = mock(VDS.class);
        when(provider.getId()).thenReturn(GUIDS[index]);
        when(provider.getName()).thenReturn(NAMES[index]);
        return provider;
    }

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpGetEntityExpectations(
            QueryType.GetHostListFromExternalProvider,
            GetHostListFromExternalProviderParameters.class,
            new String[] { "ProviderId" },
            new Object[] { GUIDS[0] },
            notFound? null: getHosts()
        );
    }

    @Override
    protected void verifyModel(ExternalHost model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
