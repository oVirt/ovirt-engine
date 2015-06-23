package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Creation;
import org.ovirt.engine.api.resource.CreationResource;

public class BackendCreationResource
        extends AbstractBackendAsyncStatusResource<Creation>
        implements CreationResource {

    public BackendCreationResource(String ids) {
        super(Creation.class, ids);
    }

    @Override
    public Creation get() {
        return query();
    }

    @Override
    protected Creation deprecatedPopulate(Creation model, List entity) {
        model.setId(asString(ids));
        if (model.isSetFault()) {
            setReason(model.getFault());
        }
        return model;
    }
}
