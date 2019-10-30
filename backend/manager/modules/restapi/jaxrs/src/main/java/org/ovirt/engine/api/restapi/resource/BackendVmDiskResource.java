/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.VmDiskResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.action.UpdateDiskParameters;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.VmDeviceIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmDiskResource
        extends AbstractBackendActionableResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements VmDiskResource {

    private Guid vmId;

    protected BackendVmDiskResource(String diskId, Guid vmId) {
        super(diskId, Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.vmId = vmId;
        this.subCollections = BackendVmDisksResource.SUB_COLLECTIONS;
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        DiskStatisticalQuery query = new DiskStatisticalQuery(new DiskResolver(), newModel(id));
        return inject(new BackendStatisticsResource<>(entityType, guid, query));
    }

    @Override
    protected Disk deprecatedPopulate(Disk model, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        if (details.contains("statistics")) {
            addStatistics(model, entity);
        }
        return model;
    }

    private void addStatistics(Disk model, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
        model.setStatistics(new Statistics());
        DiskStatisticalQuery query = new DiskStatisticalQuery(newModel(model.getId()));
        List<Statistic> statistics = query.getStatistics(entity);
        for (Statistic statistic : statistics) {
            LinkHelper.addLinks(statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }

    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }

    @Override
    public Response activate(Action action) {
        VmDiskOperationParameterBase params = new VmDiskOperationParameterBase(new DiskVmElement(guid, vmId));
        return doAction(ActionType.HotPlugDiskToVm, params, action);
    }

    @Override
    public Response deactivate(Action action) {
        VmDiskOperationParameterBase params = new VmDiskOperationParameterBase(new DiskVmElement(guid, vmId));
        return doAction(ActionType.HotUnPlugDiskFromVm, params, action);
    }

    @Override
    public Response move(Action action) {
        validateParameters(action, "storageDomain.id|name");
        Guid storageDomainId = getStorageDomainId(action);
        Disk disk = getDisk();
        Guid sourceStorageDomainId = getSourceStorageDomainId(disk);
        Guid imageId = getDiskImageId(disk.getImageId());
        MoveDiskParameters innerParams = new MoveDiskParameters(
                imageId,
                sourceStorageDomainId,
                storageDomainId);
        innerParams.setImageGroupID(asGuid(disk.getId()));
        MoveDisksParameters params =
                new MoveDisksParameters(Collections.singletonList(innerParams));
        return doAction(ActionType.MoveDisk, params, action);
    }

    protected Disk getDisk() {
        return performGet(QueryType.GetDiskByDiskId, new IdQueryParameters(guid));
    }

    protected Guid getSourceStorageDomainId(Disk disk) {
        if (disk.isSetStorageDomains()) {
            StorageDomain storageDomain = disk.getStorageDomains().getStorageDomains().get(0);
            if (storageDomain != null) {
                return asGuid(storageDomain.getId());
            }
        }
        return null;
    }

    protected Guid getDiskImageId(String id) {
        if (id == null) {
            return null;
        }
        return asGuid(id);
    }

    @Override
    public Disk get() {
        return performGet(QueryType.GetDiskByDiskId, new IdQueryParameters(guid));
    }

    @Override
    protected Disk addLinks(Disk model, String... subCollectionMembersToExclude) {
        Snapshot snapshotInfo = model.getSnapshot();
        model.setSnapshot(null);
        super.addLinks(model, subCollectionMembersToExclude);
        if (snapshotInfo != null) {
            org.ovirt.engine.core.common.businessentities.Snapshot snapshot =
                    getEntity(org.ovirt.engine.core.common.businessentities.Snapshot.class,
                            QueryType.GetSnapshotBySnapshotId,
                            new IdQueryParameters(asGuid(snapshotInfo.getId())),
                            snapshotInfo.getId());
            Vm vm = new Vm();
            vm.setId(snapshot.getVmId().toString());
            snapshotInfo.setVm(vm);
            model.setSnapshot(snapshotInfo);
            LinkHelper.addLinks(snapshotInfo, null, false);
            model.setSnapshot(snapshotInfo);
        }

        return model;
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
            QueryType.GetPermissionsForObject,
            new GetPermissionsForObjectParameters(guid),
            Disk.class,
            VdcObjectType.Disk));
    }

    @Override
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");
        return doAction(ActionType.ExportRepoImage,
            new ExportRepoImageParameters(guid, getStorageDomainId(action)), action);
    }

    @Override
    public Response reduce(Action action) {
        Disk disk = get();
        Guid imageId = getDiskImageId(disk.getImageId());
        ImagesActionsParametersBase params = new ImagesActionsParametersBase(imageId);
        return doAction(ActionType.ReduceImage, params, action);
    }

    @Override
    public Disk update(Disk disk) {
        return performUpdate(disk, new DiskResolver(), ActionType.UpdateDisk, new UpdateParametersProvider());
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.DetachDiskFromVm, new AttachDetachVmDiskParameters(new DiskVmElement(guid, vmId)));
    }

    @Override
    protected Disk addParents(Disk disk) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        disk.setVm(vm);
        return disk;
    }

    @Override
    public CreationResource getCreationResource(String oid) {
        return inject(new BackendCreationResource(oid));
    }

    private class DiskResolver extends EntityIdResolver<Guid> {
        @Override
        public org.ovirt.engine.core.common.businessentities.storage.Disk lookupEntity(Guid id) throws BackendFailureException {
            return getEntity(
                org.ovirt.engine.core.common.businessentities.storage.Disk.class,
                QueryType.GetDiskByDiskId,
                new IdQueryParameters(id),
                id.toString(),
                false
            );
        }
    }

    protected class UpdateParametersProvider implements ParametersProvider<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk> {
        @Override
        public UpdateDiskParameters getParameters(Disk incoming, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
            DiskVmElement dveFromDb = runQuery(QueryType.GetDiskVmElementById,
                    new VmDeviceIdQueryParameters(new VmDeviceId(entity.getId(), vmId))).getReturnValue();

            DiskVmElement updatedDve = updateDiskVmElementFromDisk(incoming, dveFromDb);

            return new UpdateDiskParameters(updatedDve, map(incoming, entity));
        }
    }

    private DiskVmElement updateDiskVmElementFromDisk(Disk disk, DiskVmElement diskVmElement) {
        if (disk.isSetInterface()) {
            diskVmElement.setDiskInterface(DiskMapper.mapInterface(disk.getInterface()));
        }

        if(disk.isSetBootable()) {
            diskVmElement.setBoot(disk.isBootable());
        }

        if (disk.isSetReadOnly()) {
            diskVmElement.setReadOnly(disk.isReadOnly());
        }

        return diskVmElement;
    }
}
