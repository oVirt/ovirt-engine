package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqParams;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendAttachedStorageDomainsResourceTest
    extends AbstractBackendCollectionResourceTest<StorageDomain,
                                                  org.ovirt.engine.core.common.businessentities.StorageDomain,
                                                  BackendAttachedStorageDomainsResource> {

    public BackendAttachedStorageDomainsResourceTest() {
        super(new BackendAttachedStorageDomainsResource(GUIDS[NAMES.length-1].toString()), null, null);
    }

    @Override
    @Test
    @Disabled
    public void testQuery() {
    }

    @Test
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());

        setUpGetConnection(1);

        setUpCreationExpectations(ActionType.AttachStorageDomainToPool,
                                  AttachStorageDomainToPoolParameters.class,
                                  new String[] { "StorageDomainId", "StoragePoolId" },
                                  new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                  true,
                                  true,
                                  null,
                                  QueryType.GetStorageDomainByIdAndStoragePoolId,
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
            cnx.setId(GUIDS[0].toString());
            cnx.setConnection("10.11.12.13" + ":" + "/1");
        return cnx;
    }

    @Test
    public void testAddByName() {
        setUriInfo(setUpBasicUriExpectations());

        setUpGetConnection(1);

        setUpEntityQueryExpectations(QueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[0] },
                getEntityStatic(0));

        setUpCreationExpectations(ActionType.AttachStorageDomainToPool,
                                  AttachStorageDomainToPoolParameters.class,
                                  new String[] { "StorageDomainId", "StoragePoolId" },
                                  new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                  true,
                                  true,
                                  null,
                                  QueryType.GetStorageDomainByIdAndStoragePoolId,
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
    public void testAddCantDo() {
        doTestBadAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddFailure() {
        doTestBadAdd(true, false, FAILURE);
    }

    private void doTestBadAdd(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(ActionType.AttachStorageDomainToPool,
                                           AttachStorageDomainToPoolParameters.class,
                                           new String[] { "StorageDomainId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                           valid,
                                           success));

        StorageDomain model = new StorageDomain();
        model.setId(GUIDS[0].toString());

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        StorageDomain model = new StorageDomain();
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "StorageDomain", "add", "id|name");
    }


    private void setUpGetConnection(int times) {
        for (int i=0; i<times; i++) {
            setUpGetEntityExpectations(QueryType.GetStorageServerConnectionById,
                    StorageServerConnectionQueryParametersBase.class,
                    new String[] { "ServerConnectionId" },
                    new Object[] { GUIDS[0].toString() },
                    setUpStorageServerConnection());
        }
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        String[] paramNames = new String[] { "ServerConnectionId" };
        Object[] paramValues = new Object[] { GUIDS[0].toString() };
        QueryReturnValue queryResult = new QueryReturnValue();
        when(backend.runQuery(eq(QueryType.GetStorageServerConnectionById), eqParams(StorageServerConnectionQueryParametersBase.class, addSession(paramNames), addSession(paramValues))))
        .thenReturn(queryResult);
        queryResult.setSucceeded(true);
        queryResult.setReturnValue(setUpStorageServerConnection());
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }


    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        assertEquals("", query);

        setUpEntityQueryExpectations(QueryType.GetStorageDomainsByStoragePoolId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[NAMES.length-1] },
                                     setUpStorageDomains(),
                                     failure);

    }

    protected List<org.ovirt.engine.core.common.businessentities.StorageDomain> setUpStorageDomains() {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomainStatic getEntityStatic(int index) {
        org.ovirt.engine.core.common.businessentities.StorageDomainStatic entity =
                mock(org.ovirt.engine.core.common.businessentities.StorageDomainStatic.class);
        return setUpEntityExpectations(entity, index, StorageType.NFS);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.StorageDomain getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = mock(org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        return setUpEntityExpectations(entity, index, StorageType.NFS);
    }

    private static org.ovirt.engine.core.common.businessentities.StorageDomainStatic setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.StorageDomainStatic entity,
            int index,
            StorageType storageType) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getStorageDomainType()).thenReturn(StorageDomainType.Master);
        when(entity.getStorageType()).thenReturn(storageType);
        when(entity.getStorage()).thenReturn(GUIDS[0].toString());
        return entity;
    }

    private static org.ovirt.engine.core.common.businessentities.StorageDomain setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.StorageDomain entity, int index, StorageType storageType) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getStatus()).thenReturn(StorageDomainStatus.Active);
        when(entity.getStorageDomainType()).thenReturn(StorageDomainType.Master);
        when(entity.getStorageType()).thenReturn(storageType);
        when(entity.getStorage()).thenReturn(GUIDS[0].toString());
        when(entity.getStorageStaticData()).thenReturn(new StorageDomainStatic());
        return entity;
    }

    @Override
    protected void verifyModel(StorageDomain model, int index) {
        verifyStorageDomain(model, index);
        verifyLinks(model);
    }

    private static void verifyStorageDomain(StorageDomain model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertNotNull(model.getDataCenter());
        assertEquals(GUIDS[NAMES.length-1].toString(), model.getDataCenter().getId());
        assertEquals(org.ovirt.engine.api.model.StorageDomainStatus.ACTIVE, model.getStatus());
        assertEquals(true, model.isMaster());
    }

    @Override
    protected List<StorageDomain> getCollection() {
        return collection.list().getStorageDomains();
    }
}
