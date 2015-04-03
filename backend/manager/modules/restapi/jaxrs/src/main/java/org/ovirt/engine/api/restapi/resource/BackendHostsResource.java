package org.ovirt.engine.api.restapi.resource;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostedEngine;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.HostResource;
import org.ovirt.engine.api.resource.HostsResource;
import org.ovirt.engine.api.restapi.types.FenceAgentMapper;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendHostsResource extends AbstractBackendCollectionResource<Host, VDS> implements
        HostsResource {

    private static final Logger log = LoggerFactory.getLogger(BackendHostsResource.class);
    private static final String DEFAULT_NAME = "Default";
    static final String[] SUB_COLLECTIONS = { "storage", "nics", "numanodes", "tags", "permissions", "statistics",
            "hooks", "fenceagents", "katelloerrata" };
    static final String GLUSTERONLY_MODE_COLLECTIONS_TO_HIDE = "storage";

    public BackendHostsResource() {
        super(Host.class, VDS.class, SUB_COLLECTIONS);
    }

    @Override
    public Hosts list() {
        ApplicationMode appMode = getCurrent().get(ApplicationMode.class);
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
    @SingleEntityResource
    public HostResource getHostSubResource(String id) {
        return inject(new BackendHostResource(id, this));
    }

    @Override
    public Response add(Host host) {
        validateEnums(Host.class, host);
        validateParameters(host, "name", "address");
        VdsStatic staticHost = getMapper(Host.class, VdsStatic.class).map(host, null);
        staticHost.setVdsGroupId(getClusterId(host));
        AddVdsActionParameters addParams = new AddVdsActionParameters(staticHost, host.getRootPassword());
        if (host.isSetOverrideIptables()) {
            addParams.setOverrideFirewall(host.isOverrideIptables());
        }
        if (host.isSetRebootAfterInstallation()) {
            addParams.setRebootAfterInstallation(host.isRebootAfterInstallation());
        }
        if (host.isSetPowerManagement() && host.getPowerManagement().isSetAgents()) {
            List<FenceAgent> agents = new LinkedList<FenceAgent>();
            for (Agent agent : host.getPowerManagement().getAgents().getAgents()) {
                agents.add(FenceAgentMapper.map(agent, null));
            }
            addParams.setFenceAgents(agents);
        }
        addParams = (AddVdsActionParameters) getMapper
                (Host.class, VdsOperationActionParameters.class).map(host, (VdsOperationActionParameters) addParams);
        return performCreate(VdcActionType.AddVds,
                               addParams,
                               new QueryIdResolver<Guid>(VdcQueryType.GetVdsByVdsId, IdQueryParameters.class));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVds, new RemoveVdsParameters(asGuid(id)));
    }

    @Override
    public Response remove(String id, Action action) {
        getEntity(id); //verifies that entity exists, returns 404 otherwise.
        return performAction(VdcActionType.RemoveVds,
                new RemoveVdsParameters(asGuid(id), action != null && action.isSetForce() ? action.isForce() : false));
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
        addCertificateInfo(model);
        return model;
    }

    public void addStatistics(Host model, VDS entity) {
        model.setStatistics(new Statistics());
        HostStatisticalQuery query = new HostStatisticalQuery(newModel(model.getId()));
        List<Statistic> statistics = query.getStatistics(entity);
        for (Statistic statistic : statistics) {
            LinkHelper.addLinks(uriInfo, statistic, query.getParentType());
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

    //TODO: REVISIT when backend expose CertificateSubject in vds
    public Host addCertificateInfo(Host host) {
        VdcQueryReturnValue result =
            runQuery(VdcQueryType.GetVdsCertificateSubjectByVdsId,
                    new IdQueryParameters(asGuid(host.getId())));

        if (result != null
            && result.getSucceeded()
            && result.getReturnValue() != null) {
            String subject = result.getReturnValue().toString();
            if (subject != null){
                host.setCertificate(new Certificate());
                host.getCertificate().setSubject(subject);
                host.getCertificate().setOrganization(subject.split(",")[0].replace("O=", ""));
            }
        }
        else {
            log.error("Could not fetch certificate info for host '{}'", host.getId());
        }
        return host;
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
            Cluster cluster = host.getCluster();
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
        return getEntity(VDSGroup.class,
                VdcQueryType.GetVdsGroupByName,
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
