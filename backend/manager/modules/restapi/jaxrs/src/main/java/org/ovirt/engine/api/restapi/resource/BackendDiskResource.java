package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VMs;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.MovableCopyableDiskResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDiskResource extends AbstractBackendActionableResource<Disk, org.ovirt.engine.core.common.businessentities.Disk>
        implements MovableCopyableDiskResource {

    protected BackendDiskResource(String id) {
        super(id, Disk.class, org.ovirt.engine.core.common.businessentities.Disk.class);
    }

    @Override
    public CreationResource getCreationSubresource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        QueryIdResolver<Guid> resolver = new QueryIdResolver<Guid>(VdcQueryType.GetDiskByDiskId, IdQueryParameters.class);
        DiskStatisticalQuery query = new DiskStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<Disk, org.ovirt.engine.core.common.businessentities.Disk>(entityType, guid, query));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             Disk.class,
                                                             VdcObjectType.Disk));
    }

    @Override
    public Response doExport(Action action) {
        validateParameters(action, "storageDomain.id|name");
        return doAction(VdcActionType.ExportRepoImage,
                new ExportRepoImageParameters(guid, getStorageDomainId(action)), action);
    }

    @Override
    public ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return inject(new BackendActionResource(action, oid));
    }

    @Override
    public Response move(Action action) {
        validateParameters(action, "storageDomain.id|name");
        Guid storageDomainId = getStorageDomainId(action);
        Disk disk = get();
        Guid imageId = asGuid(disk.getImageId());
        Guid sourceStorageDomainId = getSourceStorageDomainId(disk);
        MoveDisksParameters params =
                new MoveDisksParameters(Collections.singletonList(new MoveDiskParameters(
                        imageId,
                        sourceStorageDomainId,
                        storageDomainId)));
        return doAction(VdcActionType.MoveDisks, params, action);
    }

    protected Guid getSourceStorageDomainId(Disk disk) {
        return asGuid(disk.getStorageDomains().getStorageDomains().get(0).getId());
    }

    @Override
    public Response copy(Action action) {
        validateParameters(action, "storageDomain.id|name");
        Guid storageDomainId = getStorageDomainId(action);
        Disk disk = get();
        Guid imageId = asGuid(disk.getImageId());
        Guid sourceStorageDomainId = getSourceStorageDomainId(disk);
        MoveOrCopyImageGroupParameters params =
                new MoveOrCopyImageGroupParameters(imageId,
                        sourceStorageDomainId,
                        storageDomainId,
                        ImageOperation.Copy);
        return doAction(VdcActionType.MoveOrCopyDisk, params, action);
    }

    @Override
    public Disk get() {
        return performGet(VdcQueryType.GetDiskByDiskId, new IdQueryParameters(guid));
    }

    @Override
    protected Disk doPopulate(Disk model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        // Populate the references to the VMs that are using this disk:
        List<org.ovirt.engine.core.common.businessentities.VM> vms = new ArrayList<>(1);
        VdcQueryReturnValue result = runQuery(VdcQueryType.GetVmsByDiskGuid, new IdQueryParameters(entity.getId()));
        if (result.getSucceeded()) {
            Map<Boolean, List<org.ovirt.engine.core.common.businessentities.VM>> map = result.getReturnValue();
            if (MapUtils.isNotEmpty(map)) {
                for (List<org.ovirt.engine.core.common.businessentities.VM> list : map.values()) {
                    vms.addAll(list);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(vms)) {
            VMs modelVms = new VMs();
            for (org.ovirt.engine.core.common.businessentities.VM vm : vms) {
                VM modelVm = new VM();
                modelVm.setId(vm.getId().toString());
                modelVms.getVMs().add(modelVm);
            }
            model.setVms(modelVms);
        }
        return model;
    }
}
