package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendAssignedVnicProfileResourceTest
        extends AbstractBackendVnicProfileResourceTest<BackendAssignedVnicProfileResource> {

    public BackendAssignedVnicProfileResourceTest() {
        super(new BackendAssignedVnicProfileResource(GUIDS[0].toString(),
                new BackendAssignedVnicProfilesResource(GUIDS[0].toString())));
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendVnicProfileResource("foo")));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, false);

        verifyModel(resource.get(), 0);
    }

    protected void setUpEntityQueryExpectations(int times, int index, boolean notFound) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetVnicProfileById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[index] },
                    notFound ? null : getEntity(index));
        }
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.VnicProfileView getEntity(int index) {
        return setUpEntityExpectations(mock(VnicProfileView.class), index);
    }

    static VnicProfileView setUpEntityExpectations(VnicProfileView entity, int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getNetworkId()).thenReturn(GUIDS[index]);
        return entity;
    }
}
