package org.ovirt.engine.api.restapi.resource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostedEngine;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.resource.HostResource;
import org.ovirt.engine.api.resource.HostsResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetValidHostsForVmsParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostsResource extends AbstractBackendCollectionResource<Host, VDS> implements
        HostsResource {

    private static final String DEFAULT_NAME = "Default";

    static final String GLUSTERONLY_MODE_COLLECTIONS_TO_HIDE = "storage";

    static final String MIGRATION_TARGET_OF = "migration_target_of";

    public BackendHostsResource() {
        super(Host.class, VDS.class);
    }

    @Override
    public Hosts list() {
        ApplicationMode appMode = getCurrent().getApplicationMode();
        if (appMode == ApplicationMode.GlusterOnly) {
            return listGlusterOnly();
        } else {
            return listAll();
        }
    }

    private Hosts listGlusterOnly() {
        if (isFiltered()) {
            return mapGlusterOnlyCollection(getBackendCollection(QueryType.GetAllHosts,
                    new QueryParametersBase()));
        } else {
            return mapGlusterOnlyCollection(getBackendCollection(SearchType.VDS));
        }
    }

    private Hosts listAll() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(QueryType.GetAllHosts,
                    new QueryParametersBase()));
        } else {
            String migrationTargetOf = ParametersHelper.getParameter(httpHeaders, uriInfo, MIGRATION_TARGET_OF);
            if (StringUtils.isNotEmpty(migrationTargetOf)) {
                String[] vmsIds = migrationTargetOf.split(",");
                List<VM> vms = Arrays.stream(vmsIds).map(
                        id -> getEntity(
                                VM.class,
                                QueryType.GetVmByVmId,
                                new IdQueryParameters(new Guid(id)),
                                "GetVmByVmId")
                ).collect(Collectors.toList());

                return mapCollection(getBackendCollection(QueryType.GetValidHostsForVms,
                        new GetValidHostsForVmsParameters(vms)));
            } else {
                return mapCollection(getBackendCollection(SearchType.VDS));
            }
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
        BackendExternalProviderHelper.completeExternalNetworkProviderConfigurations(this,
                host.getExternalNetworkProviderConfigurations());
        AddVdsActionParameters addParams = new AddVdsActionParameters(staticHost, host.getRootPassword());
        if (host.isSetOverrideIptables()) {
            addParams.setOverrideFirewall(host.isOverrideIptables());
        }
        addParams.setHostedEngineDeployConfiguration(HostResourceParametersUtil.getHostedEngineDeployConfiguration(this));
        addParams = (AddVdsActionParameters) getMapper
            (Host.class, VdsOperationActionParameters.class).map(host, addParams);
        return performCreate(ActionType.AddVds,
                               addParams,
                               new QueryIdResolver<Guid>(QueryType.GetVdsByVdsId, IdQueryParameters.class));
    }

    @Override
    protected Host doPopulate(Host model, VDS entity) {
        Host host = addHostedEngineIfConfigured(model, entity);
        return host;
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
                QueryType.GetClusterByName,
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
