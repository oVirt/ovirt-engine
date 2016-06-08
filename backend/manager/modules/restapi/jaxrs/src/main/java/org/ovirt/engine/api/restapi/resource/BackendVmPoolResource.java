package org.ovirt.engine.api.restapi.resource;


import static org.ovirt.engine.api.restapi.resource.BackendVmPoolsResource.SUB_COLLECTION;

import java.util.List;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.VmPoolResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmPoolResource
        extends AbstractBackendActionableResource<VmPool, org.ovirt.engine.core.common.businessentities.VmPool>
    implements VmPoolResource {

    private BackendVmPoolsResource parent;

    public BackendVmPoolResource(String id, BackendVmPoolsResource parent) {
        super(id, VmPool.class, org.ovirt.engine.core.common.businessentities.VmPool.class, SUB_COLLECTION);
        this.parent = parent;
    }

    @Override
    public VmPool get() {
        return performGet(VdcQueryType.GetVmPoolById, new IdQueryParameters(guid));
    }

    @Override
    public VmPool update(VmPool incoming) {
        return performUpdate(incoming,
                             new QueryIdResolver<>(VdcQueryType.GetVmPoolById, IdQueryParameters.class),
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
    protected VmPool deprecatedPopulate(VmPool pool, org.ovirt.engine.core.common.businessentities.VmPool entity) {
        return parent.deprecatedPopulate(pool, entity);
    }

    @Override
    protected VmPool doPopulate(VmPool pool, org.ovirt.engine.core.common.businessentities.VmPool entity) {
        return parent.doPopulate(pool, entity);
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
                                                            new IdQueryParameters(templateId),
                                                            templateId.toString()));
            if (images != null && images.size() > 0) {
                storageDomainId = images.get(0).getStorageIds().get(0);
            }
        }
        return storageDomainId;
    }

    protected class UpdateParametersProvider implements ParametersProvider<VmPool, org.ovirt.engine.core.common.businessentities.VmPool> {
        @Override
        public VdcActionParametersBase getParameters(VmPool incoming,
                org.ovirt.engine.core.common.businessentities.VmPool current) {
            final int currentVmCount = current.getAssignedVmsCount();

            int size = 0;
            if (incoming.isSetSize()) {
                // in case the value is negative, the backend command will fail on validate
                size = incoming.getSize() - currentVmCount;
            }

            final org.ovirt.engine.core.common.businessentities.VmPool entity = map(incoming, current);
            final VM vm = mapToVM(map(entity));

            if (incoming.isSetTemplate()) {
                vm.setVmtGuid(getTempalteId(incoming.getTemplate()));
            } else {
                final VM existing = currentVmCount > 0
                                ? getEntity(VM.class,
                                        VdcQueryType.GetVmDataByPoolId,
                                        new IdQueryParameters(current.getId()),
                                        "Vms: pool=" + current.getId())
                              : null;
                if (existing != null) {
                    vm.setVmtGuid(existing.getVmtGuid());
                    vm.setVmInit(existing.getVmInit());
                }
            }

            if (vm.getVmtGuid() != null) {
                final VmTemplate template = getEntity(VmTemplate.class,
                                                VdcQueryType.GetVmTemplate,
                                                new GetVmTemplateParameters(vm.getVmtGuid()),
                                                vm.getVmtGuid().toString());
                vm.getStaticData().setMemSizeMb(template.getMemSizeMb());
                vm.getStaticData().setSingleQxlPci(template.getSingleQxlPci());
                vm.getStaticData().setOsId(template.getOsId());
                vm.getStaticData().setDefaultDisplayType(template.getDefaultDisplayType());
                vm.getStaticData().setMigrationSupport(template.getMigrationSupport());
                if (vm.getVmInit() == null) {
                    vm.setVmInit(template.getVmInit());
                }
            }
            if (incoming.isSetUseLatestTemplateVersion()) {
                vm.setUseLatestVersion(incoming.isUseLatestTemplateVersion());
            }

            final AddVmPoolWithVmsParameters parameters = new AddVmPoolWithVmsParameters(entity, vm, size, -1);
            parameters.setStorageDomainId(getStorageDomainId(vm.getVmtGuid()));
            return parameters;
        }
    }

    private Guid getTempalteId(Template template) {
        Guid result = null;
        if (template.isSetId()) {
            result = new Guid(template.getId());
        } else if (template.isSetName()) {
            result = lookupTemplateByName(template.getName()).getId();
        }
        return result;
    }

    private VmTemplate lookupTemplateByName(String name) {
        return getEntity(VmTemplate.class,
                VdcQueryType.GetVmTemplate,
                new GetVmTemplateParameters(name),
                "GetVmTemplate");
    }

    @Override
    public Response allocateVm(Action action) {
        return doAction(VdcActionType.AttachUserToVmFromPoolAndRun,
                        new VmPoolUserParameters(guid,  getCurrent().getUser().getId(), false),
                        action,
                        new VmQueryIdResolver(VdcQueryType.GetVmByVmId,
                                              IdQueryParameters.class));

    }

    protected class VmQueryIdResolver extends EntityResolver {

        private VdcQueryType query;
        private Class<? extends VdcQueryParametersBase> queryParamsClass;

        public VmQueryIdResolver(VdcQueryType query, Class<? extends VdcQueryParametersBase> queryParamsClass) {
            this.query = query;
            this.queryParamsClass = queryParamsClass;
        }

        @Override
        public Object lookupEntity(Object id) throws BackendFailureException {
            VM vm = doGetEntity(VM.class,
                    query, getQueryParams(queryParamsClass, id), id.toString());
            org.ovirt.engine.api.model.Vm model = new org.ovirt.engine.api.model.Vm();
            model.setId(vm.getId().toString());
            return LinkHelper.addLinks(model);
        }
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public ActionResource getActionResource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    public BackendVmPoolsResource getParent() {
        return parent;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveVmPool, new VmPoolParametersBase(asGuid(id)));
    }
}
