package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.resource.AffinityGroupHostResource;
import org.ovirt.engine.api.resource.AffinityGroupHostsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupMemberChangeParameters;
import org.ovirt.engine.core.compat.Guid;


public class BackendAffinityGroupHostsResource extends AbstractBackendCollectionResource<Host, org.ovirt.engine.core.common.businessentities.VDS>
        implements AffinityGroupHostsResource {
    private final Guid affinityGroupId;

    public BackendAffinityGroupHostsResource(Guid affinityGroupId) {
        super(Host.class, org.ovirt.engine.core.common.businessentities.VDS.class);
        this.affinityGroupId = affinityGroupId;
    }

    @Override
    public Hosts list() {
        Hosts hosts = new Hosts();
        AffinityGroup affinityGroup = getEntity();

        if (affinityGroup.getVdsIds() != null) {
            for (int i = 0; i < affinityGroup.getVdsIds().size(); i++) {
                Host host = new Host();
                host.setId(affinityGroup.getVdsIds().get(i).toString());
                host.setName(affinityGroup.getVdsEntityNames().get(i));
                host = addLinks(populate(host, null));
                // remove host actions, not relevant to this context
                host.setActions(null);
                hosts.getHosts().add(host);
            }
        }

        return hosts;
    }

    @Override
    public Response add(Host host) {
        return performAction(ActionType.AddHostToAffinityGroup,
                new AffinityGroupMemberChangeParameters(affinityGroupId, getHostId(host)));
    }

    @Override
    protected org.ovirt.engine.core.common.scheduling.AffinityGroup getEntity() {
        return getEntity(org.ovirt.engine.core.common.scheduling.AffinityGroup.class,
                QueryType.GetAffinityGroupById,
                new IdQueryParameters(affinityGroupId),
                affinityGroupId.toString());
    }

    @Override
    public AffinityGroupHostResource getHostResource(String id) {
        return inject(new BackendAffinityGroupHostResource(affinityGroupId, id));
    }
}
