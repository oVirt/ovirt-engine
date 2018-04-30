package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Role;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendAssignedRoleResourceTest extends AbstractBackendRoleResourceTest {

    public BackendAssignedRoleResourceTest() {
        super(new BackendRoleResource(GUIDS[0].toString(), GUIDS[1]));
    }

    @Override
    protected void verifyModel(Role model, int index) {
        super.verifyModel(model, index);
        assertTrue(model.isSetUser());
        assertTrue(model.getUser().isSetId());
        assertEquals(GUIDS[1].toString(), model.getUser().getId());
    }
}

