package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.NetworkAttachment;
import org.ovirt.engine.api.resource.NetworkAttachmentResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNetworkAttachmentsResource extends AbstractBackendNetworkAttachmentsResource {

    public BackendHostNetworkAttachmentsResource(Guid hostId) {
        super(hostId);
    }

    @Override
    public NetworkAttachmentResource getAttachmentResource(String id) {
        return inject(new BackendHostNetworkAttachmentResource(id, this));
    }

    @Override
    protected List<org.ovirt.engine.core.common.businessentities.network.NetworkAttachment> getNetworkAttachments() {
        verifyIfHostExistsToHandle404StatusCode();

        return getBackendCollection(QueryType.GetNetworkAttachmentsByHostId, new IdQueryParameters(getHostId()));
    }

    @Override
    protected Class<? extends BaseResource> getParentClass() {
        return Host.class;
    }

    @Override
    protected NetworkAttachment addParents(NetworkAttachment model) {
        Host host = new Host();
        model.setHost(host);
        model.getHost().setId(getHostId().toString());
        if (model.isSetHostNic()) {
            model.getHostNic().setHost(host);
        }

        return model;
    }
}
