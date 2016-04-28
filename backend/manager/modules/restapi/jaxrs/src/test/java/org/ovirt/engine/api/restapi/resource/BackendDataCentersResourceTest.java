package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Version;

public class BackendDataCentersResourceTest
        extends AbstractBackendCollectionResourceTest<DataCenter, StoragePool, BackendDataCentersResource> {

    public BackendDataCentersResourceTest() {
        super(new BackendDataCentersResource(), SearchType.StoragePool, "Datacenter : ");
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpVersionExpectations(0);
        setUpVersionExpectations(1);
        setUpVersionExpectations(2);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testQuery() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(QUERY);

        setUpVersionExpectations(0);
        setUpVersionExpectations(1);
        setUpVersionExpectations(2);
        setUpQueryExpectations(QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testAddDataCenter() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpVersionExpectations(0);
        setUpCreationExpectations(VdcActionType.AddEmptyStoragePool,
                                  StoragePoolManagementParameter.class,
                                  new String[] {},
                                  new Object[] {},
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetStoragePoolById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        DataCenter model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof DataCenter);
        verifyModel((DataCenter) response.getEntity(), 0);
    }

    @Test
    public void testAddDataCenterCantDo() throws Exception {
        doTestBadAddDataCenter(false, true, CANT_DO);
    }

    @Test
    public void testAddDataCenterFailure() throws Exception {
        doTestBadAddDataCenter(true, false, FAILURE);
    }

    private void doTestBadAddDataCenter(boolean valid, boolean success, String detail)
            throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddEmptyStoragePool,
                                           StoragePoolManagementParameter.class,
                                           new String[] {},
                                           new Object[] {},
                                           valid,
                                           success));

        DataCenter model = getModel(0);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        DataCenter model = new DataCenter();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "DataCenter", "add", "name");
        }
    }

    protected void setUpVersionExpectations(int index) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetAvailableStoragePoolVersions,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[index] },
                                   getVersions());
    }

    @Override
    protected StoragePool getEntity(int index) {
        return setUpEntityExpectations(control.createMock(StoragePool.class), index);
    }

    static StoragePool setUpEntityExpectations(StoragePool entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getdescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.isLocal()).andReturn(false).anyTimes();
        return entity;
    }

    static DataCenter getModel(int index) {
        DataCenter model = new DataCenter();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        model.setLocal(false);
        return model;
    }

    @Override
    protected List<DataCenter> getCollection() {
        return collection.list().getDataCenters();
    }

    protected List<Version> getVersions() {
        Version version = control.createMock(Version.class);
        expect(version.getMajor()).andReturn(2);
        expect(version.getMinor()).andReturn(3);
        List<Version> versions = new ArrayList<>();
        versions.add(version);
        return versions;
    }

    @Override
    protected void verifyModel(DataCenter model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }

    static void verifyModelSpecific(DataCenter model, int index) {
        assertEquals(false, model.isLocal());
        assertFalse(model.getLinks().isEmpty());
        assertEquals("storagedomains", model.getLinks().get(0).getRel());
        assertEquals(BASE_PATH + "/datacenters/" + GUIDS[index] + "/storagedomains", model.getLinks().get(0).getHref());
        assertTrue(model.isSetSupportedVersions());
        assertEquals(1, model.getSupportedVersions().getVersions().size());
        assertEquals(2, model.getSupportedVersions().getVersions().get(0).getMajor().intValue());
        assertEquals(3, model.getSupportedVersions().getVersions().get(0).getMinor().intValue());
    }
}
