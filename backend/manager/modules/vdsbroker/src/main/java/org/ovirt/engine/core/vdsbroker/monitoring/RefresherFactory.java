package org.ovirt.engine.core.vdsbroker.monitoring;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.VdsManager;


@Singleton
public class RefresherFactory {

    public VmStatsRefresher create(VdsManager vdsManager) {
        return Injector.injectMembers(getRefresherForVds(vdsManager));
    }

    private VmStatsRefresher getRefresherForVds(VdsManager vdsManager) {
        VDS vds = vdsManager.getCopyVds();
        if (VdsProtocol.STOMP == vds.getProtocol()) {
            return new EventVmStatsRefresher(vdsManager);
        }
        return new PollListAndAllVmStatsRefresher(vdsManager);
    }
}
