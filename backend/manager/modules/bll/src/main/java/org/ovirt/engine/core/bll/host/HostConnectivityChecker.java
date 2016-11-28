package org.ovirt.engine.core.bll.host;

import org.ovirt.engine.core.bll.network.host.HostSetupNetworkPoller;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostConnectivityChecker {

    private static Logger log = LoggerFactory.getLogger(HostConnectivityChecker.class);
    private static final int VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS = 120;

    public boolean check(final VDS host) {
        final int checks =
                VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS
                        / Config.<Integer> getValue(ConfigValues.SetupNetworksPollingTimeout);
        HostSetupNetworkPoller poller = new HostSetupNetworkPoller();
        for (int i = 0; i < checks; i++) {
            if (poller.poll(host.getId())) {
                log.info("Engine managed to communicate with VDSM agent on host '{}' with address '{}' ('{}')",
                        host.getName(),
                        host.getHostName(),
                        host.getId());
                return true;
            }
        }

        return false;
    }
}
