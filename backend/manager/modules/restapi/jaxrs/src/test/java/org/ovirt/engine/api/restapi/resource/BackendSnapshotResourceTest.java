package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import static org.easymock.EasyMock.expect;

public class BackendSnapshotResourceTest extends AbstractBackendSubResourceTest<Snapshot, org.ovirt.engine.core.common.businessentities.Snapshot, BackendSnapshotResource> {

    private final static Guid VM_ID = GUIDS[0];
    private final static Guid SNAPSHOT_ID = GUIDS[1];
    private final static Guid JOB_ID = GUIDS[2];
    protected static final String BASE_HREF = "vms/" + VM_ID + "/snapshots/" + SNAPSHOT_ID;

    public BackendSnapshotResourceTest() {
        super(new BackendSnapshotResource(SNAPSHOT_ID.toString(), VM_ID, null));
    }

    private BackendSnapshotsResource getSnapshotsResource() {
        BackendSnapshotsResource snapshotsResource = new BackendSnapshotsResource(VM_ID);
        initResource(snapshotsResource);
        return snapshotsResource;
    }

    @Before
    public void initParentResource() {
        resource.setCollectionResource(getSnapshotsResource());
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(asList(getEntity(1)));
        control.replay();
        verifyModel(resource.get(), 1);
    }

    @Test
    //empty list of snapshots returned from Backend.
    public void testGetNotFound_1() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(new ArrayList<org.ovirt.engine.core.common.businessentities.Snapshot>());
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    //non-empty list of snapshots returned from Backend,
    //but this specific snapshot is not there.
    public void testGetNotFound_2() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(asList(getEntity(2)));
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRestore() {
        setUriInfo(setUpBasicUriExpectations());
        setUpTryBackExpectations();
        setUpRestoreExpectations();
        control.replay();
        resource.restore(new Action());
    }

    protected UriInfo setUpTryBackExpectations() {
        return setUpActionExpectations(
                VdcActionType.TryBackToAllSnapshotsOfVm,
                TryBackToAllSnapshotsOfVmParameters.class,
                new String[] { "VmId", "DstSnapshotId" },
                new Object[] { VM_ID, SNAPSHOT_ID },
                true,
                true,
                null,
                null,
                null,
                JOB_ID,
                JobExecutionStatus.FINISHED,
                BASE_HREF + "/action",
                false);
    }

    protected UriInfo setUpRestoreExpectations() {
        return setUpActionExpectations(
                VdcActionType.RestoreAllSnapshots,
                RestoreAllSnapshotsParameters.class,
                new String[] { "VmId", "DstSnapshotId" },
                new Object[] { VM_ID, SNAPSHOT_ID },
                true,
                true,
                null,
                false);
    }

    protected void setUpGetEntityExpectations(List<org.ovirt.engine.core.common.businessentities.Snapshot> result) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetAllVmSnapshotsByVmId,
                                   GetAllVmSnapshotsByVmIdParameters.class,
                                   new String[] { "VmId" },
                                   new Object[] { VM_ID },
                                   result);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Snapshot getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Snapshot snapshot = control.createMock(org.ovirt.engine.core.common.businessentities.Snapshot.class);
        expect(snapshot.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(snapshot.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        return snapshot;
    }

    @Override
    protected void verifyModel(Snapshot model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }
}
