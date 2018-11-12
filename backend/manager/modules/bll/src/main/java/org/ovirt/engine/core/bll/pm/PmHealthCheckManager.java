package org.ovirt.engine.core.bll.pm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.pm.PowerManagementHelper.AgentsIterator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for checking PM enabled hosts by sending a status command to each host configured PM agent cards and
 * raise alerts for failed operations.
 */
@Singleton
public class PmHealthCheckManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(PmHealthCheckManager.class);
    private Lock lock = new ReentrantLock();
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private FenceAgentDao fenceAgentDao;
    @Inject
    private AlertDirector alertDirector;
    @Inject
    private Instance<BackendInternal> backend;
    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    /**
     * Initializes the PM Health Check Manager
     */
    @PostConstruct
    private void initialize() {
        if(Config.<Boolean>getValue(ConfigValues.PMHealthCheckEnabled)) {
            log.info("Start initializing {}", getClass().getSimpleName());
            long pmHealthCheckInterval = Config.<Long> getValue(ConfigValues.PMHealthCheckIntervalInSec);
            executor.scheduleWithFixedDelay(this::pmHealthCheck,
                    pmHealthCheckInterval,
                    pmHealthCheckInterval,
                    TimeUnit.SECONDS);
        }
        // recover from engine failure
        recover(vdsDao.getAll());
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    private void pmHealthCheck() {
        try {
            // skip PM health check if previous operation is not completed yet
            if (lock.tryLock()) {
                try {
                    log.info("Power Management Health Check started.");
                    List<VDS> hosts = vdsDao.getAll();
                    for (VDS host : hosts) {
                        if (host.isPmEnabled()) {
                            pmHealthCheck(host);
                        }
                    }
                    log.info("Power Management Health Check completed.");
                } finally {
                    lock.unlock();
                }
            }
        } catch (Throwable t) {
            log.error("Exception in checking PM health: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    /**
     * Check PM health of a host. Add/Remove alerts as necessary, and log the results.
     */
    public void pmHealthCheck(VDS host) {
        // check health
        PmHealth pmHealth = checkPMHealth(host);
        // handle alerts - adding or canceling as necessary
        handleAlerts(pmHealth);
        log.debug(pmHealth.toString());
    }

    /**
     * Check PM health of a host. Add/Remove alerts as necessary, and log the results.
     */
    public void pmHealthCheck(Guid hostId) {
        VDS host = vdsDao.get(hostId);
        pmHealthCheck(host);
    }


    /**
     * Collect health-status info for all agents.
     */
    private PmHealth checkPMHealth(VDS host) {
        PmHealth pmHealth = new PmHealth(host);
        AgentsIterator iterator = PowerManagementHelper.getAgentsIterator(
                fenceAgentDao.getFenceAgentsForHost(host.getId()));

        // In each step of the loop deal with the agents with the next 'order' (one or more). Write info into PmHealth.
        while (iterator.hasNext()) {
            collectHealthStatus(pmHealth, iterator.next());
        }
        return pmHealth;
    }

    private void handleAlerts(PmHealth healthStatus) {
        VDS host = healthStatus.getHost();
        // TODO: uncomment pending implementation of removing alerts by agent-id.
        // for (Entry<FenceAgent, Boolean> entry : healthStatus.getHealthMap().entrySet()) {
            // handleAgentAlerts(entry, hostId);
        // }
        handleStartAlerts(healthStatus, host);
        handleStopAlerts(healthStatus, host);
    }

    private void handleStartAlerts(PmHealth healthStatus, VDS host) {
        if (healthStatus.isStartShouldWork()) {
            removeAlert(host.getId(), AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_START_MIGHT_FAIL);
        } else {
            addAlert(host, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_START_MIGHT_FAIL);
        }
    }

    private void handleStopAlerts(PmHealth healthStatus, VDS host) {
        if (healthStatus.isStopShouldWork()) {
            removeAlert(host.getId(), AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_STOP_MIGHT_FAIL);
        } else {
            addAlert(host, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_STOP_MIGHT_FAIL);
        }
    }

    private void removeAlert(Guid hostId, AuditLogType auditMessage) {
        alertDirector.removeVdsAlert(hostId, auditMessage);
    }

    private void addAlert(VDS host, AuditLogType auditMessage) {
        AuditLogable alert = new AuditLogableImpl();
        alert.setVdsId(host.getId());
        alert.setVdsName(host.getName());
        alert.setClusterId(host.getClusterId());
        alert.setClusterName(host.getClusterName());
        auditLogDirector.log(alert, auditMessage);
    }

    /**
     * A step in the health-status check. Checks health of the provided agents.
     */
    private void collectHealthStatus(PmHealth healthStatus, List<FenceAgent> agents) {
        boolean atLeastOneHealthy = false; // initialize to false, and if one healthy agent found, change to true.
        boolean allHealthy = true; // initialize to true, and if one unhealthy agent found, change to false.
        for (FenceAgent agent : agents) {
            if (isHealthy(agent, healthStatus.getHost())) {
                healthStatus.getHealthMap().put(agent, true);
                atLeastOneHealthy = true;
            } else {
                healthStatus.getHealthMap().put(agent, false);
                allHealthy = false;
            }
        }
        if (atLeastOneHealthy) {
            healthStatus.setStartShouldWork(true);
        }
        if (allHealthy) {
            healthStatus.setStopShouldWork(true);
        }
    }

    /**
     * Checks if the agent is healthy. A healthy agent is one that returns an answer when queries for status, and it
     * doesn't matter whether that answer is "on" or "off".
     */
    private boolean isHealthy(FenceAgent agent, VDS host) {
        return new HostFenceActionExecutor(host).getFenceAgentStatus(agent).getStatus() == Status.SUCCESS;
    }

    private void waitUntilFencingAllowed() {
        // wait the quiet time from engine start in which we skip fencing operations
        ThreadUtils.sleep(
                TimeUnit.SECONDS.toMillis(
                        Config.<Integer>getValue(ConfigValues.DisableFenceAtStartupInSec)));
    }

    /**
     * Recovers hosts with status Reboot or Kdumping from engine crash
     *
     * @param hosts
     *            all existing hosts
     */
    public void recover(List<VDS> hosts) {
        startHostsWithPMInReboot(hosts);
        recoverKdumpingHosts(hosts);
        recoverNonRespondingHosts(hosts);
    }

    private void startHostsWithPMInReboot(List<VDS> hosts) {
        final List<VDS> hostsWithPMInReboot = hosts.stream()
                .filter(host -> host.isPmEnabled())
                .filter(host -> host.getStatus() == VDSStatus.Reboot)
                .collect(Collectors.toList());
        if (hostsWithPMInReboot.size() > 0) {
            ThreadPoolUtil.execute(() -> {
                waitUntilFencingAllowed();
                startHosts(hostsWithPMInReboot);
            });
        }
    }

    private void recoverNonRespondingHosts(List<VDS> hosts) {
        // Loop on all hosts, PM enabled flag and status are checked after quite period
        if (hosts.size() > 0) {
            ThreadPoolUtil.execute(() -> {
                waitUntilFencingAllowed();
                fenceHosts(hosts);
            });
        }
    }

    /**
     * This method starts hosts remained in off status because of the following flow
     * non-responding -> stop -> wait -> off -> engine restart
     * Such hosts will stay DOWN while its status will show Reboot
     * We should try to catch such hosts and attempt to restart it.
     */
    public void startHosts(List<VDS> hostWithPMInStatusReboot) {
        for (VDS host : hostWithPMInStatusReboot) {
            RestartVdsCommand<FenceVdsActionParameters> restartVdsCommand =
                    new RestartVdsCommand<>(new
                            FenceVdsActionParameters(host.getId()), null);
            if (new HostFenceActionExecutor(host).isHostPoweredOff()) {
                //if an external-status other than OK has been set on the host,
                //that is considered an indication not to perform automatic
                //power-management operations on the host.
                if (!ExternalStatus.Ok.equals(host.getExternalStatus())) {
                    log.warn("Host '{}' ({}) was not started by PM Health Check Manager becuse it has external-status: '{}'.",
                            host.getName(),
                            host.getId(),
                            host.getExternalStatus());
                    continue;
                }
                ActionReturnValue
                        retValue = backend.get().runInternalAction(ActionType.RestartVds, restartVdsCommand.getParameters());
                if (retValue!= null && retValue.getSucceeded()) {
                    log.info("Host '{}' was started successfully by PM Health Check Manager",
                            host.getName());
                } else {
                    log.info("PM Health Check Manager failed to start Host '{}'", host.getName());
                }
            }
        }
    }

    /**
     * This method calls the non-responding treatment command
     * if host that was non-responding in the quite-time in which
     * fencing is skipped is still non-responding after the quite-time
     * period passed.
     */
    public void fenceHosts(List<VDS> hosts) {
        for (VDS host : hosts) {
            // retrieve the current status from DB
            host = vdsDao.get(host.getId());
            // Check if host exists, has power management and is still in non-responsive status after quite time.
            if (host != null && host.isPmEnabled() && host.getStatus() == VDSStatus.NonResponsive) {
                VdsNotRespondingTreatmentCommand<FenceVdsActionParameters> nonResponingVdsCommand =
                        new VdsNotRespondingTreatmentCommand<>(new
                                FenceVdsActionParameters(host.getId()), null);
                backend.get().runInternalAction(ActionType.VdsNotRespondingTreatment, nonResponingVdsCommand.getParameters());
            }
        }
    }

    private void recoverKdumpingHosts(List<VDS> hosts) {
        final List<VDS> kdumpingHosts = hosts.stream()
                .filter(host -> host.getStatus() == VDSStatus.Kdumping)
                .collect(Collectors.toList());
        if (!kdumpingHosts.isEmpty()) {
            ThreadPoolUtil.execute(() -> {
                waitUntilFencingAllowed();
                executeNotRespondingTreatment(kdumpingHosts);
            });
        }
    }

    private void executeNotRespondingTreatment(List<VDS> hosts) {
        for (VDS host : hosts) {
            ThreadPoolUtil.execute(() -> backend.get().runInternalAction(
                    ActionType.VdsNotRespondingTreatment,
                    new FenceVdsActionParameters(host.getId()),
                    ExecutionHandler.createInternalJobContext()
            ));
        }
    }

    private static class PmHealth {
        public PmHealth(VDS host) {
            super();
            this.host = host;
        }
        private Map<FenceAgent, Boolean> healthMap = new HashMap<>();
        private boolean startShouldWork = false;
        private boolean stopShouldWork = false;
        private VDS host;

        public VDS getHost() {
            return host;
        }

        public Map<FenceAgent, Boolean> getHealthMap() {
            return healthMap;
        }
        public boolean isStartShouldWork() {
            return startShouldWork;
        }
        public void setStartShouldWork(boolean startShouldWork) {
            this.startShouldWork = startShouldWork;
        }
        public boolean isStopShouldWork() {
            return stopShouldWork;
        }
        public void setStopShouldWork(boolean stopShouldWork) {
            this.stopShouldWork = stopShouldWork;
        }

        @Override
        public String toString() {
            StringBuilder sb =
                    new StringBuilder().append("Power-Management Health Status for host ")
                            .append(host.getId())
                            .append(": ");
            sb.append("Using fencing to Start is ");
            if (startShouldWork) {
                sb.append("expected to work (since one or more of the agents are working properly). ");
            } else {
                sb.append("at high risk of failing (since none of the agents are working properly). ");
            }
            sb.append("Using fencing to Stop is ");
            if (stopShouldWork) {
                sb.append("expected to work (since all agents are working properly). ");
            } else {
                sb.append(" at high risk of failing (since one or more of the agents are not working properly). ");
            }
            sb.append("Agent statuses: ");
            for (Entry<FenceAgent, Boolean> entry : healthMap.entrySet()) {
                sb.append(entry.getKey().getId())
                        .append(": ")
                        .append(entry.getValue() ? "Up. " : "Down. ");
            }
            return sb.toString();
        }

    }
}
