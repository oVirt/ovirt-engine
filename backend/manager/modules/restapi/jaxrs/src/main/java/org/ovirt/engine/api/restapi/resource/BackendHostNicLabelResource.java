package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.NetworkLabelResource;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.action.VdcActionType;

public class BackendHostNicLabelResource
    extends AbstractBaseHostNicLabelResource
    implements NetworkLabelResource {

    static final String LABELS = "networklabels";

    private String id;
    private BackendHostNicLabelsResource parent;

    protected BackendHostNicLabelResource(String id, BackendHostNicLabelsResource parent) {
        super(id, parent);

        this.id = id;
        this.parent = parent;
    }

    @Override
    protected String getUriPath() {
        return LABELS;
    }

    @Override
    protected Response performRemove() {
        return performAction(VdcActionType.UnlabelNic, new LabelNicParameters(parent.getHostNicId(), id));
    }
}
