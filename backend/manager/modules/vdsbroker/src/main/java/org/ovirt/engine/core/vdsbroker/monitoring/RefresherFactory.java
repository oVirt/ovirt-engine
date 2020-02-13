package org.ovirt.engine.core.vdsbroker.monitoring;

import javax.inject.Singleton;

import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.monitoring.kubevirt.KubevirtHostConnectionRefresher;
import org.ovirt.engine.core.vdsbroker.monitoring.kubevirt.KubevirtVmStatsRefresher;

@Singleton
public class RefresherFactory {

    public VmStatsRefresher createVmStatsRefresher(VdsManager vdsManager, ResourceManager resourceManager) {
        return Injector.injectMembers(getVmStatsRefresher(vdsManager, resourceManager));
    }

    public HostConnectionRefresherInterface createHostConnectionRefresher(VdsManager vdsManager,
            ResourceManager resourceManager) {
        switch(vdsManager.getVdsType()) {
        case KubevirtNode:
            return Injector.injectMembers(new KubevirtHostConnectionRefresher(vdsManager));
        default:
            return new HostConnectionRefresher(vdsManager, resourceManager);
        }
    }

    private VmStatsRefresher getVmStatsRefresher(VdsManager vdsManager, ResourceManager resourceManager) {
        switch(vdsManager.getVdsType()) {
        case KubevirtNode:
            return new KubevirtVmStatsRefresher(vdsManager);
        default:
            return new EventVmStatsRefresher(vdsManager, resourceManager);
        }
    }
}
