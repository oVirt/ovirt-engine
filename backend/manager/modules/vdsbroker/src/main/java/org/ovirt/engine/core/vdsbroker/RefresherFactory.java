package org.ovirt.engine.core.vdsbroker;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.jsonrpc.EventVMStatsRefresher;


@Singleton
public class RefresherFactory {

    public VMStatsRefresher create(VdsManager vdsManager) {
        return Injector.injectMembers(getRefresherForVds(vdsManager));
    }

    private VMStatsRefresher getRefresherForVds(VdsManager vdsManager) {
        Version version = vdsManager.getCompatibilityVersion();
        VDS vds = vdsManager.getCopyVds();
        if (FeatureSupported.jsonProtocol(version)
                && VdsProtocol.STOMP == vds.getProtocol()
                && FeatureSupported.vmStatsEvents(version)) {
            return new EventVMStatsRefresher(vdsManager);
        }
        return new PollListAndAllVmStatsRefresher(vdsManager);
    }
}
