package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.NetworkAttachment;
import org.ovirt.engine.api.resource.NetworkAttachmentResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.NetworkAttachmentParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkAttachmentParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public abstract class AbstractBackendNetworkAttachmentResource<T extends AbstractBackendNetworkAttachmentsResource>
        extends AbstractBackendActionableResource<NetworkAttachment, org.ovirt.engine.core.common.businessentities.network.NetworkAttachment>
        implements NetworkAttachmentResource {

    private static final String OVERRIDE_CONFIGURATION = "override_configuration";
    private T parent;

    protected AbstractBackendNetworkAttachmentResource(String id, T parent) {
        super(id, NetworkAttachment.class, org.ovirt.engine.core.common.businessentities.network.NetworkAttachment.class);
        this.parent = parent;
    }

    @Override
    public NetworkAttachment get() {
        NetworkAttachment model =
                performGet(VdcQueryType.GetNetworkAttachmentById, new IdQueryParameters(guid), parent.getParentClass());
        model = parent.addParents(model);
        return addLinks(model);
    }

    @Override
    public NetworkAttachment update(NetworkAttachment resource) {
        return performUpdate(resource,
                new QueryIdResolver<>(VdcQueryType.GetNetworkAttachmentById, IdQueryParameters.class),
                VdcActionType.UpdateNetworkAttachment,
                new UpdateParametersProvider());
    }

    public T getParent() {
        return parent;
    }

    @Override
    protected NetworkAttachment addParents(NetworkAttachment model) {
        return parent.addParents(model);
    }

    protected class UpdateParametersProvider
            implements ParametersProvider<NetworkAttachment, org.ovirt.engine.core.common.businessentities.network.NetworkAttachment> {

        @Override
        public VdcActionParametersBase getParameters(NetworkAttachment incoming,
                org.ovirt.engine.core.common.businessentities.network.NetworkAttachment entity) {
            boolean overrideConfiguration = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, OVERRIDE_CONFIGURATION, true, false);
            org.ovirt.engine.core.common.businessentities.network.NetworkAttachment attachment = map(incoming, entity);
            attachment.setOverrideConfiguration(overrideConfiguration);
            NetworkAttachmentParameters params = new NetworkAttachmentParameters(parent.getHostId(), attachment);
            return params;
        }
    }

    @Override
    public Response remove() {
        get();
        RemoveNetworkAttachmentParameters params = new RemoveNetworkAttachmentParameters(parent.getHostId(), guid);
        return performAction(VdcActionType.RemoveNetworkAttachment, params);
    }
}
