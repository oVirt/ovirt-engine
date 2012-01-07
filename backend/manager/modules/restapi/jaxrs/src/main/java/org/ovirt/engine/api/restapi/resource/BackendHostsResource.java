package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.common.util.LinkHelper;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.HostResource;
import org.ovirt.engine.api.resource.HostsResource;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostsResource extends AbstractBackendCollectionResource<Host, VDS> implements
        HostsResource {

    static final String[] SUB_COLLECTIONS = { "storage", "nics", "tags", "permissions", "statistics" };

    public BackendHostsResource() {
        super(Host.class, VDS.class, SUB_COLLECTIONS);
    }

    @Override
    public Hosts list() {
        return mapCollection(getBackendCollection(SearchType.VDS));
    }

    @Override
    @SingleEntityResource
    public HostResource getHostSubResource(String id) {
        return inject(new BackendHostResource(id, this));
    }

    @Override
    public Response add(Host host) {
        validateParameters(host, "name", "address", "rootPassword");
        VdsStatic staticHost = getMapper(Host.class, VdsStatic.class).map(host, null);
        staticHost.setvds_group_id(getClusterId(host));
        AddVdsActionParameters addParams = new AddVdsActionParameters(staticHost, host.getRootPassword());
        if (host.isSetOverrideIptables()) {
            addParams.setOverrideFirewall(host.isOverrideIptables());
        }
        return performCreation(VdcActionType.AddVds,
                               addParams,
                               new QueryIdResolver(VdcQueryType.GetVdsByVdsId, GetVdsByVdsIdParameters.class));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVds, new VdsActionParameters(asGuid(id)));
    }

    @Override
    protected Host populate(Host model, VDS entity) {
        return addStatistics(model, entity, uriInfo, httpHeaders);
    }

    Host addStatistics(Host model, VDS entity, UriInfo ui, HttpHeaders httpHeaders) {
        if (DetailHelper.include(httpHeaders, "statistics")) {
            model.setStatistics(new Statistics());
            HostStatisticalQuery query = new HostStatisticalQuery(newModel(model.getId()));
            List<Statistic> statistics = query.getStatistics(entity);
            for (Statistic statistic : statistics) {
                LinkHelper.addLinks(ui, statistic, query.getParentType());
            }
            model.getStatistics().getStatistics().addAll(statistics);
        }
        return model;
    }

    private Hosts mapCollection(List<VDS> entities) {
        Hosts collection = new Hosts();
        for (VDS entity : entities) {
            collection.getHosts().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    private Guid getClusterId(Host host) {
        return host.isSetCluster() && host.getCluster().isSetId()
               ? new Guid(host.getCluster().getId())
               : getEntity(VDSGroup.class,
                           SearchType.Cluster,
                           "Cluster: name="
                           + (host.isSetCluster() && host.getCluster().isSetName()
                              ? host.getCluster().getName()
                              : "Default")).getID();
    }

}
