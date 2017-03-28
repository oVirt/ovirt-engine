package org.ovirt.engine.core.vdsbroker.monitoring;

import javax.inject.Singleton;

import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;


@Singleton
public class RefresherFactory {

    public VmStatsRefresher create(VdsManager vdsManager, ResourceManager resourceManager) {
        return Injector.injectMembers(getRefresherForVds(vdsManager, resourceManager));
    }

    private VmStatsRefresher getRefresherForVds(VdsManager vdsManager, ResourceManager resourceManager) {
        return new EventVmStatsRefresher(vdsManager, resourceManager);
    }
}
