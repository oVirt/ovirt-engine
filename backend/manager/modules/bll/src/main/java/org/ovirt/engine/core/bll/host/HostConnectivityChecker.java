package org.ovirt.engine.core.bll.host;


import static org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters.PollTechnique.POLL;
import static org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters.PollTechnique.POLL2;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.bll.network.host.HostPoller;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters.PollTechnique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostConnectivityChecker {

    private static Logger log = LoggerFactory.getLogger(HostConnectivityChecker.class);
    private static final long VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS = 120;
    private static final long VDSM_RESPONSIVENESS_PERIOD_IN_NANOS =
            TimeUnit.SECONDS.toNanos(VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS);

    public boolean check(final VDS host) {
        PollTechnique pollTechnique = FeatureSupported.isPing2SupportedByVdsm(
                host.getClusterCompatibilityVersion())? POLL2 : POLL;
        HostPoller poller = new HostPoller(new TimeBoundPollVDSCommandParameters(host.getId(), pollTechnique));
        final long startTime = System.nanoTime();
        while (System.nanoTime() - startTime < VDSM_RESPONSIVENESS_PERIOD_IN_NANOS) {
            if (poller.poll()) {
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
