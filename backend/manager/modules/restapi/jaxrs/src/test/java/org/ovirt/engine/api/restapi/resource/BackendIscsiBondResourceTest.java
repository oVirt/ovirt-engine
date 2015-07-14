package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.core.common.action.RemoveIscsiBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendIscsiBondResourceTest
    extends AbstractBackendSubResourceTest<IscsiBond, org.ovirt.engine.core.common.businessentities.IscsiBond, BackendIscsiBondResource> {

    public BackendIscsiBondResourceTest() {
        super(new BackendIscsiBondResource(GUIDS[0].toString()));
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(0, getEntity(0));
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveIscsiBond,
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
    public void testRemoveNonExistant() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(0, null);
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    private void setUpGetEntityExpectations(int index, org.ovirt.engine.core.common.businessentities.IscsiBond result)
            throws Exception {
        setUpGetEntityExpectations(
            VdcQueryType.GetIscsiBondById,
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
