package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VmPools;
import org.ovirt.engine.api.resource.VmPoolResource;
import org.ovirt.engine.api.resource.VmPoolsResource;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;
import org.ovirt.engine.core.common.queries.GetVmPoolByIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.GetVmdataByPoolIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;

public class BackendVmPoolsResource
    extends AbstractBackendCollectionResource<VmPool, vm_pools>
    implements VmPoolsResource {

    static final String SUB_COLLECTION = "permissions";

    public BackendVmPoolsResource() {
        super(VmPool.class, vm_pools.class);
    }

    @Override
    public VmPools list() {
        if (isFiltered()) {
            GetAllVmPoolsAttachedToUserParameters params = new GetAllVmPoolsAttachedToUserParameters(getCurrent().get(VdcUser.class).getUserId());
            return mapCollection(getBackendCollection(VdcQueryType.GetAllVmPoolsAttachedToUser, params));
        } else {
            return mapCollection(getBackendCollection(SearchType.VmPools));
        }
    }

    @Override
    public Response add(VmPool pool) {
        validateParameters(pool, "name", "template.id|name", "cluster.id|name");

        VmTemplate template = getVmTemplate(pool);
        if (namedTemplate(pool)) {
            pool.getTemplate().setId(template.getId().toString());
        }

        if (namedCluster(pool)) {
            pool.getCluster().setId(getClusterId(pool));
        }

        vm_pools entity = map(pool);
        entity.setvm_pool_type(VmPoolType.Automatic);

        VM vm = mapToVM(pool, template);

        int size = pool.isSetSize() ? pool.getSize() : 1;

        return performCreation(VdcActionType.AddVmPoolWithVms,
                               new AddVmPoolWithVmsParameters(entity, vm, size, -1),
                               new QueryIdResolver(VdcQueryType.GetVmPoolById,
                                                   GetVmPoolByIdParameters.class));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVmPool, new VmPoolParametersBase(asGuid(id)));
    }

    @Override
    @SingleEntityResource
    public VmPoolResource getVmPoolSubResource(String id) {
        return inject(new BackendVmPoolResource(id, this));
    }

    @Override
    public VmPool populate(VmPool pool, vm_pools entity) {
        if (pool.isSetSize() && pool.getSize() > 0) {
            VM vm = getVM(pool);
            pool.setTemplate(new Template());
            pool.getTemplate().setId(vm.getvmt_guid().toString());
        }
        return pool;
    }

   private VM getVM(VmPool pool) {
        if (isFiltered()) {
            return getEntity(VM.class,
                         VdcQueryType.GetVmDataByPoolId,
                         new GetVmdataByPoolIdParameters(asGuid(pool.getId())),
                         pool.getId());
        }
        return getEntity(VM.class, SearchType.VM, "Vms: pool=" + pool.getName());
    }

    protected VM mapToVM(VmPool model, VmTemplate template) {
        // REVISIT: we should reverse the mapping order here,
        // so that we map from template->VM in the first instance
        // and then override any values (memory size, boot order etc.)
        // that are set in the client-provided VmPool instance
        VM vm = getMapper(VmPool.class, VM.class).map(model, null);
        vm.getStaticData().setmem_size_mb(template.getmem_size_mb());
        return vm;
    }

    protected VmPools mapCollection(List<vm_pools> entities) {
        VmPools collection = new VmPools();
        for (vm_pools entity : entities) {
            collection.getVmPools().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    protected boolean namedCluster(VmPool pool) {
        return pool.getCluster().isSetName() && !pool.getCluster().isSetId();
    }

    protected String getClusterId(VmPool pool) {
        return getEntity(VDSGroup.class,
                         SearchType.Cluster,
                         "Cluster: name=" + pool.getCluster().getName()).getId().toString();
    }

    protected boolean namedTemplate(VmPool pool) {
        return pool.getTemplate().isSetName() && !pool.getTemplate().isSetId();
    }

    protected VmTemplate getVmTemplate(VmPool pool) {
        if (pool.getTemplate().isSetId()) {
            return getEntity(VmTemplate.class,
                             VdcQueryType.GetVmTemplate,
                             new GetVmTemplateParameters(asGuid(pool.getTemplate().getId())),
                             pool.getTemplate().getId());
        } else {
            return getEntity(VmTemplate.class,
                             SearchType.VmTemplate,
                             "Template: name=" + pool.getTemplate().getName());
        }
    }
}
