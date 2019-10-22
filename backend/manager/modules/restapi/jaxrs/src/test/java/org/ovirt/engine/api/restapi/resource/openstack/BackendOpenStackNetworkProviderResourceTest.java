/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackNetworkProviderType;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendOpenStackNetworkProviderResourceTest
        extends AbstractBackendSubResourceTest<OpenStackNetworkProvider, Provider, BackendOpenStackNetworkProviderResource> {
    public BackendOpenStackNetworkProviderResourceTest() {
        super(new BackendOpenStackNetworkProviderResource(GUIDS[0].toString()));
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendOpenStackNetworkProviderResource("foo")));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))));
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(2);
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateProvider,
                ProviderParameters.class,
                new String[] { "Provider.Id" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateProvider,
                ProviderParameters.class,
                new String[] { "Provider.Id" },
                new Object[] { GUIDS[0] },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))), detail);
    }

    @Test
    public void testConflictedUpdate() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);

        OpenStackNetworkProvider model = getModel(1);
        model.setId(GUIDS[1].toString());
        verifyImmutabilityConstraint(assertThrows(WebApplicationException.class, () -> resource.update(model)));
    }

    @Test
    public void testRemove() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setUpActionExpectations(ActionType.RemoveProvider,
                ProviderParameters.class,
                new String[] { "Provider.Id" },
                new Object[] { GUIDS[0] },
                true,
                true);
        resource.remove();
    }

    private OpenStackNetworkProvider getModel(int index) {
        OpenStackNetworkProvider model = new OpenStackNetworkProvider();
        model.setName(NAMES[index]);
        model.setType(OpenStackNetworkProviderType.NEUTRON);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    @Override
    protected Provider getEntity(int index) {
        Provider provider = mock(Provider.class);
        when(provider.getId()).thenReturn(GUIDS[index]);
        when(provider.getName()).thenReturn(NAMES[index]);
        when(provider.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(provider.getType()).thenReturn(ProviderType.OPENSTACK_NETWORK);
        return provider;
    }

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        while (times-- > 0) {
            setUpGetEntityExpectations(
                QueryType.GetProviderById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                notFound? null: getEntity(0)
            );
        }
    }
}
