package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.StorageConnectionExtension;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.StorageServerConnectionExtensionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageServerConnectionExtensionResourceTest extends AbstractBackendSubResourceTest<StorageConnectionExtension, StorageServerConnectionExtension, BackendStorageServerConnectionExtensionResource> {
    private static Guid extensionID = GUIDS[0];
    private static Guid hostID = GUIDS[1];
    private static String username = "user";
    private static String pass = "pass";
    private static String iqn = "iqn";

    public BackendStorageServerConnectionExtensionResourceTest() {
        super(new BackendStorageServerConnectionExtensionResource(extensionID.toString(),
                new BackendStorageServerConnectionExtensionsResource(hostID)));
    }

    @Override protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    public StorageServerConnectionExtension getDefaultEntity() {
        return StorageConnectionExtensionResourceTestHelper.getEntity(extensionID, hostID, pass, username, iqn);
    }

    public StorageConnectionExtension getDefaultModel() {
        return StorageConnectionExtensionResourceTestHelper.getModel(extensionID, hostID, pass, username, iqn);
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageServerConnectionExtensionById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { extensionID },
                getDefaultEntity());
    }

    @Test
    public void testGet() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        StorageConnectionExtension extension = resource.get();
        assertNotNull(extension);
        assertEquals(extension.getId(), extensionID.toString());
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(
                setUpActionExpectations(
                        VdcActionType.RemoveStorageServerConnectionExtension,
                        IdParameters.class,
                        new String[] { "Id" },
                        new Object[] { extensionID },
                        true,
                        true
                )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations();
        setUpGetEntityExpectations(); // Get is being called twice.

        setUriInfo(
                setUpActionExpectations(
                        VdcActionType.UpdateStorageServerConnectionExtension,
                        StorageServerConnectionExtensionParameters.class,
                        new String[] { "StorageServerConnectionExtension" },
                        new Object[] { getDefaultEntity() },
                        true,
                        true
                )
        );

        StorageConnectionExtension extension = resource.update(getDefaultModel());
        assertNotNull(extension);
        assertEquals(extension.getId(), extensionID.toString());
    }
}
