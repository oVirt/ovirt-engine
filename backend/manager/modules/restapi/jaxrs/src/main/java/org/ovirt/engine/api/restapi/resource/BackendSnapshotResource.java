package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.SnapshotResource;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class BackendSnapshotResource extends AbstractBackendActionableResource<Snapshot, DiskImage> implements SnapshotResource {

    protected Guid parentId;
    protected BackendSnapshotsResource collection;

    public BackendSnapshotResource(String id, Guid parentId, BackendSnapshotsResource collection) {
        super(id, Snapshot.class, DiskImage.class);
        this.parentId = parentId;
        this.collection = collection;
    }

    @Override
    public Snapshot get() {
        for (DiskImage diskImage : collection.getDisks()) {
            for (DiskImage snapshotImage : diskImage.getSnapshots()) {
                if (id.equals(snapshotImage.getvm_snapshot_id().toString())) {
                   return addLinks(collection.map(snapshotImage, diskImage));
                }
            }
        }
        return notFound();
    }

    @Override
    public Response restore(Action action) {
        Snapshot snapshot = new Snapshot();
        snapshot.setDescription(get().getDescription());
        action.setAsync(false);
        Response response = doAction(VdcActionType.RestoreAllSnapshots,
                new RestoreAllSnapshotsParameters(parentId, guid),
                action);
        if (response.getStatus()==Response.Status.OK.getStatusCode()) {
            Response response2 = collection.doAdd(snapshot, true);
            //if creation failed, return the response to show the failure.
            //otherwise return the response of the previous request, the
            //restore request.
            if (response2.getStatus()!=Response.Status.CREATED.getStatusCode()) {
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
}
