package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendSnapshotsResourceTest
        extends AbstractBackendCollectionResourceTest<Snapshot, org.ovirt.engine.core.common.businessentities.Snapshot, BackendSnapshotsResource> {

    public BackendSnapshotsResourceTest() {
        super(new BackendSnapshotsResource(VM_ID), null, "");
    }

    static final Guid[] SNAPSHOT_IDS = GUIDS;
    static final Date[] SNAPSHOT_DATES = {new Date(new GregorianCalendar(1978, 3, 1).getTimeInMillis()), new Date(new GregorianCalendar(1978, 3, 2).getTimeInMillis())};

    static final Guid TASK_ID = new Guid("88888888-8888-8888-8888-888888888888");
    static final Guid VM_ID = GUIDS[3];
    @Override
    protected List<Snapshot> getCollection() {
        return collection.list().getSnapshots();
    }
    @Override
    protected org.ovirt.engine.core.common.businessentities.Snapshot getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Snapshot entity = new org.ovirt.engine.core.common.businessentities.Snapshot();
        entity.setId(SNAPSHOT_IDS[index]);
        entity.setCreationDate(SNAPSHOT_DATES[index]);
        entity.setDescription(DESCRIPTIONS[index]);
        entity.setType(SnapshotType.REGULAR);
        entity.setVmId(VM_ID);
        return entity;
    }

    @Test
    public void testAddAsyncPending() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testAddAsyncInProgress() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testAddAsyncFinished() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    @Test
    @Override
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpGetEntityExpectations(1);
        setUpGetSnapshotVmConfiguration(SNAPSHOT_IDS[0]);
        setUpGetSnapshotVmConfiguration(SNAPSHOT_IDS[1]);
        collection.setUriInfo(uriInfo);
        control.replay();
        verifyCollection(getCollection());
    }

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setUpGetSnapshotVmConfiguration(SNAPSHOT_IDS[1]);
        setUpActionExpectations(VdcActionType.RemoveSnapshot,
                                RemoveSnapshotParameters.class,
                                new String[] { "SnapshotId", "VmId" },
                                new Object[] { SNAPSHOT_IDS[1], VM_ID },
                                true,
                                true);
        verifyRemove(collection.remove(SNAPSHOT_IDS[1].toString()));
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1);
        setUpGetSnapshotVmConfiguration(SNAPSHOT_IDS[1]);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveSnapshot,
                                           RemoveSnapshotParameters.class,
                                           new String[] { "SnapshotId", "VmId"},
                                           new Object[] { SNAPSHOT_IDS[1], VM_ID },
                                           canDo,
                                           success));
        try {
            collection.remove(SNAPSHOT_IDS[1].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
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

    private void doTestAddAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus creationStatus) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.CreateAllSnapshotsFromVm,
                CreateAllSnapshotsFromVmParameters.class,
                new String[] { "Description", "VmId" },
                new Object[] { DESCRIPTIONS[0], VM_ID },
                true,
                true,
                GUIDS[0],
                asList(TASK_ID),
                asList(new AsyncTaskStatus(asyncStatus)),
                VdcQueryType.GetAllVmSnapshotsByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                getEntity(0));
        Snapshot snapshot = new Snapshot();
        snapshot.setDescription(DESCRIPTIONS[0]);

        Response response = collection.add(snapshot);
        assertEquals(202, response.getStatus());
        assertTrue(response.getEntity() instanceof Snapshot);
        verifyModel((Snapshot)response.getEntity(), 0);
        Snapshot created = (Snapshot)response.getEntity();
        assertNotNull(created.getCreationStatus());
        assertEquals(creationStatus.value(), created.getCreationStatus().getState());
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetAllVmSnapshotsByVmId,
                    IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { VM_ID },
                                     getEntities());
        }
    }

    protected void setUpGetSnapshotVmConfiguration(Guid snpashotId) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVmConfigurationBySnapshot,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { snpashotId },
                getVmConfiguration());
    }

    protected List<org.ovirt.engine.core.common.businessentities.Snapshot> getEntities() {
        List<org.ovirt.engine.core.common.businessentities.Snapshot> entities = new ArrayList<org.ovirt.engine.core.common.businessentities.Snapshot>();
        for (int i = 0; i<2; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    @Override
    protected void verifyModel(Snapshot model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }

    @Override
    protected void verifyCollection(List<Snapshot> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(2, collection.size());
        for (int i = 0; i < 2; i++) {
            verifyModel(collection.get(i), i);
        }
    }

    private VM getVmConfiguration() {
        VM vm = new VM();
        return vm;
    }

}
