package org.ovirt.engine.api.restapi.resource;

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
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmPoolParameters;
import org.ovirt.engine.core.common.action.AttachUserToVmFromPoolAndRunParameters;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmPoolResource
        extends AbstractBackendActionableResource<VmPool, org.ovirt.engine.core.common.businessentities.VmPool>
    implements VmPoolResource {

    private BackendVmPoolsResource parent;

    public BackendVmPoolResource(String id, BackendVmPoolsResource parent) {
        super(id, VmPool.class, org.ovirt.engine.core.common.businessentities.VmPool.class);
        this.parent = parent;
    }

    @Override
    public VmPool get() {
        return performGet(QueryType.GetVmPoolById, new IdQueryParameters(guid));
    }

    @Override
    public VmPool update(VmPool incoming) {
        return performUpdate(incoming,
                             new QueryIdResolver<>(QueryType.GetVmPoolById, IdQueryParameters.class),
                             ActionType.UpdateVmPool,
                             new UpdateParametersProvider());
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             QueryType.GetPermissionsForObject,
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
                                                            QueryType.GetVmTemplatesDisks,
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
        public ActionParametersBase getParameters(VmPool incoming,
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
                                        QueryType.GetVmDataByPoolId,
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
                                                QueryType.GetVmTemplate,
                                                new GetVmTemplateParameters(vm.getVmtGuid()),
                                                vm.getVmtGuid().toString());
                vm.getStaticData().setMemSizeMb(template.getMemSizeMb());
                vm.getStaticData().setMaxMemorySizeMb(template.getMaxMemorySizeMb());
                vm.getStaticData().setOsId(template.getOsId());
                vm.getStaticData().setDefaultDisplayType(template.getDefaultDisplayType());
                vm.getStaticData().setMigrationSupport(template.getMigrationSupport());
                vm.getStaticData().setMultiQueuesEnabled(template.isMultiQueuesEnabled());
                vm.getStaticData().setVirtioScsiMultiQueues(template.getVirtioScsiMultiQueues());
                if (vm.getVmInit() == null) {
                    vm.setVmInit(template.getVmInit());
                }
            }
            if (incoming.isSetUseLatestTemplateVersion()) {
                vm.setUseLatestVersion(incoming.isUseLatestTemplateVersion());
            }

            final AddVmPoolParameters parameters = new AddVmPoolParameters(entity, vm, size);
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
                QueryType.GetVmTemplate,
                new GetVmTemplateParameters(name),
                "GetVmTemplate");
    }

    @Override
    public Response allocateVm(Action action) {
        return doAction(ActionType.AttachUserToVmFromPoolAndRun,
                        new AttachUserToVmFromPoolAndRunParameters(guid,  getCurrent().getUser().getId()),
                        action,
                        new VmQueryIdResolver(QueryType.GetVmByVmId,
                                              IdQueryParameters.class));

    }

    protected class VmQueryIdResolver extends EntityResolver {

        private QueryType query;
        private Class<? extends QueryParametersBase> queryParamsClass;

        public VmQueryIdResolver(QueryType query, Class<? extends QueryParametersBase> queryParamsClass) {
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
        return performAction(ActionType.RemoveVmPool, new VmPoolParametersBase(asGuid(id)));
    }
}
