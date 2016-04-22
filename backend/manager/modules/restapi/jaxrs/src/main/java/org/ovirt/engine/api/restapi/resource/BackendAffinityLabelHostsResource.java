package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.resource.AffinityLabelHostResource;
import org.ovirt.engine.api.resource.AffinityLabelHostsResource;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VDS;

public class BackendAffinityLabelHostsResource extends AbstractBackendAffinityLabelledEntitiesResource<Host, Hosts, VDS>
        implements AffinityLabelHostsResource {
    public BackendAffinityLabelHostsResource(String parentId) {
        super(parentId, Host.class, VDS.class, VDS::new);
    }

    @Override
    public Response add(Host entity) {
        return super.add(entity);
    }

    @Override
    protected Hosts mapCollection(List<VDS> entities) {
        Hosts hosts = new Hosts();
        for (VDS host: entities) {
            Host hostModel = new Host();
            hostModel.setId(host.getId().toString());
            hosts.getHosts().add(addLinks(hostModel));
        }
        return hosts;
    }

    @Override
    public Hosts list() {
        Label label = getLabel();
        return mapCollection(label.getHosts().stream().map(g -> {
            VDS host = new VDS();
            host.setId(g);
            return host;
        }).collect(Collectors.toList()));
    }

    @Override
    public AffinityLabelHostResource getHostResource(@PathParam("id") String id) {
        return inject(new BackendAffinityLabelHostResource(parentId, id));
    }
}
