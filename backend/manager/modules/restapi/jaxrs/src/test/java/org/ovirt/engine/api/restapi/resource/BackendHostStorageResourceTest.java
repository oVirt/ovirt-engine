package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.resource.StorageResource;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;

import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostStorageResourceTest
    extends AbstractBackendCollectionResourceTest<Storage, LUNs, BackendHostStorageResource> {

    private static final Guid HOST_GUID = GUIDS[0];
    private static final int SINGLE_STORAGE_IDX = GUIDS.length - 2;
    private static final String VG_ID_PREFIX = "vg";

    public BackendHostStorageResourceTest() {
        super(new BackendHostStorageResource(HOST_GUID.toString()), null, null);
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    public void testGetNotFound() throws Exception {
        StorageResource subresource = collection.getStorageSubResource("foo");
        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations("");
        try {
            subresource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        StorageResource subresource = collection.getStorageSubResource(GUIDS[SINGLE_STORAGE_IDX].toString());

        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations("", null, false);

        verifyModel(subresource.get(), SINGLE_STORAGE_IDX);
    }

    @Test
    public void testGetVg() throws Exception {
        StorageResource subresource = collection.getStorageSubResource(VG_ID_PREFIX + GUIDS[SINGLE_STORAGE_IDX].toString());

        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations("");

        verifyModel(subresource.get(), SINGLE_STORAGE_IDX);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpQueryExpectations(query, failure, true);
    }

    protected void setUpQueryExpectations(String query, Object failure, boolean vgs_query) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetDeviceList,
                                     GetDeviceListQueryParameters.class,
                                     new String[] { "VdsId" },
                                     new Object[] { HOST_GUID },
                                     setUpLuns(),
                                     failure);
        if (vgs_query && failure == null) {
            setUpEntityQueryExpectations(VdcQueryType.GetVgList,
                                         VdsIdParametersBase.class,
                                         new String[] { "VdsId" },
                                         new Object[] { HOST_GUID },
                                         setUpVgs(),
                                         failure);
        }

        control.replay();
    }

    private List<LUNs> setUpLuns() {
        List<LUNs> luns = new ArrayList<LUNs>();
        for (int i = 0; i < NAMES.length; i++) {
            luns.add(getEntity(i));
        }
        return luns;
    }

    private List<org.ovirt.engine.core.common.businessentities.StorageDomain> setUpVgs() {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> vgs = new ArrayList<org.ovirt.engine.core.common.businessentities.StorageDomain>();
        for (int i = 0; i < NAMES.length; i++) {
            vgs.add(getVgEntity(i));
        }
        return vgs;
    }

    @Override
    protected LUNs getEntity(int index) {
        LUNs entity = new LUNs();
        entity.setLUN_id(GUIDS[index].toString());
        entity.setLunType(org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI);
        return entity;
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain getVgEntity(int index) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        entity.setStorage(VG_ID_PREFIX + GUIDS[index].toString());
        entity.setStorageType(org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI);
        return entity;
    }

    @Override
    protected void verifyModel(Storage model, int index) {
        assertEquals(StorageType.ISCSI.value(), model.getType());
        if (!model.isSetVolumeGroup()) {
            assertEquals(GUIDS[index].toString(), model.getId());
            assertEquals(1, model.getLogicalUnits().size());
            assertEquals(GUIDS[index].toString(), model.getLogicalUnits().get(0).getId());
        } else {
            assertEquals(VG_ID_PREFIX + GUIDS[index].toString(), model.getId());
            assertEquals(VG_ID_PREFIX + GUIDS[index].toString(), model.getVolumeGroup().getId());
        }
        verifyLinks(model);
    }

    @Override
    protected void verifyCollection(List<Storage> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(NAMES.length * 2, collection.size());
        for (int i = 0; i < (NAMES.length * 2); i++) {
            verifyModel(collection.get(i), i % NAMES.length);
        }
    }

    @Override
    protected List<Storage> getCollection() {
        return collection.list().getStorage();
    }
}
