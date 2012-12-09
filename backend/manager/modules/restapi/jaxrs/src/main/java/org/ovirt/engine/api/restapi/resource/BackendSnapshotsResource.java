package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Snapshots;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.resource.SnapshotResource;
import org.ovirt.engine.api.resource.SnapshotsResource;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmConfigurationBySnapshotQueryParams;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendSnapshotsResource
        extends AbstractBackendCollectionResource<Snapshot, org.ovirt.engine.core.common.businessentities.Snapshot>
        implements SnapshotsResource {

    static final String[] SUB_COLLECTIONS = { "cdroms", "disks", "nics" };
    protected Guid parentId;

    public BackendSnapshotsResource(Guid parentId) {
        super(Snapshot.class, org.ovirt.engine.core.common.businessentities.Snapshot.class, SUB_COLLECTIONS);
        this.parentId = parentId;
    }

    @Override
    public Snapshots list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetAllVmSnapshotsByVmId,
                new GetAllVmSnapshotsByVmIdParameters(parentId)));
    }

    @Override
    public Response add(Snapshot snapshot) {
        return doAdd(snapshot, expectBlocking());
    }

    protected Response doAdd(Snapshot snapshot, boolean block) {
        validateParameters(snapshot, "description");
        CreateAllSnapshotsFromVmParameters snapshotParams =
            new CreateAllSnapshotsFromVmParameters(parentId, snapshot.getDescription());
        return performCreate(VdcActionType.CreateAllSnapshotsFromVm,
                               snapshotParams,
                               new SnapshotIdResolver(),
                               block);
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveSnapshot,
                new RemoveSnapshotParameters(asGuid(id), parentId));
    }

    @Override
    @SingleEntityResource
    public SnapshotResource getSnapshotSubResource(String id) {
        return inject(new BackendSnapshotResource(id, parentId, this));
    }

    protected Snapshots mapCollection(List<org.ovirt.engine.core.common.businessentities.Snapshot> entities) {
        Snapshots snapshots = new Snapshots();
        for (org.ovirt.engine.core.common.businessentities.Snapshot entity : entities) {
            Snapshot snapshot = map(entity, null);
            snapshot = addLinks(snapshot);
            snapshot = addVmConfiguration(entity, snapshot);
            snapshots.getSnapshots().add(snapshot);
        }
        return snapshots;
    }

    protected Snapshot addVmConfiguration(org.ovirt.engine.core.common.businessentities.Snapshot entity, Snapshot snapshot) {
        if (entity.isVmConfigurationAvailable()) {
            snapshot.setVm(new VM());
            getMapper(org.ovirt.engine.core.common.businessentities.VM.class, VM.class).map(getVmPreview(snapshot), snapshot.getVm());
            snapshot.getVm().getLinks().addAll(snapshot.getLinks());
        }
        else {
            snapshot.setVm(null);
        }
        snapshot.getLinks().clear();
        return snapshot;
    }

    protected org.ovirt.engine.core.common.businessentities.VM getVmPreview(Snapshot snapshot) {
        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class, VdcQueryType.GetVmConfigurationBySnapshot, new GetVmConfigurationBySnapshotQueryParams(asGuid(snapshot.getId())), null);
        return vm;
    }

    protected org.ovirt.engine.core.common.businessentities.Snapshot getSnapshotById(Guid id) {
        //TODO: move to 'GetSnapshotBySnapshotId' once Backend supplies it.
        for (org.ovirt.engine.core.common.businessentities.Snapshot snapshot : getBackendCollection(VdcQueryType.GetAllVmSnapshotsByVmId,
                new GetAllVmSnapshotsByVmIdParameters(parentId))) {
            if (snapshot.getId().equals(id)) {
                return snapshot;
            }
        }
        return null;
    }

    @Override
    protected Snapshot addParents(Snapshot snapshot) {
        snapshot.setVm(new VM());
        snapshot.getVm().setId(parentId.toString());
        return snapshot;
    }

    protected class SnapshotIdResolver extends EntityIdResolver<Guid> {

        SnapshotIdResolver() {}

        @Override
        public org.ovirt.engine.core.common.businessentities.Snapshot lookupEntity(
                Guid id) throws BackendFailureException {
            return getSnapshotById(id);
        }
    }

    @Override
    protected Snapshot doPopulate(Snapshot model, org.ovirt.engine.core.common.businessentities.Snapshot entity) {
        return model;
    }
}
