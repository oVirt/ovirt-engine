package org.ovirt.engine.core.bll.scheduling;

import static org.ovirt.engine.core.common.config.ConfigValues.VdsRefreshRate;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.VdsEventListener;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.monitoring.HostMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some commands e.g RunVm may run as a bulk and performs logic to <br>
 * count and reserve memory and CPU to assure there are both enough resources to complete them<br>
 * (i.e run a VM on a selected host) and that we don't exceed those for the subsequent executions.<br>
 * Moreover bulk operations may cause a pick in VDSM resources utilization and the engine can regulate <br>
 * the pace to enable <br>
 * Successful end of the operations.
 */
@Singleton
public class RunVmDelayer {

    private static final Logger log = LoggerFactory.getLogger(RunVmDelayer.class);

    @Inject
    private ResourceManager resourceManager;

    /**
     * throttle bulk run of VMs by waiting for the update of run-time to kick in and fire <br>
     * the DecreasePendingVms event.
     * @see VdsEventListener
     * @see HostMonitoring
     */
    public void delay(Guid vdsId) {
        log.debug("Try to wait for te engine update the host memory and cpu stats");

        try {
            // time out waiting for an update is the highest between the refresh rate and the last update elapsed time
            // but still no higher than a configurable max to prevent very long updates to stall command.
            long t = Math.max(
                    resourceManager.getVdsManager(vdsId).getLastUpdateElapsed(),
                    TimeUnit.SECONDS.toMillis(Config.<Long> getValue(VdsRefreshRate)));
            t = Math.min(Config.<Integer> getValue(ConfigValues.ThrottlerMaxWaitForVdsUpdateInMillis), t);

            // wait for the run-time refresh to decrease any current powering-up VMs
            BlockingQueue<Boolean> queue = resourceManager.getVdsManager(vdsId).getVdsMonitor().getQueue();
            queue.poll(t, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public void delay(List<Guid> vdsIds) {
        if (vdsIds.isEmpty()) {
            return;
        }

        log.debug("Try to wait for the engine to update memory and cpu stats");

        long maxUpdateElapsed = vdsIds.stream()
                .mapToLong(vdsId -> resourceManager.getVdsManager(vdsId).getLastUpdateElapsed())
                .max().getAsLong();

        long maxWaitTime = Math.min(
                Math.max(maxUpdateElapsed,
                        TimeUnit.SECONDS.toMillis(Config.<Long> getValue(VdsRefreshRate))),
                Config.<Integer> getValue(ConfigValues.ThrottlerMaxWaitForVdsUpdateInMillis));

        long endTime = System.currentTimeMillis() + maxWaitTime;

        // Wait on all queues sequentially
        for (Guid vdsId : vdsIds) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= endTime) {
                break;
            }

            try {
                // wait for the run-time refresh to decrease any current powering-up VMs
                BlockingQueue<Boolean> queue = resourceManager.getVdsManager(vdsId).getVdsMonitor().getQueue();
                queue.poll(endTime - currentTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
