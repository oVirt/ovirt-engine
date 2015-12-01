package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDiskResource extends AbstractBackendActionableResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements DiskResource {

    protected BackendDiskResource(String id) {
        super(id, Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        QueryIdResolver<Guid> resolver = new QueryIdResolver<>(VdcQueryType.GetDiskByDiskId, IdQueryParameters.class);
        DiskStatisticalQuery query = new DiskStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<>(entityType, guid, query));
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
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");
        return doAction(VdcActionType.ExportRepoImage,
                new ExportRepoImageParameters(guid, getStorageDomainId(action)), action);
    }

    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }

    @Override
    public Response move(Action action) {
        validateParameters(action, "storageDomain.id|name");
        Guid storageDomainId = getStorageDomainId(action);
        Disk disk = get();
        Guid imageId = getDiskImageId(disk.getImageId());
        Guid sourceStorageDomainId = getSourceStorageDomainId(disk);
        MoveDiskParameters innerParams = new MoveDiskParameters(
                imageId,
                sourceStorageDomainId,
                storageDomainId);
        innerParams.setImageGroupID(asGuid(disk.getId()));
        MoveDisksParameters params =
                new MoveDisksParameters(Collections.singletonList(innerParams));
        return doAction(VdcActionType.MoveDisks, params, action);
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
    public Response copy(Action action) {
        validateParameters(action, "storageDomain.id|name");
        Guid storageDomainId = getStorageDomainId(action);
        Disk disk = get();
        Guid imageId = getDiskImageId(disk.getImageId());
        Guid sourceStorageDomainId = getSourceStorageDomainId(disk);
        MoveOrCopyImageGroupParameters params =
                new MoveOrCopyImageGroupParameters(imageId,
                        sourceStorageDomainId,
                        storageDomainId,
                        ImageOperation.Copy);

        params.setImageGroupID(asGuid(disk.getId()));

        Disk actionDisk = action.getDisk();
        if (actionDisk != null) {
            String name = actionDisk.getName();
            String alias = actionDisk.getAlias();
            if (name != null && !StringUtils.isEmpty(name)) {
                params.setNewAlias(name);
            } else if (alias != null && !StringUtils.isEmpty(alias)) {
                params.setNewAlias(alias);
            }
        }

        return doAction(VdcActionType.MoveOrCopyDisk, params, action);
    }

    @Override
    public Disk get() {
        return performGet(VdcQueryType.GetDiskByDiskId, new IdQueryParameters(guid));
    }

    @Override
    protected Disk doPopulate(Disk model, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
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
            Vms modelVms = new Vms();
            for (org.ovirt.engine.core.common.businessentities.VM vm : vms) {
                Vm modelVm = new Vm();
                modelVm.setId(vm.getId().toString());
                modelVms.getVms().add(modelVm);
            }
            model.setVms(modelVms);
        }
        return model;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveDisk, new RemoveDiskParameters(guid));
    }
}
