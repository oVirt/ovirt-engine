package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.LabelResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VfsConfigLabelParameters;

public class BackendVirtualFunctionAllowedLabelResource extends AbstractBaseHostNicLabelResource
        implements LabelResource {

    private String id;
    private BackendVirtualFunctionAllowedLabelsResource parent;

    protected BackendVirtualFunctionAllowedLabelResource(String id, BackendVirtualFunctionAllowedLabelsResource parent) {
        super(id, parent);
        this.id = id;
        this.parent = parent;
    }

    @Override
    protected Response performRemove() {
        return performAction(VdcActionType.RemoveVfsConfigLabel,
                new VfsConfigLabelParameters(parent.getHostNicId(), id));
    }
}
