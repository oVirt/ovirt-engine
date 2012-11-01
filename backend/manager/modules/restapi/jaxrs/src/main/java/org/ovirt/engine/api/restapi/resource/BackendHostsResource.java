package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.common.util.LinkHelper;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.HostResource;
import org.ovirt.engine.api.resource.HostsResource;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostsResource extends AbstractBackendCollectionResource<Host, VDS> implements
        HostsResource {

    static final String[] SUB_COLLECTIONS = { "storage", "nics", "tags", "permissions", "statistics" };
    static final String GLUSTERONLY_MODE_COLLECTIONS_TO_HIDE = "storage";

    public BackendHostsResource() {
        super(Host.class, VDS.class, SUB_COLLECTIONS);
    }

    @Override
    public Hosts list() {
        ApplicationMode appMode = getCurrent().get(ApplicationMode.class);
        if (appMode == ApplicationMode.GlusterOnly)
        {
            return listGlusterOnly();
        }
        else
        {
            return listAll();
        }
    }

    private Hosts listGlusterOnly() {
        if (isFiltered())
        {
            return mapGlusterOnlyCollection(getBackendCollection(VdcQueryType.GetAllHosts,
                    new VdcQueryParametersBase()));
        }
        else
        {
            return mapGlusterOnlyCollection(getBackendCollection(SearchType.VDS));
        }
    }

    private Hosts listAll() {
        if (isFiltered())
        {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllHosts,
                    new VdcQueryParametersBase()));
        }
        else
        {
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
        validateParameters(host, "name", "address", "rootPassword");
        VdsStatic staticHost = getMapper(Host.class, VdsStatic.class).map(host, null);
        staticHost.setvds_group_id(getClusterId(host));
        AddVdsActionParameters addParams = new AddVdsActionParameters(staticHost, host.getRootPassword());
        if (host.isSetOverrideIptables()) {
            addParams.setOverrideFirewall(host.isOverrideIptables());
        }
        if (host.isSetRebootAfterInstallation()) {
            addParams.setRebootAfterInstallation(host.isRebootAfterInstallation());
        }
        return performCreation(VdcActionType.AddVds,
                               addParams,
                               new QueryIdResolver<Guid>(VdcQueryType.GetVdsByVdsId, GetVdsByVdsIdParameters.class));
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
    protected Host populate(Host model, VDS entity) {
        Host host = addStatistics(model, entity, uriInfo, httpHeaders);
        addCertificateInfo(host);
        return host;
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

    //TODO: REVISIT when backend expose CertificateSubject in vds
    public Host addCertificateInfo(Host host) {
        VdcQueryReturnValue result =
            runQuery(VdcQueryType.GetVdsCertificateSubjectByVdsId,
                    new GetVdsByVdsIdParameters(asGuid(host.getId())));

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
            LOG.error("Could not fetch certificate info for host " + host.getId());
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
        return host.isSetCluster() && host.getCluster().isSetId()
               ? new Guid(host.getCluster().getId())
               : getEntity(VDSGroup.class,
                           SearchType.Cluster,
                           "Cluster: name="
                           + (host.isSetCluster() && host.getCluster().isSetName()
                              ? host.getCluster().getName()
                              : "Default")).getId();
    }

}
