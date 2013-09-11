package org.ovirt.engine.api.restapi.util;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.ovirt.engine.api.restapi.resource.BackendResource;

public abstract class ResourceHelper extends BackendResource {

    public ResourceHelper() {
        ResteasyProviderFactory.getInstance()
                .getInjectorFactory()
                .createPropertyInjector(this.getClass())
                .inject(this);
    }

}
