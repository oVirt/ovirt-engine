package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Snapshots;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.SnapshotResource;
import org.ovirt.engine.api.resource.SnapshotsResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.types.SnapshotMapper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendSnapshotsResource
        extends AbstractBackendCollectionResource<Snapshot, org.ovirt.engine.core.common.businessentities.Snapshot>
        implements SnapshotsResource {

    private static final Logger log = LoggerFactory.getLogger(BackendSnapshotsResource.class);

    protected Guid parentId;
    private boolean parseVmConfiguration;

    public BackendSnapshotsResource(Guid parentId) {
        super(Snapshot.class, org.ovirt.engine.core.common.businessentities.Snapshot.class);
        this.parentId = parentId;
        parseVmConfiguration = true;
    }

    protected BackendSnapshotsResource(Guid parentId, boolean parseVmConfiguration) {
        this(parentId);
        this.parseVmConfiguration = parseVmConfiguration;
    }

    @Override
    public Snapshots list() {
        return mapCollection(getBackendCollection(QueryType.GetAllVmSnapshotsByVmId,
                new IdQueryParameters(parentId)));
    }

    @Override
    public Response add(Snapshot snapshot) {
        return doAdd(snapshot, expectBlocking());
    }

    protected Response doAdd(Snapshot snapshot, boolean block) {
        validateParameters(snapshot, "description");
        CreateSnapshotForVmParameters snapshotParams =
            new CreateSnapshotForVmParameters(parentId, snapshot.getDescription());
        if (snapshot.isSetPersistMemorystate()) {
            snapshotParams.setSaveMemory(snapshot.isPersistMemorystate());
        }
        if (snapshot.isSetDiskAttachments()) {
            Map<Guid, DiskImage> diskImagesMap = mapDisks(snapshot.getDiskAttachments());
            snapshotParams.setDiskIds(new TreeSet<>(diskImagesMap.keySet()));
            snapshotParams.setDiskImagesMap(diskImagesMap);
        }
        return performCreate(ActionType.CreateSnapshotForVm,
                               snapshotParams,
                               new QueryIdResolver<Guid>(QueryType.GetSnapshotBySnapshotId, IdQueryParameters.class),
                               block);
    }

    private Map<Guid, DiskImage> mapDisks(DiskAttachments diskAttachments) {
        Map<Guid, DiskImage> diskImagesMap = null;
        if (diskAttachments.isSetDiskAttachments()) {
            diskImagesMap =
                    diskAttachments.getDiskAttachments().stream()
                            .map(DiskAttachment::getDisk)
                            .filter(Objects::nonNull)
                            .map(disk -> (DiskImage) DiskMapper.map(disk, null))
                            .collect(Collectors.toMap(BaseDisk::getId, Function.identity()));
        }
        return diskImagesMap;
    }

    List<DiskImage> mapDisks(Disks disks) {
        List<DiskImage> diskImages = null;
        if (disks != null && disks.isSetDisks()) {
            diskImages = disks.getDisks().stream()
                    .filter(Objects::nonNull)
                    .map(disk -> (DiskImage) DiskMapper.map(disk, null))
                    .collect(Collectors.toList());
        }
        return diskImages;
    }

    @Override
    public SnapshotResource getSnapshotResource(String id) {
        return inject(new BackendSnapshotResource(id, parentId, this));
    }

    protected Snapshots mapCollection(List<org.ovirt.engine.core.common.businessentities.Snapshot> entities) {
        Snapshots snapshots = new Snapshots();
        for (org.ovirt.engine.core.common.businessentities.Snapshot entity : entities) {
            Snapshot snapshot = map(entity, null);
            snapshot = populate(snapshot, entity);
            snapshot = addLinks(snapshot);
            if (parseVmConfiguration) {
                try {
                    snapshot = addVmConfiguration(entity, snapshot);
                } catch (WebFaultException wfe) {
                    // Avoid adding the snapshot to the response if the VM configuration is missing.
                    // This scenario might be caused by initiating a snapshot deletion request
                    // right before listing the snapshots. See: https://bugzilla.redhat.com/1530603
                    if (Response.Status.NOT_FOUND.getStatusCode() == wfe.getResponse().getStatus()) {
                        log.warn("Missing VM configuration for snapshot \"{}\". " +
                                "Excluding the snapshot from response.", snapshot.getDescription());
                        continue;
                    }
                    throw wfe;
                }
            }
            snapshots.getSnapshots().add(snapshot);
        }
        return snapshots;
    }

    protected Snapshot addVmConfiguration(org.ovirt.engine.core.common.businessentities.Snapshot entity, Snapshot snapshot) {
        if (entity.isVmConfigurationAvailable()) {
            snapshot.setVm(new Vm());
            getMapper(org.ovirt.engine.core.common.businessentities.VM.class, Vm.class).map(getVmPreview(snapshot), snapshot.getVm());
        } else {
            snapshot.setVm(null);
            snapshot.getLinks().clear();
        }
        return snapshot;
    }

    protected org.ovirt.engine.core.common.businessentities.VM getVmPreview(Snapshot snapshot) {
        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class, QueryType.GetVmConfigurationBySnapshot, new IdQueryParameters(asGuid(snapshot.getId())), null);
        return vm;
    }

    protected org.ovirt.engine.core.common.businessentities.Snapshot getSnapshotById(Guid id) {
        return getEntity(org.ovirt.engine.core.common.businessentities.Snapshot.class,
                QueryType.GetSnapshotBySnapshotId,
                new IdQueryParameters(id),
                String.format("GetSnapshotBySnapshotId: snapshot id=%s", id));
    }

    @Override
    protected Snapshot addParents(Snapshot snapshot) {
        snapshot.setVm(new Vm());
        snapshot.getVm().setId(parentId.toString());
        return snapshot;
    }

    @Override
    protected Snapshot doPopulate(Snapshot model, org.ovirt.engine.core.common.businessentities.Snapshot entity) {
        return populateSnapshotConfiguration(model);
    }

    private Snapshot populateSnapshotConfiguration (Snapshot model) {
        QueryReturnValue queryReturnValue =
                runQuery(QueryType.GetSnapshotBySnapshotId,
                        new IdQueryParameters(Guid.createGuidFromString(model.getId())));

        if (queryReturnValue.getSucceeded() && queryReturnValue.getReturnValue() != null) {
            org.ovirt.engine.core.common.businessentities.Snapshot snapshot = queryReturnValue.getReturnValue();
            if (snapshot.getVmConfiguration() != null) {
                return SnapshotMapper.map(snapshot.getVmConfiguration(),
                        ConfigurationType.OVF,
                        model);
            }
        }

        return model;
    }
}
