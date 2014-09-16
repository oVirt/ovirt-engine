package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VmPools;
import org.ovirt.engine.api.resource.VmPoolResource;
import org.ovirt.engine.api.resource.VmPoolsResource;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmPoolsResource
        extends AbstractBackendCollectionResource<VmPool, org.ovirt.engine.core.common.businessentities.VmPool>
    implements VmPoolsResource {

    static final String SUB_COLLECTION = "permissions";

    public BackendVmPoolsResource() {
        super(VmPool.class, org.ovirt.engine.core.common.businessentities.VmPool.class, SUB_COLLECTION);
    }

    @Override
    public VmPools list() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllVmPoolsAttachedToUser,
                    new VdcQueryParametersBase()));
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

        org.ovirt.engine.core.common.businessentities.VmPool entity = map(pool);
        entity.setVmPoolType(VmPoolType.Automatic);

        VM vm = mapToVM(pool, template);

        int size = pool.isSetSize() ? pool.getSize() : 1;

        AddVmPoolWithVmsParameters params = new AddVmPoolWithVmsParameters(entity, vm, size, -1);
        params.setConsoleEnabled(!getConsoleDevicesForEntity(template.getId()).isEmpty());
        params.setVirtioScsiEnabled(!VmHelper.getVirtioScsiControllersForEntity(this, template.getId()).isEmpty());
        params.setSoundDeviceEnabled((pool.isSetSoundcardEnabled() ? pool.isSoundcardEnabled() : !VmHelper.getSoundDevicesForEntity(this, template.getId()).isEmpty()));

        return performCreate(VdcActionType.AddVmPoolWithVms,
                               params,
                               new QueryIdResolver<Guid>(VdcQueryType.GetVmPoolById,
                                                   IdQueryParameters.class));
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
    public VmPool doPopulate(VmPool pool, org.ovirt.engine.core.common.businessentities.VmPool entity) {
        setRngDevice(pool);
        return pool;
    }

    protected void setRngDevice(VmPool model) {
        List<VmRngDevice> rngDevices = getEntity(List.class,
                VdcQueryType.GetRngDevice,
                new IdQueryParameters(Guid.createGuidFromString(model.getId())),
                "GetRngDevice", true);

        if (rngDevices != null && !rngDevices.isEmpty()) {
            model.setRngDevice(RngDeviceMapper.map(rngDevices.get(0), null));
        }
    }

    @Override
    protected VmPool deprecatedPopulate(VmPool model, org.ovirt.engine.core.common.businessentities.VmPool entity) {
        if (model.isSetSize() && model.getSize() > 0) {
            VM vm = getVM(model);
            model.setTemplate(new Template());
            model.getTemplate().setId(vm.getVmtGuid().toString());
        }
        return model;
    }

    private VM getVM(VmPool model) {
        if (isFiltered()) {
            return getEntity(VM.class,
                         VdcQueryType.GetVmDataByPoolId,
                         new IdQueryParameters(asGuid(model.getId())),
                         model.getId());
        }
        return getEntity(VM.class,
                VdcQueryType.GetVmDataByPoolName,
                new NameQueryParameters(model.getName()),
                "Vms: pool=" + model.getName());
    }

    protected VM mapToVM(VmPool model, VmTemplate template) {
        // apply template
        VmStatic vmStatic = getMapper(VmTemplate.class, VmStatic.class).map(template, null);
        // override with client-provided data
        VM vm = getMapper(VmPool.class, VM.class).map(model, new VM(vmStatic, new VmDynamic(), new VmStatistics()));

        return vm;
    }

    protected VmPools mapCollection(List<org.ovirt.engine.core.common.businessentities.VmPool> entities) {
        VmPools collection = new VmPools();
        for (org.ovirt.engine.core.common.businessentities.VmPool entity : entities) {
            collection.getVmPools().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    protected boolean namedCluster(VmPool pool) {
        return pool.getCluster().isSetName() && !pool.getCluster().isSetId();
    }

    protected String getClusterId(VmPool pool) {
        return getEntity(VDSGroup.class,
                VdcQueryType.GetVdsGroupByName,
                new NameQueryParameters(pool.getCluster().getName()),
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
                    VdcQueryType.GetVmTemplate,
                    new GetVmTemplateParameters(pool.getTemplate().getName()),
                    "Template: name=" + pool.getTemplate().getName());
        }
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                VdcQueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

}
