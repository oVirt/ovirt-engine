package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public abstract class AbstractBackendVnicProfileResourceTest<C extends AbstractBackendVnicProfileResource> extends AbstractBackendSubResourceTest<VnicProfile, org.ovirt.engine.core.common.businessentities.network.VnicProfile, AbstractBackendVnicProfileResource> {
    protected AbstractBackendVnicProfileResourceTest(C resource) {
        super(resource);
    }

    @Test
    public void testRemoveNotFound() {
        setUpEntityQueryExpectations(1, 0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemove() {
        setUpEntityQueryExpectations(2, 0, false);
        setUriInfo(setUpActionExpectations(ActionType.RemoveVnicProfile,
                VnicProfileParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpEntityQueryExpectations(QueryType.GetVnicProfileById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(2, 0, false);

        setUriInfo(setUpActionExpectations(ActionType.RemoveVnicProfile,
                VnicProfileParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));
        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
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
    protected org.ovirt.engine.core.common.businessentities.network.VnicProfile getEntity(int index) {
        return setUpEntityExpectations(mock(org.ovirt.engine.core.common.businessentities.network.VnicProfile.class),
                index);
    }

    static org.ovirt.engine.core.common.businessentities.network.VnicProfile setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.network.VnicProfile entity,
            int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getNetworkId()).thenReturn(GUIDS[index]);
        return entity;
    }
}
