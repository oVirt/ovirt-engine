package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

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
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendAttachedStorageDomainResource("foo", null);
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetDomainExpectations(StorageType.NFS, false);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUpGetDomainExpectations(StorageType.NFS, true);
        setUpGetConnectionExpectations();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        verifyStorageDomain(resource.get());
    }

    @Test
    public void testActivate() throws Exception {
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.ActivateStorageDomain,
                StorageDomainPoolParametersBase.class,
                new String[]{"StorageDomainId", "StoragePoolId"},
                new Object[]{STORAGE_DOMAIN_ID, DATA_CENTER_ID}
            )
        );
        Action action = new Action();
        verifyActionResponse(resource.activate(action));
    }

    @Test
    public void testActivateAsyncPending() throws Exception {
        doTestActivateAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testActivateAsyncInProgress() throws Exception {
        doTestActivateAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testActivateAsyncFinished() throws Exception {
        doTestActivateAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestActivateAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) throws Exception {
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.ActivateStorageDomain,
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
    public void testDeactivate() throws Exception {
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.DeactivateStorageDomainWithOvfUpdate,
                StorageDomainPoolParametersBase.class,
                new String[]{"StorageDomainId", "StoragePoolId"},
                new Object[]{STORAGE_DOMAIN_ID, DATA_CENTER_ID}
            )
        );
        Action action = new Action();
        verifyActionResponse(resource.deactivate(action));
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetDomainExpectations(StorageType.NFS, true);
        setUpGetConnectionExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.DetachStorageDomainFromPool,
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
    public void testRemoveNonExistant() throws Exception{
        setUpGetDomainExpectations(StorageType.NFS, false);
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveLocalStorage() throws Exception {
        setUpGetDomainExpectations(StorageType.LOCALFS, true);
        setUpGetConnectionExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveStorageDomain,
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
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    private void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpGetDomainExpectations(StorageType.NFS, true);
        setUpGetConnectionExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.DetachStorageDomainFromPool,
                DetachStorageDomainFromPoolParameters.class,
                new String[]{"StorageDomainId", "StoragePoolId"},
                new Object[]{STORAGE_DOMAIN_ID, DATA_CENTER_ID},
                valid,
                success
            )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    private void setUpGetDomainExpectations(StorageType storageType, boolean succeed) throws Exception {
        setUpGetEntityExpectations(
            VdcQueryType.GetStorageDomainByIdAndStoragePoolId,
            StorageDomainAndPoolQueryParameters.class,
            new String[]{"StorageDomainId", "StoragePoolId"},
            new Object[]{STORAGE_DOMAIN_ID, DATA_CENTER_ID},
            succeed ? setUpDomainExpectations(storageType) : null
        );
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              ArrayList<Guid> asyncTasks,
                                              ArrayList<AsyncTaskStatus> asyncStatuses) {
        String uri = "datacenters/" + DATA_CENTER_ID + "/storagedomains/" + STORAGE_DOMAIN_ID + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, null, null, uri, true);
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "datacenters/" + DATA_CENTER_ID + "/storagedomains/" + STORAGE_DOMAIN_ID, false);
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain setUpDomainExpectations(StorageType type) {
        org.ovirt.engine.core.common.businessentities.StorageDomain domain =
            control.createMock(org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        expect(domain.getId()).andReturn(STORAGE_DOMAIN_ID).anyTimes();
        expect(domain.getStorageDomainType()).andReturn(StorageDomainType.Master).anyTimes();
        expect(domain.getStorageType()).andReturn(type).anyTimes();
        expect(domain.getStorage()).andReturn(STORAGE_CONNECTION_ID.toString()).anyTimes();
        return domain;
    }

    private StorageServerConnections setUpConnectionExpectations() {
        StorageServerConnections connection = new StorageServerConnections();
        connection.setId(STORAGE_CONNECTION_ID.toString());
        connection.setConnection("10.11.12.13" + ":" + "/1");
        return connection;
    }

    private void setUpGetConnectionExpectations() throws Exception {
        setUpGetEntityExpectations(
            VdcQueryType.GetStorageServerConnectionById,
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
