package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostedEngine;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.HostResource;
import org.ovirt.engine.api.resource.HostsResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostsResource extends AbstractBackendCollectionResource<Host, VDS> implements
        HostsResource {

    private static final String DEFAULT_NAME = "Default";

    static final String[] SUB_COLLECTIONS = {
        "affinitylabels",
        "devices",
        "fenceagents",
        "hooks",
        "katelloerrata",
        "networkattachments",
        "nics",
        "numanodes",
        "permissions",
        "statistics",
        "storage",
        "storageconnectionextensions",
        "tags",
        "unmanagednetworks",
    };

    static final String GLUSTERONLY_MODE_COLLECTIONS_TO_HIDE = "storage";

    public BackendHostsResource() {
        super(Host.class, VDS.class, SUB_COLLECTIONS);
    }

    @Override
    public Hosts list() {
        ApplicationMode appMode = getCurrent().getApplicationMode();
        if (appMode == ApplicationMode.GlusterOnly) {
            return listGlusterOnly();
        }
        else {
            return listAll();
        }
    }

    private Hosts listGlusterOnly() {
        if (isFiltered()) {
            return mapGlusterOnlyCollection(getBackendCollection(VdcQueryType.GetAllHosts,
                    new VdcQueryParametersBase()));
        }
        else {
            return mapGlusterOnlyCollection(getBackendCollection(SearchType.VDS));
        }
    }

    private Hosts listAll() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllHosts,
                    new VdcQueryParametersBase()));
        }
        else {
            return mapCollection(getBackendCollection(SearchType.VDS));
        }
    }

    @Override
    public HostResource getHostResource(String id) {
        return inject(new BackendHostResource(id, this));
    }

    @Override
    public Response add(Host host) {
        validateParameters(host, "name", "address");
        VdsStatic staticHost = getMapper(Host.class, VdsStatic.class).map(host, null);
        staticHost.setClusterId(getClusterId(host));
        AddVdsActionParameters addParams = new AddVdsActionParameters(staticHost, host.getRootPassword());
        if (host.isSetOverrideIptables()) {
            addParams.setOverrideFirewall(host.isOverrideIptables());
        }
        addParams.setHostedEngineDeployConfiguration(HostResourceParametersUtil.getHostedEngineDeployConfiguration(this));
        addParams = (AddVdsActionParameters) getMapper
            (Host.class, VdsOperationActionParameters.class).map(host, addParams);
        return performCreate(VdcActionType.AddVds,
                               addParams,
                               new QueryIdResolver<Guid>(VdcQueryType.GetVdsByVdsId, IdQueryParameters.class));
    }

    @Override
    protected Host doPopulate(Host model, VDS entity) {
        Host host = addHostedEngineIfConfigured(model, entity);
        return host;
    }

    @Override
    protected Host deprecatedPopulate(Host model, VDS entity) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        if (details.contains("statistics")) {
            addStatistics(model, entity);
        }
        return model;
    }

    public void addStatistics(Host model, VDS entity) {
        model.setStatistics(new Statistics());
        HostStatisticalQuery query = new HostStatisticalQuery(newModel(model.getId()));
        List<Statistic> statistics = query.getStatistics(entity);
        for (Statistic statistic : statistics) {
            LinkHelper.addLinks(statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }

    private Hosts mapCollection(List<VDS> entities) {
        Hosts collection = new Hosts();
        for (VDS entity : entities) {
            collection.getHosts().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    private Hosts mapGlusterOnlyCollection(List<VDS> entities) {
        Hosts collection = new Hosts();
        for (VDS entity : entities) {
            collection.getHosts().add(addLinks(populate(map(entity), entity), GLUSTERONLY_MODE_COLLECTIONS_TO_HIDE));
        }
        return collection;
    }

    private Guid getClusterId(Host host) {
        if (host.isSetCluster()) {
            org.ovirt.engine.api.model.Cluster cluster = host.getCluster();
            if (cluster.isSetId()) {
                return asGuid(cluster.getId());
            }
            if (cluster.isSetName()) {
                return getClusterIdByName(cluster.getName());
            }
        }
        return getClusterIdByName(DEFAULT_NAME);
    }

    private Guid getClusterIdByName(String name) {
        return getEntity(Cluster.class,
                VdcQueryType.GetClusterByName,
                new NameQueryParameters(name),
                "Cluster: name=" + name).getId();
    }

    Host addHostedEngineIfConfigured(Host host, VDS entity) {
        /* Add entity data only if the hosted engine agent is configured on this host */
        if (entity.getHighlyAvailableIsConfigured()) {
            HostedEngine hostedEngine = getMapper(VDS.class, HostedEngine.class).map(entity, null);
            host.setHostedEngine(hostedEngine);
        }
        return host;
    }
}
