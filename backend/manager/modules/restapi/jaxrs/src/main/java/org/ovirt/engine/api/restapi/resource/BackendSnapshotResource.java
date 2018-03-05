package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.SnapshotCdromsResource;
import org.ovirt.engine.api.resource.SnapshotDisksResource;
import org.ovirt.engine.api.resource.SnapshotNicsResource;
import org.ovirt.engine.api.resource.SnapshotResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetDiskImageByDiskAndImageIdsParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendSnapshotResource
        extends AbstractBackendActionableResource<Snapshot, org.ovirt.engine.core.common.businessentities.Snapshot>
        implements SnapshotResource {

    private static final String RESTORE_SNAPSHOT_CORRELATION_ID = "RestoreSnapshot";
    protected Guid parentId;
    protected BackendSnapshotsResource collection;

    public BackendSnapshotResource(String id, Guid parentId, BackendSnapshotsResource collection) {
        super(id, Snapshot.class, org.ovirt.engine.core.common.businessentities.Snapshot.class);
        this.parentId = parentId;
        this.collection = collection;
    }

    @Override
    public Snapshot get() {
        org.ovirt.engine.core.common.businessentities.Snapshot entity = getSnapshot();
        Snapshot snapshot = populate(map(entity, null), entity);
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
        if (action.isSetRestoreMemory()) {
            tryBackParams.setRestoreMemory(action.isRestoreMemory());
        }
        if (action.isSetDisks()) {
            // Each disk parameter is being mapped to a DiskImage.
            List<DiskImage> disks = collection.mapDisks(action.getDisks());
            List<DiskImage> disksFromDB = null;

            if (disks != null) {
                // In case a disk hasn't specified its image_id, the imageId value is set to Guid.Empty().
                disksFromDB = disks.stream()
                        .map(disk -> getEntity(org.ovirt.engine.core.common.businessentities.storage.DiskImage.class,
                                QueryType.GetDiskImageByDiskAndImageIds,
                                new GetDiskImageByDiskAndImageIdsParameters(disk.getId(), disk.getImageId()),
                                String.format("GetDiskImageByDiskAndImageIds: disk id=%s, image_id=%s",
                                        disk.getId(), disk.getImageId())))
                        .collect(Collectors.toList());
            }
            tryBackParams.setDisks(disksFromDB);
        }
        tryBackParams.setCorrelationId(RESTORE_SNAPSHOT_CORRELATION_ID); //TODO: if user supplied, override with user value
        Response response = doAction(ActionType.TryBackToAllSnapshotsOfVm,
                tryBackParams,
                action,
                PollingType.JOB);
        if (response.getStatus()==Response.Status.OK.getStatusCode()) {
            RestoreAllSnapshotsParameters restoreParams = new RestoreAllSnapshotsParameters(parentId, SnapshotActionEnum.COMMIT);
            restoreParams.setCorrelationId(RESTORE_SNAPSHOT_CORRELATION_ID);
            Response response2 = doAction(ActionType.RestoreAllSnapshots,
                    restoreParams,
                    action);
            if (response2.getStatus()!=Response.Status.OK.getStatusCode()) {
                return response2;
            }
        }
        return response;
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public ActionResource getActionResource(String action, String ids) {
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
    public SnapshotCdromsResource getCdromsResource() {
        return inject(new BackendSnapshotCdRomsResource(this));
    }
    @Override
    public SnapshotDisksResource getDisksResource() {
        return inject(new BackendSnapshotDisksResource(this));
    }

    @Override
    public SnapshotNicsResource getNicsResource() {
        return inject(new BackendSnapshotNicsResource(this));
    }

    public void setCollectionResource(BackendSnapshotsResource collection) {
        this.collection = collection;
    }

    @Override
    protected Snapshot doPopulate(Snapshot model, org.ovirt.engine.core.common.businessentities.Snapshot entity) {
        return collection.doPopulate(model, entity);
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveSnapshot, new RemoveSnapshotParameters(guid, parentId));
    }
}
