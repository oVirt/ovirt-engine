package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendAssignedRolesResourceTest
        extends AbstractBackendCollectionResourceTest<Role, Permission, BackendAssignedRolesResource> {

    public BackendAssignedRolesResourceTest() {
        super(new BackendAssignedRolesResource(GUIDS[0]), null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Override
    protected List<Role> getCollection() {
        return collection.list().getRoles();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetPermissionsByAdElementId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     setUpPermissions(),
                                     failure);

        control.replay();
    }

    @Override
    protected Permission getEntity(int index) {
        Permission permission = new Permission();
        permission.setId(GUIDS[(index + 1) % 3]);
        permission.setAdElementId(GUIDS[0]);
        permission.setRoleId(GUIDS[index]);
        permission.setObjectType(VdcObjectType.System);
        return permission;
    }

    protected List<Permission> setUpPermissions() {
        List<Permission> perms = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            perms.add(getEntity(i));
        }
        return perms;
    }

    @Override
    protected void verifyModel(Role model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
    }
}

