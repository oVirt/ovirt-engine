package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendSnapshotsResourceTest
        extends AbstractBackendCollectionResourceTest<Snapshot, org.ovirt.engine.core.common.businessentities.Snapshot, BackendSnapshotsResource> {

    public BackendSnapshotsResourceTest() {
        super(new BackendSnapshotsResource(VM_ID), null, "");
    }

    @Override
    @BeforeEach
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
    public void testAddAsyncPending() {
        doTestAddAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testAddAsyncInProgress() {
        doTestAddAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testAddAsyncFinished() {
        doTestAddAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    @Test
    @Override
    public void testList() {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpGetEntityExpectations(1);
        setUpGetSnapshotVmConfiguration(SNAPSHOT_IDS[0]);
        setUpGetSnapshotVmConfiguration(SNAPSHOT_IDS[1]);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testGetWithPopulate() {
        List<String> populates = new ArrayList<>();
        populates.add("true");
        String ovfData = "data";
        org.ovirt.engine.core.common.businessentities.Snapshot resultSnapshot0 = new org.ovirt.engine.core.common.businessentities.Snapshot();
        resultSnapshot0.setVmConfiguration(ovfData);
        resultSnapshot0.setId(SNAPSHOT_IDS[0]);
        org.ovirt.engine.core.common.businessentities.Snapshot resultSnapshot1 = new org.ovirt.engine.core.common.businessentities.Snapshot();
        resultSnapshot1.setVmConfiguration(ovfData);
        resultSnapshot1.setId(SNAPSHOT_IDS[1]);
        when(httpHeaders.getRequestHeader(BackendResource.ALL_CONTENT_HEADER)).thenReturn(populates);
        UriInfo uriInfo = setUpUriExpectations(null);
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setUpGetSnapshotVmConfiguration(SNAPSHOT_IDS[0]);
        setUpGetSnapshotVmConfiguration(SNAPSHOT_IDS[1]);
        setUpEntityQueryExpectations(QueryType.GetSnapshotBySnapshotId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{SNAPSHOT_IDS[1]},
                resultSnapshot0);
        setUpEntityQueryExpectations(QueryType.GetSnapshotBySnapshotId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{SNAPSHOT_IDS[0]},
                resultSnapshot1);
        collection.setUriInfo(uriInfo);
        List<Snapshot> snapshots =  getCollection();
        verifyCollection(snapshots);
        for (int i = 0; i < 2; i++) {
            verifyAllContent(snapshots.get(i), ConfigurationType.OVF, ovfData);
        }
    }

    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Test
    @Disabled
    @Override
    public void testListFailure() {
    }

    @Test
    @Disabled
    @Override
    public void testListCrash() {
    }

    @Test
    @Override
    @Disabled
    public void testListCrashClientLocale() {
    }

    private void doTestAddAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus creationStatus) {
        setUriInfo(setUpBasicUriExpectations());
        String ovfData = "data";
        org.ovirt.engine.core.common.businessentities.Snapshot resultSnapshot0 = new org.ovirt.engine.core.common.businessentities.Snapshot(false);
        resultSnapshot0.setVmConfiguration(ovfData);
        resultSnapshot0.setId(SNAPSHOT_IDS[0]);
        resultSnapshot0.setDescription(DESCRIPTIONS[0]);
        setUpCreationExpectations(ActionType.CreateSnapshotForVm,
                CreateSnapshotForVmParameters.class,
                new String[] { "Description", "VmId" },
                new Object[] { DESCRIPTIONS[0], VM_ID },
                true,
                true,
                GUIDS[0],
                asList(TASK_ID),
                asList(new AsyncTaskStatus(asyncStatus)),
                QueryType.GetSnapshotBySnapshotId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { SNAPSHOT_IDS[0] },
                resultSnapshot0);
        Snapshot snapshot = new Snapshot();
        snapshot.setDescription(DESCRIPTIONS[0]);

        Response response = collection.add(snapshot);
        assertEquals(202, response.getStatus());
        assertTrue(response.getEntity() instanceof Snapshot);
        Snapshot responseSnapshot = (Snapshot)response.getEntity();
        verifyModel(responseSnapshot, 0);
        verifyAllContent(responseSnapshot, ConfigurationType.OVF, ovfData);
        assertNotNull(responseSnapshot.getCreationStatus());
        assertEquals(creationStatus.value(), responseSnapshot.getCreationStatus());
    }

    protected void setUpGetEntityExpectations(int times) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetAllVmSnapshotsByVmId,
                    IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { VM_ID },
                                     getEntities());
        }
    }

    protected void setUpGetSnapshotVmConfiguration(Guid snpashotId) {
        setUpEntityQueryExpectations(QueryType.GetVmConfigurationBySnapshot,
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
    protected void verifyCollection(List<Snapshot> collection) {
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
        OsRepository mockOsRepository = mock(OsRepository.class);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, mockOsRepository);
    }
}
