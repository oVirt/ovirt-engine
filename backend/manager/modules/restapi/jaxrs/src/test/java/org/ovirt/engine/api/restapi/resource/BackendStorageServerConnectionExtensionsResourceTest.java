package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.StorageConnectionExtension;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageServerConnectionExtensionParameters;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testList() {
        int numOfEntitiesInList = 2;
        setUpGetEntityExpectations(QueryType.GetStorageServerConnectionExtensionsByHostId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { hostID },
                getEntityList(numOfEntitiesInList));

        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        List<StorageConnectionExtension> retCollection = getCollection();
        assertNotNull(retCollection);
        assertEquals(numOfEntitiesInList, retCollection.size());
    }

    @Test
    public void testAdd() {
        StorageServerConnectionExtension entity =
                StorageConnectionExtensionResourceTestHelper.getEntity(extensionID, hostID, pass, user, iqn);

        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddStorageServerConnectionExtension,
                StorageServerConnectionExtensionParameters.class,
                new String[] { "StorageServerConnectionExtension" },
                new Object[] { entity },
                true,
                true,
                extensionID,
                QueryType.GetStorageServerConnectionExtensionById,
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
    @Disabled
    @Override
    public void testQuery() {
    }

    @Test
    @Disabled
    @Override
    public void testListFailure() {
    }

    @Test
    @Disabled
    @Override
    public void testListCrash() {
    }

    @Test
    @Override
    @Disabled
    public void testListCrashClientLocale() {
    }

    public List<StorageServerConnectionExtension> getEntityList(int numOfEntities) {
        List<StorageServerConnectionExtension> retVal = new ArrayList<>();
        for (int i = 0; i<numOfEntities; i++) {
            retVal.add(StorageConnectionExtensionResourceTestHelper.getEntity(extensionID, hostID, pass, user, iqn));
        }
        return retVal;
    }
}
