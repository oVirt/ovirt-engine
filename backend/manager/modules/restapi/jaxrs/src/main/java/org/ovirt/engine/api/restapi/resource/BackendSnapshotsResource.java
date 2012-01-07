package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.ovirt.engine.api.common.util.LinkHelper;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Snapshots;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.resource.SnapshotResource;
import org.ovirt.engine.api.resource.SnapshotsResource;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.MergeSnapshotParamenters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class BackendSnapshotsResource
        extends AbstractBackendCollectionResource<Snapshot, DiskImage>
        implements SnapshotsResource {

    protected Guid parentId;

    public BackendSnapshotsResource(Guid parentId) {
        super(Snapshot.class, DiskImage.class);
        this.parentId = parentId;
    }

    @Override
    public Snapshots list() {
        return mapCollection(getDisks());
    }

    @Override
    public Response add(Snapshot snapshot) {
        validateParameters(snapshot, "description");
        CreateAllSnapshotsFromVmParameters snapshotParams =
            new CreateAllSnapshotsFromVmParameters(parentId, snapshot.getDescription());
        snapshotParams.setDisksList(getDrives());

        return performCreation(VdcActionType.CreateAllSnapshotsFromVm,
                               snapshotParams,
                               getEntityIdResolver(snapshot.getDescription()));
    }

    @Override
    public Response performRemove(String id) {
        for (DiskImage diskImage : getDisks()) {
            Map<NGuid, NGuid> parents = getParentage(diskImage);
            for (DiskImage snapshotImage : diskImage.getSnapshots()) {
                Guid sourceVmSnapshotId = new Guid(snapshotImage.getvm_snapshot_id().getUuid());
                if (id.equals(sourceVmSnapshotId.toString())) {
                    NGuid dest = findSnapshotParent(sourceVmSnapshotId, parents);
                    if (dest != null) {
                        return performAction(VdcActionType.MergeSnapshot,
                                             new MergeSnapshotParamenters(sourceVmSnapshotId,
                                                                          dest,
                                                                          parentId));
                    }
                    break;
                }
            }
        }
        notFound();
        return null;
    }

    private NGuid findSnapshotParent(Guid snapshotId, Map<NGuid, NGuid> parents) {
        for (NGuid parentId : parents.keySet()) {
            if (parents.get(parentId).equals(snapshotId)) {
                return parentId;
            }
        }
        return null;
     }

    @Override
    @SingleEntityResource
    public SnapshotResource getSnapshotSubResource(String id) {
        return inject(new BackendSnapshotResource(id, parentId, this));
    }

    public Snapshot addParents(Snapshot model) {
        model.setVm(new VM());
        model.getVm().setId(parentId.toString());
        return model;
    }

    protected ArrayList<String> getDrives() {
        List<DiskImage> disks = getDisks();
        ArrayList<String> drives = new ArrayList<String>();
        for (DiskImage disk : disks) {
            drives.add(disk.getinternal_drive_mapping());
        }
        return drives;
    }

    protected List<DiskImage> getDisks() {
        return getBackendCollection(VdcQueryType.GetAllDisksByVmId,
                                    new GetAllDisksByVmIdParameters(parentId));
    }

    protected Snapshots mapCollection(List<DiskImage> diskImages) {
        Map<String, Snapshot> snapshots = new LinkedHashMap<String, Snapshot>();
        for (DiskImage diskImage : diskImages) {
            Map<NGuid, NGuid> parents = getParentage(diskImage);
            for (DiskImage snapshotImage : diskImage.getSnapshots()) {
                Snapshot candidate = map(snapshotImage, diskImage);
                if (!snapshots.containsKey(candidate.getId())) {
                    snapshots.put(candidate.getId(), addLinks(candidate));
                    addPrevLink(candidate, parents.get(snapshotImage.getvm_snapshot_id()));
                }
            }
        }

        Snapshots collection = new Snapshots();
        collection.getSnapshots().addAll(snapshots.values());
        return collection;
    }

    protected Snapshot map(DiskImage snapshot, DiskImage diskImage) {
        Snapshot template = null;
        if (diskImage != null) {
            template =  new Snapshot();
            Disk disk = new Disk();
            disk.setId(diskImage.getId().toString());
            VM vm = new VM();
            vm.setId(parentId.toString());
            disk.setVm(vm);
            template.setDisk(disk);
        }
        return super.map(snapshot, template);
    }

    protected Map<NGuid, NGuid> getParentage(DiskImage diskImage) {
        Map<Guid, NGuid> images = new HashMap<Guid, NGuid>();
        for (DiskImage snapshotImage : diskImage.getSnapshots()) {
            images.put(snapshotImage.getId(),
                       snapshotImage.getvm_snapshot_id());
        }

        Map<NGuid, NGuid> parents = new HashMap<NGuid, NGuid>();
        for (DiskImage snapshotImage : diskImage.getSnapshots()) {
            if (!(Guid.Empty.equals(snapshotImage.getParentId()))
                && images.containsKey(snapshotImage.getParentId())) {
                parents.put(snapshotImage.getvm_snapshot_id(),
                            images.get(snapshotImage.getParentId()));
            }
        }
        return parents;
    }

    protected void addPrevLink(Snapshot snapshot, NGuid id) {
        if (id != null) {
            UriBuilder uriBuilder = LinkHelper.getUriBuilder(getUriInfo(), snapshot.getVm()).path("snapshots");
            Link prev = new Link();
            prev.setRel("prev");
            prev.setHref(uriBuilder.clone().path(id.toString()).build().toString());
            snapshot.getLinks().add(prev);
        }
    }

    public DiskImage lookupEntityByDescription(String description) {
        for (DiskImage diskImage : getDisks()) {
            for (DiskImage snapshotImage : diskImage.getSnapshots()) {
                if (description.equals(snapshotImage.getdescription())) {
                    return snapshotImage;
                }
            }
        }
        return null;
    }

    public EntityIdResolver getEntityIdResolver(String description) {
        return new DiskImageIdResolver(description);
    }

    protected class DiskImageIdResolver extends EntityIdResolver {

        private String description;

        DiskImageIdResolver() {}

        DiskImageIdResolver(String description) {
            this.description = description;
        }

        @Override
        public DiskImage lookupEntity(Guid id) {
            return lookupEntityByDescription(description);
        }
    }
}
