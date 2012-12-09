package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.SnapshotCdRomsResource;
import org.ovirt.engine.api.resource.SnapshotDisksResource;
import org.ovirt.engine.api.resource.SnapshotNicsResource;
import org.ovirt.engine.api.resource.SnapshotResource;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendSnapshotsResource.SUB_COLLECTIONS;

public class BackendSnapshotResource extends AbstractBackendActionableResource<Snapshot, org.ovirt.engine.core.common.businessentities.Snapshot> implements SnapshotResource {

    private static final String RESTORE_SNAPSHOT_CORRELATION_ID = "RestoreSnapshot";
    protected Guid parentId;
    protected BackendSnapshotsResource collection;

    public BackendSnapshotResource(String id, Guid parentId, BackendSnapshotsResource collection) {
        super(id, Snapshot.class, org.ovirt.engine.core.common.businessentities.Snapshot.class, SUB_COLLECTIONS);
        this.parentId = parentId;
        this.collection = collection;
    }

    @Override
    public Snapshot get() {
        org.ovirt.engine.core.common.businessentities.Snapshot entity = getSnapshot();
        Snapshot snapshot = map(entity, null);
        snapshot = addLinks(snapshot);
        snapshot = collection.addVmConfiguration(entity, snapshot);
        return snapshot;
    }

    protected org.ovirt.engine.core.common.businessentities.Snapshot getSnapshot() {
        org.ovirt.engine.core.common.businessentities.Snapshot entity = collection.getSnapshotById(guid);
        if (entity==null) {
            notFound();
        }
        return entity;
    }

    @Override
    public Response restore(Action action) {
        action.setAsync(false);
        TryBackToAllSnapshotsOfVmParameters tryBackParams = new TryBackToAllSnapshotsOfVmParameters(parentId, guid);
        tryBackParams.setCorrelationId(RESTORE_SNAPSHOT_CORRELATION_ID); //TODO: if user supplied, override with user value
        Response response = doAction(VdcActionType.TryBackToAllSnapshotsOfVm,
                tryBackParams,
                action,
                PollingType.JOB);
        if (response.getStatus()==Response.Status.OK.getStatusCode()) {
            RestoreAllSnapshotsParameters restoreParams = new RestoreAllSnapshotsParameters(parentId, guid);
            restoreParams.setCorrelationId(RESTORE_SNAPSHOT_CORRELATION_ID);
            Response response2 = doAction(VdcActionType.RestoreAllSnapshots,
                    restoreParams,
                    action);
            if (response2.getStatus()!=Response.Status.OK.getStatusCode()) {
                return response2;
            }
        }
        return response;
    }

    @Override
    public CreationResource getCreationSubresource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    protected Snapshot addParents(Snapshot snapshot) {
        return collection.addParents(snapshot);
    }

    BackendSnapshotsResource getCollection() {
        return collection;
    }

    @Override
    public SnapshotCdRomsResource getSnapshotCdRomsResource() {
        return new BackendSnapshotCdRomsResource(this, id);
    }
    @Override
    public SnapshotDisksResource getSnapshotDisksResource() {
        return new BackendSnapshotDisksResource(this, id);
    }
    @Override
    public SnapshotNicsResource getSnapshotNicsResource() {
        return new BackendSnapshotNicsResource(this, id);
    }

    public void setCollectionResource(BackendSnapshotsResource collection) {
        this.collection = collection;
    }

    @Override
    protected Snapshot doPopulate(Snapshot model, org.ovirt.engine.core.common.businessentities.Snapshot entity) {
        return model;
    }
}
