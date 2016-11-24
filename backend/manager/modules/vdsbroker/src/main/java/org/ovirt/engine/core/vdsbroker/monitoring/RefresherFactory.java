package org.ovirt.engine.core.vdsbroker.monitoring;

import javax.inject.Singleton;

import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.VdsManager;


@Singleton
public class RefresherFactory {

    public VmStatsRefresher create(VdsManager vdsManager) {
        return Injector.injectMembers(getRefresherForVds(vdsManager));
    }

    private VmStatsRefresher getRefresherForVds(VdsManager vdsManager) {
        return new EventVmStatsRefresher(vdsManager);
    }
}
