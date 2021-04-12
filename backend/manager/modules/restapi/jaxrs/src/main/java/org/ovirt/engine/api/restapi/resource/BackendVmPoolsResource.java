package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VmPools;
import org.ovirt.engine.api.resource.VmPoolResource;
import org.ovirt.engine.api.resource.VmPoolsResource;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmPoolParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetFilteredAndSortedParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmPoolsResource
        extends AbstractBackendCollectionResource<VmPool, org.ovirt.engine.core.common.businessentities.VmPool>
    implements VmPoolsResource {

    public BackendVmPoolsResource() {
        super(VmPool.class, org.ovirt.engine.core.common.businessentities.VmPool.class);
    }

    @Override
    public VmPools list() {
        if (isFiltered()) {
            if (isSortedAndMaxResults()) { //Specific use-case of User-Portal
                return getVmPoolsFilteredAndSorted();
            } else {
                return mapCollection(getBackendCollection(QueryType.GetAllVmPoolsAttachedToUser,
                        new QueryParametersBase(), SearchType.VmPools));
            }
        } else {
            return mapCollection(getBackendCollection(SearchType.VmPools));
        }
    }

    /**
     * Check for a combination of sorting ("sortby name asc) and specification of
     * max results ('max' URL parameter). This is a use-case of the User-Portal
     * that requires specific handling.
     */
    private boolean isSortedAndMaxResults() {
        String searchConstraint = QueryHelper.getConstraint(httpHeaders, uriInfo, "", modelType);
        int max = ParametersHelper.getIntegerParameter(httpHeaders, uriInfo, "max", -1, -1);
        return searchConstraint!=null
                && !searchConstraint.isEmpty()
                && searchConstraint.toLowerCase().contains("sortby name asc")
                && max != -1;
    }

    /**
     * Specific use-case in the User-Portal - Get vm-pools:
     *
     *   1) filtered by user
     *   2) sorted (ascending order)
     *   3) with max # of results specified.
     *   4) potentially with page number (paging)
     *
     * The engine does not support search + filtering simultaneously.
     * The API supports this using an intersection of two queries, but
     * can not consider max results as well. This is why a designated
     * query is needed.
     *
     * (https://bugzilla.redhat.com/1537735)
     */
    private VmPools getVmPoolsFilteredAndSorted() {
        int max = ParametersHelper.getIntegerParameter(httpHeaders, uriInfo, "max", -1, -1);
        String searchConstraint = QueryHelper.getConstraint(httpHeaders, uriInfo, "", modelType);
        Integer pageNum = QueryHelper.parsePageNum(searchConstraint);
        GetFilteredAndSortedParameters params = new GetFilteredAndSortedParameters(max, pageNum == null ? 1 : pageNum);
        return mapCollection(getBackendCollection(QueryType.GetAllVmPoolsFilteredAndSorted, params));
    }

    @Override
    public Response add(VmPool pool) {
        validateParameters(pool, "name", "template.id|name", "cluster.id|name");

        Cluster cluster = getCluster(pool);
        pool.getCluster().setId(cluster.getId().toString());

        VmTemplate template = getVmTemplate(pool);
        if (namedTemplate(pool)) {
            pool.getTemplate().setId(template.getId().toString());
        }

        org.ovirt.engine.core.common.businessentities.VmPool entity = map(pool);

        VM vm = mapToVM(pool, template, cluster);

        int size = pool.isSetSize() ? pool.getSize() : 1;

        AddVmPoolParameters params = new AddVmPoolParameters(entity, vm, size);
        params.setConsoleEnabled(pool.isSetVm() && pool.getVm().isSetConsole() && pool.getVm().getConsole().isSetEnabled() ?
                pool.getVm().getConsole().isEnabled() : !getConsoleDevicesForEntity(template.getId()).isEmpty());
        params.setVirtioScsiEnabled(!VmHelper.getVirtioScsiControllersForEntity(this, template.getId()).isEmpty());
        params.setSoundDeviceEnabled(pool.isSetSoundcardEnabled() ? pool.isSoundcardEnabled() : !VmHelper.getSoundDevicesForEntity(this, template.getId()).isEmpty());
        params.setRngDevice(pool.isSetVm() && pool.getVm().isSetRngDevice() ?
                RngDeviceMapper.map(pool.getVm().getRngDevice(), null) : params.getRngDevice());
        params.getVmStaticData().setCustomProperties(pool.isSetVm() && pool.getVm().isSetCustomProperties() ?
                CustomPropertiesParser.parse(pool.getVm().getCustomProperties().getCustomProperties()) : params.getVmStaticData().getCustomProperties());
        params.setTpmEnabled(pool.isSetTpmEnabled() ? pool.isTpmEnabled() : null);

        return performCreate(ActionType.AddVmPool,
                               params,
                               new QueryIdResolver<Guid>(QueryType.GetVmPoolById,
                                                   IdQueryParameters.class));
    }

    @Override
    public VmPoolResource getPoolResource(String id) {
        return inject(new BackendVmPoolResource(id, this));
    }

    @Override
    public VmPool doPopulate(VmPool pool, org.ovirt.engine.core.common.businessentities.VmPool entity) {
        VM vmModel = getVM(pool);
        if (vmModel != null) {
            Vm vm = VmMapper.map(vmModel, new Vm());
            DisplayHelper.adjustDisplayData(this, vm, false);
            BackendVmDeviceHelper.setPayload(this, vm);
            BackendVmDeviceHelper.setConsoleDevice(this, vm);
            BackendVmDeviceHelper.setVirtioScsiController(this, vm);
            BackendVmDeviceHelper.setSoundcard(this, vm);
            BackendVmDeviceHelper.setCertificateInfo(this, vm);
            BackendVmDeviceHelper.setRngDevice(this, vm);
            BackendVmDeviceHelper.setTpmDevice(this, vm);
            pool.setVm(vm);
        }
        // Legacy code
        setRngDevice(pool);
        return pool;
    }

    protected void setRngDevice(VmPool model) {
        List<VmRngDevice> rngDevices = getEntity(List.class,
                QueryType.GetRngDevice,
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
            model = getMapper(VM.class, VmPool.class).map(vm, model);
            DisplayHelper.adjustDisplayData(this, model.getVm(), false);
            if (vm.getInstanceTypeId() != null) {
                model.setInstanceType(new InstanceType());
                model.getInstanceType().setId(vm.getInstanceTypeId().toString());
            }
        }
        return model;
    }

    private VM getVM(VmPool model) {
        if (isFiltered()) {
            return getEntity(VM.class,
                         QueryType.GetVmDataByPoolId,
                         new IdQueryParameters(asGuid(model.getId())),
                         model.getId());
        }
        return getEntity(VM.class,
                QueryType.GetVmDataByPoolName,
                new NameQueryParameters(model.getName()),
                "Vms: pool=" + model.getName());
    }

    protected VM mapToVM(VmPool model, VmTemplate template, Cluster cluster) {
        // apply template
        VmStatic fromTemplate = getMapper(VmTemplate.class, VmStatic.class).map(template, null);

        VmStatic fromInstanceType = null;
        if (model.isSetInstanceType()) {
            org.ovirt.engine.core.common.businessentities.InstanceType instanceType = loadInstanceType(model);
            fromTemplate.setInstanceTypeId(instanceType.getId());
            fromInstanceType = VmMapper.map(instanceType, fromTemplate, cluster.getCompatibilityVersion());
            fromInstanceType.setInstanceTypeId(instanceType.getId());
        }

        // override with client-provided data
        VM vm = new VM(getMapper(VmPool.class, VmStatic.class).map(
                model, fromInstanceType != null ? fromInstanceType : fromTemplate), new VmDynamic(), new VmStatistics());

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

    protected Cluster getCluster(VmPool pool) {
        if (namedCluster(pool)) {
            return getEntity(Cluster.class,
                    QueryType.GetClusterByName,
                    new NameQueryParameters(pool.getCluster().getName()),
                    "Cluster: name=" + pool.getCluster().getName());
        } else {
            return getEntity(Cluster.class,
                    QueryType.GetClusterById,
                    new IdQueryParameters(asGuid(pool.getCluster().getId())),
                    "Cluster: id=" + pool.getCluster().getId());
        }
    }

    protected boolean namedTemplate(VmPool pool) {
        return pool.getTemplate().isSetName() && !pool.getTemplate().isSetId();
    }

    protected VmTemplate getVmTemplate(VmPool pool) {
        if (pool.getTemplate().isSetId()) {
            return getEntity(VmTemplate.class,
                             QueryType.GetVmTemplate,
                             new GetVmTemplateParameters(asGuid(pool.getTemplate().getId())),
                             pool.getTemplate().getId());
        } else {
            GetVmTemplateParameters params = new GetVmTemplateParameters(pool.getTemplate().getName());
            params.setClusterId(asGuid(pool.getCluster().getId()));
            return getEntity(VmTemplate.class,
                    QueryType.GetVmTemplate,
                    params,
                    "Template: name=" + pool.getTemplate().getName());
        }
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                QueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

    private org.ovirt.engine.core.common.businessentities.InstanceType loadInstanceType(VmPool pool) {
        validateParameters(pool.getInstanceType(), "id|name");

        GetVmTemplateParameters params;
        String identifier;

        InstanceType instanceType = pool.getInstanceType();
        if (instanceType.isSetId()) {
            params = new GetVmTemplateParameters(asGuid(instanceType.getId()));
            identifier = "InstanceType: id=" + instanceType.getId();
        } else {
            params = new GetVmTemplateParameters(instanceType.getName());
            identifier = "InstanceType: name=" + instanceType.getName();
        }

        return getEntity(VmTemplate.class,
                QueryType.GetInstanceType,
                params,
                identifier);
    }

}
