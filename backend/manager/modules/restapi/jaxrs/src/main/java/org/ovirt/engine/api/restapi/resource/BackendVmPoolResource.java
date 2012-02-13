package org.ovirt.engine.api.restapi.resource;


import java.util.List;

import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.VmPoolResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmPoolByIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendVmPoolsResource.SUB_COLLECTION;

public class BackendVmPoolResource
    extends AbstractBackendSubResource<VmPool, vm_pools>
    implements VmPoolResource {

    private BackendVmPoolsResource parent;

    public BackendVmPoolResource(String id, BackendVmPoolsResource parent) {
        super(id, VmPool.class, vm_pools.class, SUB_COLLECTION);
        this.parent = parent;
    }

    @Override
    public VmPool get() {
        return performGet(VdcQueryType.GetVmPoolById, new GetVmPoolByIdParameters(guid));
    }

    @Override
    public VmPool update(VmPool incoming) {
        return performUpdate(incoming,
                             new QueryIdResolver(VdcQueryType.GetVmPoolById,
                                                 GetVmPoolByIdParameters.class),
                             VdcActionType.UpdateVmPoolWithVms,
                             new UpdateParametersProvider());
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             VmPool.class,
                                                             VdcObjectType.VmPool));
    }

    @Override
    protected VmPool populate(VmPool pool, vm_pools entity) {
        return parent.populate(pool, entity);
    }

    protected VM mapToVM(VmPool model) {
        return getMapper(VmPool.class, VM.class).map(model, null);
    }

    protected Guid getStorageDomainId(Guid templateId) {
        Guid storageDomainId = null;
        if (templateId != null) {
            List<DiskImage> images = asCollection(DiskImage.class,
                                                  getEntity(List.class,
                                                            VdcQueryType.GetVmTemplatesDisks,
                                                            new GetVmTemplatesDisksParameters(templateId),
                                                            templateId.toString()));
            if (images != null && images.size() > 0) {
                storageDomainId = images.get(0).getstorage_id().getValue();
            }
        }
        return storageDomainId;
    }

    protected class UpdateParametersProvider implements ParametersProvider<VmPool, vm_pools> {
        @Override
        public VdcActionParametersBase getParameters(VmPool incoming, vm_pools current) {
            int currentVmCount = current.getvm_assigned_count();
            vm_pools entity = map(incoming, current);

            VM vm = mapToVM(map(entity));

            int size = incoming.isSetSize() && incoming.getSize() > currentVmCount
                       ? incoming.getSize() - currentVmCount
                       : 0;

            if (incoming.isSetTemplate()) {
                vm.setvmt_guid(new Guid(incoming.getTemplate().getId()));
            } else {
                VM existing = currentVmCount > 0
                              ? getEntity(VM.class, SearchType.VM, "Vms: pool=" + incoming.getName())
                              : null;
                if (existing != null) {
                    vm.setvmt_guid(existing.getvmt_guid());
                }
            }

            if (vm.getvmt_guid() != null) {
                VmTemplate template = getEntity(VmTemplate.class,
                                                VdcQueryType.GetVmTemplate,
                                                new GetVmTemplateParameters(vm.getId()),
                                                vm.getId().toString());
                vm.getStaticData().setmem_size_mb(template.getmem_size_mb());
            }

            AddVmPoolWithVmsParameters parameters = new AddVmPoolWithVmsParameters(entity, vm, size, -1);
            parameters.setStorageDomainId(getStorageDomainId(vm.getvmt_guid()));
            return parameters;
        }
    }
}
