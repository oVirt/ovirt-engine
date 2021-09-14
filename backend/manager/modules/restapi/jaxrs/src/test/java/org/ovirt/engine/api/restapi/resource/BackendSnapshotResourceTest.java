package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendSnapshotResourceTest
    extends AbstractBackendSubResourceTest<Snapshot, org.ovirt.engine.core.common.businessentities.Snapshot, BackendSnapshotResource> {

    private static final Guid VM_ID = GUIDS[0];
    private static final Guid SNAPSHOT_ID = GUIDS[1];
    private static final String SNAPSHOT_DESCRIPTION = DESCRIPTIONS[1];

    public BackendSnapshotResourceTest() {
        super(new BackendSnapshotResource(SNAPSHOT_ID.toString(), VM_ID, null));
    }

    private BackendSnapshotsResource getSnapshotsResource() {
        BackendSnapshotsResource snapshotsResource = new BackendSnapshotsResource(VM_ID);
        initResource(snapshotsResource);
        return snapshotsResource;
    }

    @BeforeEach
    public void initParentResource() {
        resource.setCollectionResource(getSnapshotsResource());
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(getEntity(1));
        verifyModel(resource.get(), 1);
    }

    @Test
    public void testGetWithPopulate() {
        List<String> populates = new ArrayList<>();
        populates.add("true");
        String ovfData = "data";
        org.ovirt.engine.core.common.businessentities.Snapshot resultSnapshot = new org.ovirt.engine.core.common.businessentities.Snapshot(false);
        resultSnapshot.setVmConfiguration(ovfData);
        resultSnapshot.setId(SNAPSHOT_ID);
        resultSnapshot.setDescription(SNAPSHOT_DESCRIPTION);
        when(httpHeaders.getRequestHeader(BackendResource.ALL_CONTENT_HEADER)).thenReturn(populates);
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(resultSnapshot);
        Snapshot snapshot = resource.get();
        verifyModel(snapshot, 1);
        assertNotNull(snapshot.getInitialization());
        assertNotNull(snapshot.getInitialization().getConfiguration());
        assertEquals(ovfData, snapshot.getInitialization().getConfiguration().getData());
        assertEquals(ConfigurationType.OVF, snapshot.getInitialization().getConfiguration().getType());
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(null);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testRestore() {
        setUriInfo(setUpBasicUriExpectations());
        setUpTryBackExpectations();
        setUpRestoreExpectations();
        resource.restore(new Action());
    }

    @Test
    public void testRemove() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(getEntity(1));
        setUpActionExpectations(
            ActionType.RemoveSnapshot,
            RemoveSnapshotParameters.class,
            new String[] { "SnapshotId", "VmId" },
            new Object[] { SNAPSHOT_ID, VM_ID },
            true,
            true
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

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(getEntity(1));
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveSnapshot,
                RemoveSnapshotParameters.class,
                new String[] { "SnapshotId", "VmId"},
                new Object[] { SNAPSHOT_ID, VM_ID },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.remove()), detail);
    }

    protected UriInfo setUpTryBackExpectations() {
        return setUpActionExpectations(
                ActionType.TryBackToAllSnapshotsOfVm,
                TryBackToAllSnapshotsOfVmParameters.class,
                new String[] { "VmId", "DstSnapshotId" },
                new Object[] { VM_ID, SNAPSHOT_ID },
                true,
                true,
                null
        );
    }

    protected UriInfo setUpRestoreExpectations() {
        return setUpActionExpectations(
                ActionType.RestoreAllSnapshots,
                RestoreAllSnapshotsParameters.class,
                new String[] { "VmId", "SnapshotAction" },
                new Object[] { VM_ID, SnapshotActionEnum.COMMIT },
                true,
                true,
                null);
    }

    protected void setUpGetEntityExpectations(org.ovirt.engine.core.common.businessentities.Snapshot result) {
        setUpGetEntityExpectations(QueryType.GetSnapshotBySnapshotId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { SNAPSHOT_ID },
                                   result);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Snapshot getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Snapshot snapshot = mock(org.ovirt.engine.core.common.businessentities.Snapshot.class);
        when(snapshot.getId()).thenReturn(GUIDS[index]);
        when(snapshot.getDescription()).thenReturn(DESCRIPTIONS[index]);
        return snapshot;
    }

    @Override
    protected void verifyModel(Snapshot model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }
}
