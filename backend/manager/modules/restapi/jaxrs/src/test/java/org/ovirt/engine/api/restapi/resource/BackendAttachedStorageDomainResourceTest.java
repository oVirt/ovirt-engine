package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendAttachedStorageDomainsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendAttachedStorageDomainsResourceTest.verifyStorageDomain;

public class BackendAttachedStorageDomainResourceTest
    extends AbstractBackendSubResourceTest<StorageDomain,
                                           org.ovirt.engine.core.common.businessentities.StorageDomain,
                                           BackendAttachedStorageDomainResource> {

    private static final Guid STORAGE_DOMAIN_ID = GUIDS[0];
    private static final Guid DATA_CENTER_ID = GUIDS[NAMES.length - 1];

    public BackendAttachedStorageDomainResourceTest() {
        super(new BackendAttachedStorageDomainResource(STORAGE_DOMAIN_ID.toString(), DATA_CENTER_ID));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendAttachedStorageDomainResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[0].toString() },
                setUpStorageServerConnection());
        setUpGetEntityExpectations();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        verifyStorageDomain(resource.get(), 0);
    }

    @Test
    public void testActivate() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ActivateStorageDomain,
                                           StorageDomainPoolParametersBase.class,
                                           new String[] { "StorageDomainId", "StoragePoolId" },
                                           new Object[] { STORAGE_DOMAIN_ID, DATA_CENTER_ID }));

        verifyActionResponse(resource.activate(new Action()));
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
        setUriInfo(setUpActionExpectations(VdcActionType.ActivateStorageDomain,
                                           StorageDomainPoolParametersBase.class,
                                           new String[] { "StorageDomainId", "StoragePoolId" },
                                           new Object[] { STORAGE_DOMAIN_ID, DATA_CENTER_ID },
                                           asList(GUIDS[1]),
                                           asList(new AsyncTaskStatus(asyncStatus))));

        Response response = resource.activate(new Action());
        verifyActionResponse(response, "datacenters/" + DATA_CENTER_ID + "/storagedomains/" + STORAGE_DOMAIN_ID, true, null, null);
        Action action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus().getState());

    }

    @Test
    public void testDeactivate() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.DeactivateStorageDomainWithOvfUpdate,
                                           StorageDomainPoolParametersBase.class,
                                           new String[] { "StorageDomainId", "StoragePoolId" },
                                           new Object[] { STORAGE_DOMAIN_ID, DATA_CENTER_ID }));

        verifyActionResponse(resource.deactivate(new Action()));
    }

    protected void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(false);
    }

    protected void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStorageDomainByIdAndStoragePoolId,
                                   StorageDomainAndPoolQueryParameters.class,
                                   new String[] { "StorageDomainId", "StoragePoolId" },
                                   new Object[] { STORAGE_DOMAIN_ID, DATA_CENTER_ID },
                                   notFound ? null : getEntity(0));
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

    @Override
    protected org.ovirt.engine.core.common.businessentities.StorageDomain getEntity(int index) {
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.StorageDomain.class), index, StorageType.NFS);
    }

    static StorageServerConnections setUpStorageServerConnection() {
        StorageServerConnections cnx = new StorageServerConnections();
            cnx.setid(GUIDS[0].toString());
            cnx.setconnection("10.11.12.13" + ":" + "/1");
        return cnx;
    }
}
