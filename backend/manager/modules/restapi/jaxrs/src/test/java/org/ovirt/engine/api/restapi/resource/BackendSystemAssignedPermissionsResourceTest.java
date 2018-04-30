package org.ovirt.engine.api.restapi.resource;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendSystemAssignedPermissionsResourceTest
        extends BackendEntityAssignedPermissionsResourceTest {

    public BackendSystemAssignedPermissionsResourceTest() {
        super(Guid.SYSTEM, BaseResource.class);
    }
}

