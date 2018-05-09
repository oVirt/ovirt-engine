package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveIscsiBondParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendIscsiBondResourceTest
    extends AbstractBackendSubResourceTest<IscsiBond, org.ovirt.engine.core.common.businessentities.IscsiBond, BackendIscsiBondResource> {

    public BackendIscsiBondResourceTest() {
        super(new BackendIscsiBondResource(GUIDS[0].toString()));
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations(0, getEntity(0));
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveIscsiBond,
                RemoveIscsiBondParameters.class,
                new String[] { "IscsiBondId" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(0, null);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    private void setUpGetEntityExpectations(int index, org.ovirt.engine.core.common.businessentities.IscsiBond result) {
        setUpGetEntityExpectations(
            QueryType.GetIscsiBondById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[index] },
            result
        );
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.IscsiBond getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond =
            new org.ovirt.engine.core.common.businessentities.IscsiBond();
        iscsiBond.setId(GUIDS[index]);
        iscsiBond.setName(NAMES[index]);
        iscsiBond.setStoragePoolId(GUIDS[0]);
        return iscsiBond;
    }
}
