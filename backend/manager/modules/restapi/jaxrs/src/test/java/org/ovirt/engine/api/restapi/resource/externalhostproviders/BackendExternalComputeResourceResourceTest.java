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
import org.ovirt.engine.api.model.ExternalComputeResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExternalComputeResourceResourceTest
        extends AbstractBackendSubResourceTest<
            ExternalComputeResource,
            org.ovirt.engine.core.common.businessentities.ExternalComputeResource,
            BackendExternalComputeResourceResource
        > {
    public BackendExternalComputeResourceResourceTest() {
        super(new BackendExternalComputeResourceResource(string2hex(NAMES[1]), GUIDS[0].toString()));
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

    private Provider getProvider() {
        Provider provider = mock(Provider.class);
        when(provider.getId()).thenReturn(GUIDS[0]);
        when(provider.getName()).thenReturn(NAMES[0]);
        return provider;
    }

    private List<org.ovirt.engine.core.common.businessentities.ExternalComputeResource> getResources() {
        List<org.ovirt.engine.core.common.businessentities.ExternalComputeResource> resources = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            resources.add(getEntity(i));
        }
        return resources;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.ExternalComputeResource getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.ExternalComputeResource resource =
                mock(org.ovirt.engine.core.common.businessentities.ExternalComputeResource.class);
        when(resource.getName()).thenReturn(NAMES[index]);
        return resource;
    }

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpEntityQueryExpectations(
            QueryType.GetProviderById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getProvider()
        );
        setUpGetEntityExpectations(
            QueryType.GetComputeResourceFromExternalProvider,
            ProviderQueryParameters.class,
            new String[] { "Provider.Id" },
            new Object[] { GUIDS[0] },
            notFound? null: getResources()
        );
    }

    @Override
    protected void verifyModel(ExternalComputeResource model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
