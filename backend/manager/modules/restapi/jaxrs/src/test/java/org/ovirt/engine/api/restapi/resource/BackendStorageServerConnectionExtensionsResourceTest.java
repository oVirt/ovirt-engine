package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.StorageConnectionExtension;
import org.ovirt.engine.core.common.action.StorageServerConnectionExtensionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageServerConnectionExtensionsResourceTest extends AbstractBackendCollectionResourceTest<StorageConnectionExtension, StorageServerConnectionExtension, BackendStorageServerConnectionExtensionsResource>{
    private static Guid hostID = GUIDS[1];
    private Guid extensionID    = GUIDS[0];
    private String iqn = "iqn";
    private String user = "user";
    private String pass = "pass";

    public BackendStorageServerConnectionExtensionsResourceTest() {
        super(new BackendStorageServerConnectionExtensionsResource(hostID), null, "");
    }

    @Override protected List<StorageConnectionExtension> getCollection() {
        return collection.list().getStorageConnectionExtensions();
    }

    @Test
    @Override
    public void testList() throws Exception {
        int numOfEntitiesInList = 2;
        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionExtensionsByHostId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { hostID },
                getEntityList(numOfEntitiesInList));

        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);
        control.replay();

        List<StorageConnectionExtension> retCollection = getCollection();
        assertNotNull(retCollection);
        assertEquals(numOfEntitiesInList, retCollection.size());
    }

    @Test
    public void testAdd() throws Exception {
        StorageServerConnectionExtension entity =
                StorageConnectionExtensionResourceTestHelper.getEntity(extensionID, hostID, pass, user, iqn);

        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddStorageServerConnectionExtension,
                StorageServerConnectionExtensionParameters.class,
                new String[] { "StorageServerConnectionExtension" },
                new Object[] { entity },
                true,
                true,
                extensionID,
                VdcQueryType.GetStorageServerConnectionExtensionById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { extensionID },
                entity);

        Response response = collection.add(StorageConnectionExtensionResourceTestHelper.getModel(extensionID, hostID, pass, user, iqn));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageConnectionExtension);
        assertNotNull(response.getEntity());
        assertEquals(((StorageConnectionExtension) response.getEntity()).getId(), entity.getId().toString());
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    @Ignore
    @Override
    public void testListFailure() throws Exception {
    }

    @Test
    @Ignore
    @Override
    public void testListCrash() throws Exception {
    }

    @Test
    @Override
    @Ignore
    public void testListCrashClientLocale() throws Exception {
    }

    public List<StorageServerConnectionExtension> getEntityList(int numOfEntities) {
        List<StorageServerConnectionExtension> retVal = new ArrayList<>();
        for (int i = 0; i<numOfEntities; i++) {
            retVal.add(StorageConnectionExtensionResourceTestHelper.getEntity(extensionID, hostID, pass, user, iqn));
        }
        return retVal;
    }
}
