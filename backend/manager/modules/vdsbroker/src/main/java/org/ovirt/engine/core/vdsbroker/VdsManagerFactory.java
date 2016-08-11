package org.ovirt.engine.core.vdsbroker;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.di.Injector;

@Singleton
public class VdsManagerFactory {

    public VdsManager create(VDS vds, ResourceManager resourceManager) {
        // we need to pass the reference to ResourceManager manually because CDI
        // cannot inject ResourceManager while we are inside of ResourceManager's
        // @PostConstruct method
        return Injector.injectMembers(new VdsManager(vds, resourceManager));
    }
}
