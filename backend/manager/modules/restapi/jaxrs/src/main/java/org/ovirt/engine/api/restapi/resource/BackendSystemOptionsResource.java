package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.resource.SystemOptionResource;
import org.ovirt.engine.api.resource.SystemOptionsResource;

public class BackendSystemOptionsResource extends BackendResource implements SystemOptionsResource {
    /*
     * This class is purposely left almost empty. We don't want to provide a mechanism to list options.
     */

    @Override
    public SystemOptionResource getOptionResource(String id) {
        return inject(new BackendSystemOptionResource(id));
    }
}
