package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;
import org.ovirt.engine.api.restapi.resource.validation.ValidatorLocator;
import org.ovirt.engine.api.restapi.types.MappingLocator;
import org.ovirt.engine.api.restapi.types.PermitMapper;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;

public class BackendPermitResourceTest extends Assert {

    private static final Guid ROLE_ID = new Guid("11111111-1111-1111-1111-111111111111");

    private MappingLocator mapperLocator;
    private ValidatorLocator validatorLocator;

    public BackendPermitResourceTest() {
        mapperLocator = new MappingLocator();
        mapperLocator.populate();
        validatorLocator = new ValidatorLocator();
        validatorLocator.populate();
    }

    @Test
    public void testGetBadId() throws Exception {
        doTestGetNotFound("foo");
    }

    @Test
    public void testGetNotFound() throws Exception {
        doTestGetNotFound("11111");
    }

    private void doTestGetNotFound(String id) throws Exception {
        BackendPermitResource resource = new BackendPermitResource(id, new BackendPermitsResource(ROLE_ID));
        resource.getParent().setMappingLocator(mapperLocator);
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testGet() {
        for (ActionGroup action : ActionGroup.values()) {
            BackendPermitResource resource =
                new BackendPermitResource(Integer.toString(action.getId()), new BackendPermitsResource(ROLE_ID));
            resource.setMappingLocator(mapperLocator);
            resource.getParent().setMappingLocator(mapperLocator);
            resource.setValidatorLocator(validatorLocator);
            resource.getParent().setValidatorLocator(validatorLocator);
            verifyPermit(resource.get(), action);
        }
    }

    private void verifyPermit(Permit permit, ActionGroup action) {
        assertEquals(Integer.toString(action.getId()), permit.getId());
        PermitType permitType = PermitMapper.map(action, (PermitType)null);
        assertEquals(permitType.value(), permit.getName());
        assertNotNull(permit.getRole());
        assertEquals(ROLE_ID.toString(), permit.getRole().getId());
    }
}

