package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.NetworkAttachment;
import org.ovirt.engine.api.model.NetworkAttachments;
import org.ovirt.engine.api.resource.NetworkAttachmentsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.NetworkAttachmentParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendNetworkAttachmentsResource
        extends AbstractBackendCollectionResource<NetworkAttachment, org.ovirt.engine.core.common.businessentities.network.NetworkAttachment>
        implements NetworkAttachmentsResource {

    private Guid hostId;

    protected AbstractBackendNetworkAttachmentsResource(Guid hostId) {
        super(NetworkAttachment.class,
                org.ovirt.engine.core.common.businessentities.network.NetworkAttachment.class);
        this.hostId = hostId;
    }

    public Guid getHostId() {
        return hostId;
    }

    @Override
    public NetworkAttachments list() {
        verifyIfHostExistsToHandle404StatusCode();
        return mapCollection(getNetworkAttachments());
    }

    protected void verifyIfHostExistsToHandle404StatusCode() {
        Guid hostId = getHostId();

        //verify if host exists to handle 404 status code.
        getEntity(VDS.class, QueryType.GetVdsByVdsId, new IdQueryParameters(hostId), hostId.toString(), true);
    }

    @Override
    public Response add(NetworkAttachment attachment) {
        validateParameters(attachment, "network.id|name");
        org.ovirt.engine.core.common.businessentities.network.NetworkAttachment networkAttachment = map(attachment);
        NetworkAttachmentParameters params = new NetworkAttachmentParameters(hostId, networkAttachment);
        return performCreate(ActionType.AddNetworkAttachment,
                params,
                new QueryIdResolver<Guid>(QueryType.GetNetworkAttachmentById, IdQueryParameters.class));
    }

    @Override
    protected NetworkAttachment addLinks(NetworkAttachment model,
            Class<? extends BaseResource> suggestedParent,
            String... excludeSubCollectionMembers) {
        return super.addLinks(model, getParentClass());
    }

    protected abstract Class<? extends BaseResource> getParentClass();

    protected abstract List<org.ovirt.engine.core.common.businessentities.network.NetworkAttachment> getNetworkAttachments();

    private NetworkAttachments mapCollection(List<org.ovirt.engine.core.common.businessentities.network.NetworkAttachment> networkAttachments) {
        NetworkAttachments collection = new NetworkAttachments();
        for (org.ovirt.engine.core.common.businessentities.network.NetworkAttachment networkAttachmentEntity : networkAttachments) {
            NetworkAttachment networkAttachmentModel = populate(map(networkAttachmentEntity), networkAttachmentEntity);
            collection.getNetworkAttachments().add(addLinks(networkAttachmentModel, getParentClass()));
        }

        return collection;
    }
}
