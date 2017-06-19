package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.NetworkLabelResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VfsConfigLabelParameters;

public class BackendVirtualFunctionAllowedLabelResource
    extends AbstractBaseHostNicLabelResource
    implements NetworkLabelResource {

    private String id;
    private BackendVirtualFunctionAllowedLabelsResource parent;

    protected BackendVirtualFunctionAllowedLabelResource(String id, BackendVirtualFunctionAllowedLabelsResource parent) {
        super(id, parent);
        this.id = id;
        this.parent = parent;
    }

    @Override
    protected String getUriPath() {
        return BackendHostNicsResource.VIRTUAL_FUNCTION_ALLOWED_LABELS;
    }

    @Override
    protected Response performRemove() {
        return performAction(ActionType.RemoveVfsConfigLabel,
                new VfsConfigLabelParameters(parent.getHostNicId(), id));
    }
}
