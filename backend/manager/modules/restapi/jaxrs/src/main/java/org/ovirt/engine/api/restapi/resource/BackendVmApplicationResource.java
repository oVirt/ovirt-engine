package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Application;
import org.ovirt.engine.api.model.Applications;
import org.ovirt.engine.api.resource.VmApplicationResource;

public class BackendVmApplicationResource extends AbstractBackendSubResource<Application, Applications> implements VmApplicationResource {

    BackendVmApplicationsResource parent;

    public BackendVmApplicationResource(String id, BackendVmApplicationsResource parent) {
        super(id, Application.class, Applications.class);
        this.parent = parent;
    }

    public BackendVmApplicationsResource getParent() {
        return parent;
    }

    @Override
    public Application get() {
        Applications applications = parent.list();
        for (Application app : applications.getApplications()) {
            if (app.getId().equals(id)) {
                return app;
            }
        }
        return notFound();
    }
}
