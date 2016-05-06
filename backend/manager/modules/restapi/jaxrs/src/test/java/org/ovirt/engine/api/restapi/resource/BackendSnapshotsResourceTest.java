package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;

public class BackendSnapshotsResourceTest
        extends AbstractBackendCollectionResourceTest<Snapshot, org.ovirt.engine.core.common.businessentities.Snapshot, BackendSnapshotsResource> {

    public BackendSnapshotsResourceTest() {
        super(new BackendSnapshotsResource(VM_ID), null, "");
    }

    @Override
    public void setUp() {
        super.setUp();
        mockOsRepository();
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
    public void testGetWithPopulate() throws Exception {
        List<String> populates = new ArrayList<>();
        populates.add("true");
        String ovfData = "data";
        org.ovirt.engine.core.common.businessentities.Snapshot resultSnapshot0 = new org.ovirt.engine.core.common.businessentities.Snapshot();
        resultSnapshot0.setVmConfiguration(ovfData);
        resultSnapshot0.setId(SNAPSHOT_IDS[0]);
        org.ovirt.engine.core.common.businessentities.Snapshot resultSnapshot1 = new org.ovirt.engine.core.common.businessentities.Snapshot();
        resultSnapshot1.setVmConfiguration(ovfData);
        resultSnapshot1.setId(SNAPSHOT_IDS[1]);
        expect(httpHeaders.getRequestHeader(BackendResource.POPULATE)).andReturn(populates).anyTimes();
        UriInfo uriInfo = setUpUriExpectations(null);
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setUpGetSnapshotVmConfiguration(SNAPSHOT_IDS[0]);
        setUpGetSnapshotVmConfiguration(SNAPSHOT_IDS[1]);
        setUpEntityQueryExpectations(VdcQueryType.GetSnapshotBySnapshotId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{SNAPSHOT_IDS[1]},
                resultSnapshot0);
        setUpEntityQueryExpectations(VdcQueryType.GetSnapshotBySnapshotId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{SNAPSHOT_IDS[0]},
                resultSnapshot1);
        collection.setUriInfo(uriInfo);
        control.replay();
        List<Snapshot> snapshots =  getCollection();
        verifyCollection(snapshots);
        for (int i = 0; i < 2; i++) {
            verifyAllContent(snapshots.get(i), ConfigurationType.OVF, ovfData);
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
        String ovfData = "data";
        org.ovirt.engine.core.common.businessentities.Snapshot resultSnapshot0 = new org.ovirt.engine.core.common.businessentities.Snapshot();
        resultSnapshot0.setVmConfiguration(ovfData);
        resultSnapshot0.setId(SNAPSHOT_IDS[0]);
        setUpEntityQueryExpectations(VdcQueryType.GetSnapshotBySnapshotId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{SNAPSHOT_IDS[0]},
                resultSnapshot0);
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
        Snapshot responseSnapshot = (Snapshot)response.getEntity();
        verifyModel(responseSnapshot, 0);
        verifyAllContent(responseSnapshot, ConfigurationType.OVF, ovfData);
        Snapshot created = (Snapshot)response.getEntity();
        assertNotNull(created.getCreationStatus());
        assertEquals(creationStatus.value(), created.getCreationStatus());
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
        List<org.ovirt.engine.core.common.businessentities.Snapshot> entities = new ArrayList<>();
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

    private void verifyAllContent(Snapshot model, ConfigurationType type, String data) {
        assertNotNull(model.getInitialization());
        assertNotNull(model.getInitialization().getConfiguration());
        assertEquals(data, model.getInitialization().getConfiguration().getData());
        assertEquals(type, model.getInitialization().getConfiguration().getType());
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

    private void mockOsRepository() {
        OsRepository mockOsRepository = control.createMock(OsRepository.class);
        expect(mockOsRepository.getUniqueOsNames()).andReturn(new HashMap<>()).anyTimes();
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, mockOsRepository);
    }
}
