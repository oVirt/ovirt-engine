package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.ExternalNetworkProviderConfiguration;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostExternalNetworkProviderConfigurationResourceTest
        extends AbstractBackendSubResourceTest<ExternalNetworkProviderConfiguration, Provider,
        BackendHostExternalNetworkProviderConfigurationResource> {

    private static final int PROVIDER_INDEX = 0;
    private static final Guid PROVIDER_ID = GUIDS[PROVIDER_INDEX];
    private static final Guid HOST_ID = GUIDS[PROVIDER_INDEX +1];

    public BackendHostExternalNetworkProviderConfigurationResourceTest() {
        super(new BackendHostExternalNetworkProviderConfigurationResource(PROVIDER_ID.toString(), HOST_ID));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
                QueryType.GetProviderById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { PROVIDER_ID },
                Collections.emptyList()
        );

        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
                QueryType.GetProviderById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { PROVIDER_ID },
                getEntityList()
        );

        ExternalNetworkProviderConfiguration config = resource.get();
        verifyModel(config, PROVIDER_INDEX);
    }

    protected List<Provider> getEntityList() {
        List<Provider> entities = new ArrayList<>();
        entities.add(getEntity(0));
        return entities;
    }

    @Override
    protected Provider getEntity(int index) {
        return setUpEntityExpectations(mock(Provider.class),
                index);
    }

    private Provider setUpEntityExpectations(
            Provider entity,
            int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        return entity;
    }

    @Override
    protected void verifyModel(ExternalNetworkProviderConfiguration model, int index) {
        assertEquals(HexUtils.string2hex(GUIDS[index].toString()), model.getId());
        assertEquals(GUIDS[index+1].toString(), model.getHost().getId());
        assertEquals(GUIDS[index].toString(), model.getExternalNetworkProvider().getId());
        verifyLinks(model);
    }
}
