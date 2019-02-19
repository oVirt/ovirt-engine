package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendAttachedStorageDomainResourceTest
    extends AbstractBackendSubResourceTest<StorageDomain,
                                           org.ovirt.engine.core.common.businessentities.StorageDomain,
                                           BackendAttachedStorageDomainResource> {

    private static final Guid STORAGE_DOMAIN_ID = GUIDS[0];
    private static final Guid DATA_CENTER_ID = GUIDS[1];
    private static final Guid STORAGE_CONNECTION_ID = GUIDS[2];

    public BackendAttachedStorageDomainResourceTest() {
        super(new BackendAttachedStorageDomainResource(STORAGE_DOMAIN_ID.toString(), DATA_CENTER_ID));
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(
                WebApplicationException.class, () -> new BackendAttachedStorageDomainResource("foo", null)));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetDomainExpectations(StorageType.NFS, false);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUpGetDomainExpectations(StorageType.NFS, true);
        setUpGetConnectionExpectations();
        setUriInfo(setUpBasicUriExpectations());
        verifyStorageDomain(resource.get());
    }

    @Test
    public void testActivate() {
        setUriInfo(
            setUpActionExpectations(
                ActionType.ActivateStorageDomain,
                StorageDomainPoolParametersBase.class,
                new String[]{"StorageDomainId", "StoragePoolId"},
                new Object[]{STORAGE_DOMAIN_ID, DATA_CENTER_ID}
            )
        );
        Action action = new Action();
        verifyActionResponse(resource.activate(action));
    }

    @Test
    public void testActivateAsyncPending() {
        doTestActivateAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testActivateAsyncInProgress() {
        doTestActivateAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testActivateAsyncFinished() {
        doTestActivateAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestActivateAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) {
        setUriInfo(
            setUpActionExpectations(
                ActionType.ActivateStorageDomain,
                StorageDomainPoolParametersBase.class,
                new String[] { "StorageDomainId", "StoragePoolId" },
                new Object[] { STORAGE_DOMAIN_ID, DATA_CENTER_ID },
                asList(GUIDS[1]),
                asList(new AsyncTaskStatus(asyncStatus))
            )
        );
        Response response = resource.activate(new Action());
        verifyActionResponse(
            response,
            "datacenters/" + DATA_CENTER_ID + "/storagedomains/" + STORAGE_DOMAIN_ID,
            true,
            null
        );
        Action action = (Action) response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus());
    }

    @Test
    public void testDeactivate() {
        setUriInfo(
            setUpActionExpectations(
                ActionType.DeactivateStorageDomainWithOvfUpdate,
                StorageDomainPoolParametersBase.class,
                new String[]{"StorageDomainId", "StoragePoolId"},
                new Object[]{STORAGE_DOMAIN_ID, DATA_CENTER_ID}
            )
        );
        Action action = new Action();
        verifyActionResponse(resource.deactivate(action));
    }

    @Test
    public void testRemove() {
        setUpGetDomainExpectations(StorageType.NFS, true);
        setUpGetConnectionExpectations();
        setUriInfo(
            setUpActionExpectations(
                ActionType.DetachStorageDomainFromPool,
                DetachStorageDomainFromPoolParameters.class,
                new String[] { "StorageDomainId", "StoragePoolId" },
                new Object[] { STORAGE_DOMAIN_ID, DATA_CENTER_ID },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }


    @Test
    public void testRemoveNonExistant() {
        setUpGetDomainExpectations(StorageType.NFS, false);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveLocalStorage() {
        setUpGetDomainExpectations(StorageType.LOCALFS, true);
        setUpGetConnectionExpectations();
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveStorageDomain,
                RemoveStorageDomainParameters.class,
                new String[] { "StorageDomainId"},
                new Object[] { STORAGE_DOMAIN_ID },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    private void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetDomainExpectations(StorageType.NFS, true);
        setUpGetConnectionExpectations();
        setUriInfo(
            setUpActionExpectations(
                ActionType.DetachStorageDomainFromPool,
                DetachStorageDomainFromPoolParameters.class,
                new String[]{"StorageDomainId", "StoragePoolId"},
                new Object[]{STORAGE_DOMAIN_ID, DATA_CENTER_ID},
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    private void setUpGetDomainExpectations(StorageType storageType, boolean succeed) {
        setUpGetEntityExpectations(
            QueryType.GetStorageDomainByIdAndStoragePoolId,
            StorageDomainAndPoolQueryParameters.class,
            new String[]{"StorageDomainId", "StoragePoolId"},
            new Object[]{STORAGE_DOMAIN_ID, DATA_CENTER_ID},
            succeed ? setUpDomainExpectations(storageType) : null
        );
    }

    protected UriInfo setUpActionExpectations(ActionType task,
                                              Class<? extends ActionParametersBase> clz,
                                              String[] names,
                                              Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
                                              Class<? extends ActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              ArrayList<Guid> asyncTasks,
                                              ArrayList<AsyncTaskStatus> asyncStatuses) {
        String uri = "datacenters/" + DATA_CENTER_ID + "/storagedomains/" + STORAGE_DOMAIN_ID + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, null, null, uri, true);
    }

    private void verifyActionResponse(Response r) {
        verifyActionResponse(r, "datacenters/" + DATA_CENTER_ID + "/storagedomains/" + STORAGE_DOMAIN_ID, false);
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain setUpDomainExpectations(StorageType type) {
        org.ovirt.engine.core.common.businessentities.StorageDomain domain =
            mock(org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        when(domain.getId()).thenReturn(STORAGE_DOMAIN_ID);
        when(domain.getStorageDomainType()).thenReturn(StorageDomainType.Master);
        when(domain.getStorageType()).thenReturn(type);
        when(domain.getStorage()).thenReturn(STORAGE_CONNECTION_ID.toString());
        when(domain.getStorageStaticData()).thenReturn(new StorageDomainStatic());
        return domain;
    }

    private StorageServerConnections setUpConnectionExpectations() {
        StorageServerConnections connection = new StorageServerConnections();
        connection.setId(STORAGE_CONNECTION_ID.toString());
        connection.setConnection("10.11.12.13" + ":" + "/1");
        return connection;
    }

    private void setUpGetConnectionExpectations() {
        setUpGetEntityExpectations(
            QueryType.GetStorageServerConnectionById,
            StorageServerConnectionQueryParametersBase.class,
            new String[] { "ServerConnectionId" },
            new Object[] { STORAGE_CONNECTION_ID.toString() },
            setUpConnectionExpectations()
        );
    }

    private void verifyStorageDomain(StorageDomain model) {
        assertEquals(STORAGE_DOMAIN_ID.toString(), model.getId());
        assertNotNull(model.getDataCenter());
        assertEquals(DATA_CENTER_ID.toString(), model.getDataCenter().getId());
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.StorageDomain getEntity(int index) {
        return setUpDomainExpectations(StorageType.NFS);
    }
}
