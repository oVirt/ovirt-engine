package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.MergeSnapshotParamenters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.classextension.EasyMock.expect;

public class BackendSnapshotsResourceTest
        extends AbstractBackendCollectionResourceTest<Snapshot, DiskImage, BackendSnapshotsResource> {

    static final Guid[] SNAPSHOT_IDS = GUIDS;
    static final Guid[] IMAGE_IDS = { new Guid("55555555-5555-5555-5555-555555555555"),
                                      new Guid("66666666-6666-6666-6666-666666666666"),
                                      new Guid("77777777-7777-7777-7777-777777777777") };

    static final Guid TASK_ID = new Guid("88888888-8888-8888-8888-888888888888");
    static final Guid VM_ID = GUIDS[3];

    public BackendSnapshotsResourceTest() {
        super(new BackendSnapshotsResource(VM_ID), null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    public void testRemoveNotFound() throws Exception {
        setUpGetEntityExpectations(1);
        control.replay();
        try {
            collection.remove("c5cf0cd1-8580-44a8-b2e5-f6f9e4bc8f70");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(2);
        setUpActionExpectations(VdcActionType.MergeSnapshot,
                                MergeSnapshotParamenters.class,
                                new String[] { "SourceVmSnapshotId", "DestVmSnapshotId", "VmId" },
                                new Object[] { SNAPSHOT_IDS[1], SNAPSHOT_IDS[2], VM_ID },
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
        setUpGetEntityExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.MergeSnapshot,
                                           MergeSnapshotParamenters.class,
                                           new String[] { "SourceVmSnapshotId", "DestVmSnapshotId", "VmId" },
                                           new Object[] { SNAPSHOT_IDS[1], SNAPSHOT_IDS[2], VM_ID },
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

    private void doTestAddAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus creationStatus) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setUpCreationExpectations(VdcActionType.CreateAllSnapshotsFromVm,
                                  CreateAllSnapshotsFromVmParameters.class,
                                  new String[] { "Description", "VmId" },
                                  new Object[] { DESCRIPTIONS[0], VM_ID },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(TASK_ID),
                                  asList(new AsyncTaskStatus(asyncStatus)),
                                  VdcQueryType.GetAllDisksByVmId,
                                  GetAllDisksByVmIdParameters.class,
                                  new String[] { "VmId" },
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

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpGetEntityExpectations(1);
        setUpGetEntityExpectations(1);
        setUpCreationExpectations(VdcActionType.CreateAllSnapshotsFromVm,
                                  CreateAllSnapshotsFromVmParameters.class,
                                  new String[] { "Description", "VmId" },
                                  new Object[] { DESCRIPTIONS[0], VM_ID },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[1]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetAllDisksByVmId,
                                  GetAllDisksByVmIdParameters.class,
                                  new String[] { "VmId" },
                                  new Object[] { VM_ID },
                                  getEntity(0));
        Snapshot snapshot = new Snapshot();
        snapshot.setDescription(DESCRIPTIONS[0]);

        Response response = collection.add(snapshot);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Snapshot);
        verifyModel((Snapshot)response.getEntity(), 0);
        assertNull(((Snapshot)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddFromScratchCantDo() throws Exception {
        doTestBadAddFromScratch(false, true, CANT_DO);
    }

    @Test
    public void testAddFromScratchFailure() throws Exception {
        doTestBadAddFromScratch(true, false, FAILURE);
    }

    private void doTestBadAddFromScratch(boolean canDo, boolean success, String detail)
            throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.CreateAllSnapshotsFromVm,
                                           CreateAllSnapshotsFromVmParameters.class,
                                           new String[] { "Description", "VmId" },
                                           new Object[] { DESCRIPTIONS[0], VM_ID },
                                           canDo,
                                           success));
        Snapshot snapshot = new Snapshot();
        snapshot.setDescription(DESCRIPTIONS[0]);

        try {
            collection.add(snapshot);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Snapshot snapshot = new Snapshot();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(snapshot);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Snapshot", "add", "description");
        }
    }

    @Override
    protected List<Snapshot> getCollection() {
        return collection.list().getSnapshots();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assert(query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetAllDisksByVmId,
                                     GetAllDisksByVmIdParameters.class,
                                     new String[] { "VmId" },
                                     new Object[] { VM_ID },
                                     setUpImages(),
                                     failure);

        control.replay();
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetAllDisksByVmId,
                                     GetAllDisksByVmIdParameters.class,
                                     new String[] { "VmId" },
                                     new Object[] { VM_ID },
                                     setUpImages());
        }
    }

    @Override
    protected DiskImage getEntity(int index) {
        DiskImage image = control.createMock(DiskImage.class);
        expect(image.getId()).andReturn(IMAGE_IDS[index]).anyTimes();
        ArrayList<DiskImage> snapshots = new ArrayList<DiskImage>();
        Guid parentId = Guid.Empty.getValue();
        for (int i = 0 ; i < NAMES.length ; i++) {
            DiskImage snapshot = control.createMock(DiskImage.class);
            Guid snapshotImageId = mangle(IMAGE_IDS[index], i);
            expect(snapshot.getId()).andReturn(snapshotImageId).anyTimes();
            expect(snapshot.getvm_snapshot_id()).andReturn(SNAPSHOT_IDS[i]).anyTimes();
            expect(snapshot.getParentId()).andReturn(parentId).anyTimes();
            expect(snapshot.getdescription()).andReturn(DESCRIPTIONS[i]).anyTimes();
            snapshots.add(snapshot);
            parentId = snapshotImageId;
        }
        expect(image.getSnapshots()).andReturn(snapshots).anyTimes();
        return image;
    }

    static Guid mangle(Guid imageId, int index) {
        return new Guid(imageId.toString().replace(imageId.toString().charAt(0),
                                            Integer.toString(index).charAt(0)));
    }

    protected List<DiskImage> setUpImages() {
        List<DiskImage> images = new ArrayList<DiskImage>();
        for (int i = 0; i < NAMES.length; i++) {
            images.add(getEntity(i));
        }
        return images;
    }

    protected void verifyModel(Snapshot model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(Snapshot model, int index) {
        for (Link link : model.getLinks()) {
            if ("prev".equals(link.getRel())) {
                assertTrue(link.getHref().startsWith("/api/vms/" + VM_ID + "/snapshots/"));
                // assert that prev link points to the preceding snapshot ID
                // (checking the difference between the last chars will suffice,
                //  as all snapshots IDs are sequences of repeated integers)
                assertEquals(1, last(model.getId()) - last(link.getHref()));
            }
        }
        if (model.getDisk() != null) {
            assertEquals(model.getDisk().getId(), IMAGE_IDS[0].toString());
        }
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(DESCRIPTIONS[index], model.getDescription());
    }

    protected static int last(String str) {
        return Integer.valueOf(str.charAt(str.length() - 1));
    }
}
