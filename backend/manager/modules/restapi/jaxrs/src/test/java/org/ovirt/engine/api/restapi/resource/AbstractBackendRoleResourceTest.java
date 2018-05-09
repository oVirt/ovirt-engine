package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public abstract class AbstractBackendRoleResourceTest
        extends AbstractBackendSubResourceTest<Role, org.ovirt.engine.core.common.businessentities.Role, BackendRoleResource> {

    public AbstractBackendRoleResourceTest(BackendRoleResource roleResource) {
        super(roleResource);
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendRoleResource("foo", null)));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();

        verifyModel(resource.get(), 0);
    }

    protected void setUpGetEntityExpectations() {
        setUpGetEntityExpectations(false);
    }

    protected void setUpGetEntityExpectations(boolean notFound) {
        setUpGetEntityExpectations(QueryType.GetRoleById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   notFound ? null : getEntity(0));
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Role getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Role role = new org.ovirt.engine.core.common.businessentities.Role();
        role.setId(GUIDS[index]);
        role.setName(NAMES[index]);
        role.setDescription(DESCRIPTIONS[index]);
        role.setReadonly(false);
        role.setType(RoleType.ADMIN);
        return role;
    }

    @Override
    protected void verifyModel(Role model, int index) {
        super.verifyModel(model, index);
        assertTrue(model.isMutable());
        assertTrue(model.isAdministrative());
    }

}

