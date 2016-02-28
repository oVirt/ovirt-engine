package org.ovirt.engine.core.vdsbroker.monitoring;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.VdsManager;


@Singleton
public class RefresherFactory {

    public VmStatsRefresher create(VdsManager vdsManager) {
        return Injector.injectMembers(getRefresherForVds(vdsManager));
    }

    private VmStatsRefresher getRefresherForVds(VdsManager vdsManager) {
        Version version = vdsManager.getCompatibilityVersion();
        VDS vds = vdsManager.getCopyVds();
        if (VdsProtocol.STOMP == vds.getProtocol()
                && FeatureSupported.vmStatsEvents(version)
                && FeatureSupported.events(version)) {
            return new EventVmStatsRefresher(vdsManager);
        }
        return new PollListAndAllVmStatsRefresher(vdsManager);
    }
}
