package org.ovirt.engine.core.bll.host;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.bll.network.host.HostSetupNetworkPoller;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostConnectivityChecker {

    private static Logger log = LoggerFactory.getLogger(HostConnectivityChecker.class);
    private static final long VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS = 120;
    private static final long VDSM_RESPONSIVENESS_PERIOD_IN_NANOS =
            TimeUnit.SECONDS.toNanos(VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS);

    public boolean check(final VDS host) {
        HostSetupNetworkPoller poller = new HostSetupNetworkPoller();
        final long startTime = System.nanoTime();
        while (System.nanoTime() - startTime < VDSM_RESPONSIVENESS_PERIOD_IN_NANOS) {
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
