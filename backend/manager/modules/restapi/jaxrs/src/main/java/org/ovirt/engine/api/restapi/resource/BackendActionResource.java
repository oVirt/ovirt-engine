package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.resource.ActionResource;

public class BackendActionResource
        extends AbstractBackendAsyncStatusResource<Action>
        implements ActionResource {

    public BackendActionResource(String action, String ids) {
        super(Action.class, ids);
    }

    public Response get() {
        return Response.ok(query()).build();
    }

    @Override
    protected Action deprecatedPopulate(Action model, List entity) {
        model.setId(asString(ids));
        if (model.isSetFault()) {
            setReason(model.getFault());
        }
        return model;
    }

    @Override
    public Action getAction() {
        // REVISIT
        return null;
    }
}
