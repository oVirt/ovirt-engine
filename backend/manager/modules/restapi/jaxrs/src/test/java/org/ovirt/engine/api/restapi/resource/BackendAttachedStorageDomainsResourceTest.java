package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAttachedStorageDomainsResourceTest
    extends AbstractBackendCollectionResourceTest<StorageDomain,
                                                  org.ovirt.engine.core.common.businessentities.StorageDomain,
                                                  BackendAttachedStorageDomainsResource> {

    public BackendAttachedStorageDomainsResourceTest() {
        super(new BackendAttachedStorageDomainsResource(GUIDS[NAMES.length-1].toString()), null, null);
    }

    @Override
    @Test
    @Ignore
    public void testQuery() throws Exception {
    }

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        setUpGetConnection(1);

        setUpCreationExpectations(VdcActionType.AttachStorageDomainToPool,
                                  AttachStorageDomainToPoolParameters.class,
                                  new String[] { "StorageDomainId", "StoragePoolId" },
                                  new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetStorageDomainByIdAndStoragePoolId,
                                  StorageDomainAndPoolQueryParameters.class,
                                  new String[] { "StorageDomainId", "StoragePoolId" },
                                  new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                  getEntity(0));

        StorageDomain model = new StorageDomain();
        model.setId(GUIDS[0].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyModel((StorageDomain) response.getEntity(), 0);
    }

    static StorageServerConnections setUpStorageServerConnection() {
        StorageServerConnections cnx = new StorageServerConnections();
            cnx.setid(GUIDS[0].toString());
            cnx.setconnection("10.11.12.13" + ":" + "/1");
        return cnx;
    }

    @Test
    public void testAddByName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        setUpGetConnection(1);

        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[0] },
                getEntityStatic(0));

        setUpCreationExpectations(VdcActionType.AttachStorageDomainToPool,
                                  AttachStorageDomainToPoolParameters.class,
                                  new String[] { "StorageDomainId", "StoragePoolId" },
                                  new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetStorageDomainByIdAndStoragePoolId,
                                  StorageDomainAndPoolQueryParameters.class,
                                  new String[] { "StorageDomainId", "StoragePoolId" },
                                  new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                getEntity(0));

        StorageDomain model = new StorageDomain();
        model.setName(NAMES[0]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyModel((StorageDomain) response.getEntity(), 0);
    }

    @Test
    public void testAddCantDo() throws Exception {
        doTestBadAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddFailure() throws Exception {
        doTestBadAdd(true, false, FAILURE);
    }

    private void doTestBadAdd(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AttachStorageDomainToPool,
                                           AttachStorageDomainToPoolParameters.class,
                                           new String[] { "StorageDomainId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                           canDo,
                                           success));

        StorageDomain model = new StorageDomain();
        model.setId(GUIDS[0].toString());

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        StorageDomain model = new StorageDomain();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "StorageDomain", "add", "id|name");
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetConnection(2);
        setUpGetEntityExpectations(GUIDS[0], 2, getEntity(0));
        setUriInfo(setUpActionExpectations(VdcActionType.DetachStorageDomainFromPool,
                                           DetachStorageDomainFromPoolParameters.class,
                                           new String[] { "StorageDomainId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveLocalStorage() throws Exception {
        setUpGetConnection(2);
        setUpGetEntityExpectations(GUIDS[0], 2, getEntity(0, StorageType.LOCALFS));
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveStorageDomain,
                                           RemoveStorageDomainParameters.class,
                                           new String[] { "StorageDomainId"},
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    private void setUpGetConnection(int times) throws Exception {
        for (int i=0; i<times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                    StorageServerConnectionQueryParametersBase.class,
                    new String[] { "ServerConnectionId" },
                    new Object[] { GUIDS[0].toString() },
                    setUpStorageServerConnection());
        }
    }

    private void setUpGetEntityExpectations(Guid guid, int times, org.ovirt.engine.core.common.businessentities.StorageDomain entity) throws Exception {
        for (int i=0; i<times; i++) {
            setUpGetEntityExpectations(guid, entity);
        }
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(NON_EXISTANT_GUID, null);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(wae.getResponse().getStatus(), 404);
        }
    }

    private void setUpGetEntityExpectations(Guid entityId, org.ovirt.engine.core.common.businessentities.StorageDomain entity) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStorageDomainByIdAndStoragePoolId,
                StorageDomainAndPoolQueryParameters.class,
                new String[] { "StorageDomainId", "StoragePoolId" },
                new Object[] { entityId, GUIDS[NAMES.length-1] },
                entity);
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        String[] paramNames = new String[] { "ServerConnectionId" };
        Object[] paramValues = new Object[] { GUIDS[0].toString() };
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(backend.runQuery(eq(VdcQueryType.GetStorageServerConnectionById), eqQueryParams(StorageServerConnectionQueryParametersBase.class, addSession(paramNames), addSession(paramValues))))
        .andReturn(queryResult).anyTimes();
        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(queryResult.getReturnValue()).andReturn(setUpStorageServerConnection()).anyTimes();
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetConnection(2);
        setUpGetEntityExpectations(GUIDS[0], 2, getEntity(0));
        setUriInfo(setUpActionExpectations(VdcActionType.DetachStorageDomainFromPool,
                                           DetachStorageDomainFromPoolParameters.class,
                                           new String[] { "StorageDomainId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainsByStoragePoolId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[NAMES.length-1] },
                                     setUpStorageDomains(),
                                     failure);

        control.replay();
    }

    protected List<org.ovirt.engine.core.common.businessentities.StorageDomain> setUpStorageDomains() {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> entities = new ArrayList<org.ovirt.engine.core.common.businessentities.StorageDomain>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomainStatic getStaticEntity(int index) {
        org.ovirt.engine.core.common.businessentities.StorageDomainStatic entity =
                control.createMock(org.ovirt.engine.core.common.businessentities.StorageDomainStatic.class);
        return setUpEntityExpectations(entity, index, StorageType.NFS);
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomainStatic getEntityStatic(int index) {
        org.ovirt.engine.core.common.businessentities.StorageDomainStatic entity =
                control.createMock(org.ovirt.engine.core.common.businessentities.StorageDomainStatic.class);
        return setUpEntityExpectations(entity, index, StorageType.NFS);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.StorageDomain getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = control.createMock(org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        return setUpEntityExpectations(entity, index, StorageType.NFS);
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain getEntity(int index, StorageType storageType) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = control.createMock(org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        return setUpEntityExpectations(entity, index, storageType);
    }

    static org.ovirt.engine.core.common.businessentities.StorageDomainStatic setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.StorageDomainStatic entity,
            int index,
            StorageType storageType) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getStorageDomainType()).andReturn(StorageDomainType.Master).anyTimes();
        expect(entity.getStorageType()).andReturn(storageType).anyTimes();
        expect(entity.getStorage()).andReturn(GUIDS[0].toString()).anyTimes();
        return entity;
    }

    static org.ovirt.engine.core.common.businessentities.StorageDomain setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.StorageDomain entity, int index, StorageType storageType) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getStatus()).andReturn(StorageDomainStatus.Active).anyTimes();
        expect(entity.getStorageDomainType()).andReturn(StorageDomainType.Master).anyTimes();
        expect(entity.getStorageType()).andReturn(storageType).anyTimes();
        expect(entity.getStorage()).andReturn(GUIDS[0].toString()).anyTimes();
        return entity;
    }

    @Override
    protected void verifyModel(StorageDomain model, int index) {
        verifyStorageDomain(model, index);
        verifyLinks(model);
    }

    static void verifyStorageDomain(StorageDomain model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertNotNull(model.getDataCenter());
        assertEquals(GUIDS[NAMES.length-1].toString(), model.getDataCenter().getId());
        assertEquals(org.ovirt.engine.api.model.StorageDomainStatus.ACTIVE.value(), model.getStatus().getState());
        assertEquals(true, model.isMaster());
    }

    @Override
    protected List<StorageDomain> getCollection() {
        return collection.list().getStorageDomains();
    }
}
