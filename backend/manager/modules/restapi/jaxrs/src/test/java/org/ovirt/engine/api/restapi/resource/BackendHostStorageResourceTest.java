package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.resource.StorageResource;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostStorageResourceTest
    extends AbstractBackendCollectionResourceTest<HostStorage, LUNs, BackendHostStorageResource> {

    private static final Guid HOST_GUID = GUIDS[0];
    private static final int SINGLE_STORAGE_IDX = GUIDS.length - 2;

    public BackendHostStorageResourceTest() {
        super(new BackendHostStorageResource(HOST_GUID.toString()), null, null);
    }

    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Test
    public void testGetNotFound() throws Exception {
        StorageResource subresource = collection.getStorageResource("foo");
        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations("");
        verifyNotFoundException(assertThrows(WebApplicationException.class, subresource::get));
    }

    @Test
    public void testGet() {
        StorageResource subresource = collection.getStorageResource(GUIDS[SINGLE_STORAGE_IDX].toString());

        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations("", null);

        verifyModel(subresource.get(), SINGLE_STORAGE_IDX);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        assertEquals("", query);

        setUpEntityQueryExpectations(QueryType.GetDeviceList,
                                     GetDeviceListQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { HOST_GUID },
                                     setUpLuns(),
                                     failure);
    }

    private List<LUNs> setUpLuns() {
        List<LUNs> luns = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            luns.add(getEntity(i));
        }
        return luns;
    }

    @Override
    protected LUNs getEntity(int index) {
        LUNs entity = new LUNs();
        entity.setLUNId(GUIDS[index].toString());
        entity.setLunType(org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI);
        return entity;
    }

    @Override
    protected void verifyModel(HostStorage model, int index) {
        assertEquals(StorageType.ISCSI, model.getType());
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(1, model.getLogicalUnits().getLogicalUnits().size());
        assertEquals(GUIDS[index].toString(), model.getLogicalUnits().getLogicalUnits().get(0).getId());
        verifyLinks(model);
    }

    @Override
    protected void verifyCollection(List<HostStorage> collection) {
        assertNotNull(collection);
        assertEquals(NAMES.length, collection.size());
        for (int i = 0; i < NAMES.length; i++) {
            verifyModel(collection.get(i), i % NAMES.length);
        }
    }

    @Override
    protected List<HostStorage> getCollection() {
        return collection.list().getHostStorages();
    }
}
