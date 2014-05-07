package org.ovirt.engine.core.bll.pm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.FenceExecutor;
import org.ovirt.engine.core.bll.RestartVdsCommand;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.pm.PowerManagementHelper.AgentsIterator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
//import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Responsible for checking PM enabled hosts by sending a status command to each host configured PM agent cards and
 * raise alerts for failed operations.
 */
public class PmHealthCheckManager {

    private static final Logger log = LoggerFactory.getLogger(PmHealthCheckManager.class);
    // private static final String AGENT_ID = "AgentId";
    private static final PmHealthCheckManager instance = new PmHealthCheckManager();
    private boolean active = false;

    private PmHealthCheckManager() {
        // intentionally empty
    }

    /**
     * Initializes the PM Health Check Manager
     */
    public void initialize() {
        if(Config.<Boolean>getValue(ConfigValues.PMHealthCheckEnabled)) {
            log.info("Start initializing {}", getClass().getSimpleName());
            Integer pmHealthCheckInterval = Config.<Integer> getValue(ConfigValues.PMHealthCheckIntervalInSec);
            SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this,
                    "pmHealthCheck",
                    new Class[]{},
                    new Object[]{},
                    pmHealthCheckInterval,
                    pmHealthCheckInterval,
                    TimeUnit.SECONDS);
            log.info("Finished initializing {}", getClass().getSimpleName());
        }
    }

    @OnTimerMethodAnnotation("pmHealthCheck")
    public void pmHealthCheck() {
        // skip PM health check if previous operation is not completed yet
        if (!active) {
            try {
                synchronized (instance) {
                    log.info("Power Management Health Check started.");
                    active = true;
                    List<VDS> hosts = DbFacade.getInstance().getVdsDao().getAll();
                    for (VDS host : hosts) {
                        if (host.getpm_enabled()) {
                            // check health
                            PmHealth pmHealth = checkPMHealth(host);
                            // handle alerts - adding or canceling as necessary
                            handleAlerts(pmHealth);
                            log.debug(pmHealth.toString());
                        }
                    }
                    log.info("Power Management Health Check completed.");
                }
            }
            finally {
                active = false;
            }
        }
    }

    /**
     * Collect health-status info for all agents.
     */
    private PmHealth checkPMHealth(VDS host) {
        PmHealth pmHealth = new PmHealth(host);
        AgentsIterator iterator = PowerManagementHelper.getAgentsIterator(host.getFenceAgents());

        // In each step of the loop deal with the agents with the next 'order' (one or more). Write info into PmHealth.
        while (iterator.hasNext()) {
            collectHealthStatus(pmHealth, iterator.next());
        }
        return pmHealth;
    }

    private void handleAlerts(PmHealth healthStatus) {
        Guid hostId = healthStatus.getHost().getId();
        // TODO: uncomment pending implementation of removing alerts by agent-id.
        // for (Entry<FenceAgent, Boolean> entry : healthStatus.getHealthMap().entrySet()) {
            // handleAgentAlerts(entry, hostId);
        // }
        handleStartAlerts(healthStatus, hostId);
        handleStopAlerts(healthStatus, hostId);
    }

    // private void handleAgentAlerts(Entry<FenceAgent, Boolean> entry, Guid hostId) {
    // Guid agentId = entry.getKey().getId();
    // boolean agentHealthy = entry.getValue();
    // if (agentHealthy) {
    // removeAlert(agentId, hostId, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_FENCE_AGENT_NON_RESPONSIVE);
    // } else {
    // addAlert(agentId, hostId, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_FENCE_AGENT_NON_RESPONSIVE);
    // }
    //
    // }

    // private void addAlert(Guid agentId, Guid hostId, AuditLogType auditMessage) {
    // AuditLogableBase alert = new AuditLogableBase();
    // alert.addCustomValue(AGENT_ID, agentId.toString());
    // AlertDirector.AddVdsAlert(hostId, auditMessage, alert);
    // }
    //
    // private void removeAlert(Guid agentId, Guid hostId, AuditLogType auditMessage) {
    // AuditLogableBase alert = new AuditLogableBase();
    // alert.addCustomValue(AGENT_ID, agentId.toString());
    // AlertDirector.RemoveVdsAlert(hostId, auditMessage, alert);
    // }

    private void handleStartAlerts(PmHealth healthStatus, Guid hostId) {
        if (healthStatus.isStartShouldWork()) {
            removeAlert(hostId, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_START_MIGHT_FAIL);
        } else {
            addAlert(hostId, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_START_MIGHT_FAIL);
        }
    }

    private void handleStopAlerts(PmHealth healthStatus, Guid hostId) {
        if (healthStatus.isStopShouldWork()) {
            removeAlert(hostId, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_STOP_MIGHT_FAIL);
        } else {
            addAlert(hostId, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_STOP_MIGHT_FAIL);
        }
    }

    private void removeAlert(Guid hostId, AuditLogType auditMessage) {
        AlertDirector.RemoveVdsAlert(hostId, auditMessage);
    }

    private void addAlert(Guid hostId, AuditLogType auditMessage) {
        AlertDirector.AddVdsAlert(hostId, auditMessage);
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

    private boolean isHealthy(FenceAgent agent, VDS host) {
        return new FenceExecutor(host).fence(FenceActionType.Status, agent).getSucceeded();
    }

    private void waitUntilFencingAllowed() {
        // wait the quiet time from engine start in which we skip fencing operations
        int mSecToWait = Config.<Integer>getValue(ConfigValues.DisableFenceAtStartupInSec) * 1000;
        ThreadUtils.sleep(mSecToWait);
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
    }

    private void startHostsWithPMInReboot(List<VDS> hosts) {
        final List<VDS> hostsWithPMInReboot = LinqUtils.filter(hosts,
                new Predicate<VDS>() {
                    @Override
                    public boolean eval(VDS host) {
                        return (host.getpm_enabled() && host.getStatus() == VDSStatus.Reboot);
                    }
                });
        if (hostsWithPMInReboot.size() > 0) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    waitUntilFencingAllowed();
                    startHosts(hostsWithPMInReboot);
                }
            });
        }
    }

    /**
     * This method starts hosts remained in off status because of the following flow
     * non-responding -> stop -> wait -> off -> engine restart
     * Such hosts will stay DOWN while its status will show Reboot
     * We should try to catch such hosts and attempt to restart it.
     * @param hostWithPMInStatusReboot
     */
    public void startHosts(List<VDS> hostWithPMInStatusReboot) {
        for (VDS host : hostWithPMInStatusReboot) {
            RestartVdsCommand<FenceVdsActionParameters> restartVdsCommand =
                    new RestartVdsCommand<FenceVdsActionParameters>(new
                    FenceVdsActionParameters(host.getId(), FenceActionType.Status));
            if (restartVdsCommand.isPmReportsStatusDown()) {
                VdcReturnValueBase retValue = Backend.getInstance().runInternalAction(VdcActionType.RestartVds, restartVdsCommand.getParameters());
                if (retValue!= null && retValue.getSucceeded()) {
                    log.info("Host '{}' was started successfully by PM Health Check Manager",
                            host.getName());
                }
                else {
                    log.info("PM Health Check Manager failed to start Host '{}'", host.getName());
                }
            }
        }
    }

    private void recoverKdumpingHosts(List<VDS> hosts) {
        final List<VDS> kdumpingHosts = new ArrayList<>();
        for (VDS host : hosts) {
            if (host.getStatus() == VDSStatus.Kdumping) {
                kdumpingHosts.add(host);
            }
        }
        if (!kdumpingHosts.isEmpty()) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    waitUntilFencingAllowed();
                    executeNotRespondingTreatment(kdumpingHosts);
                }
            });
        }
    }

    private void executeNotRespondingTreatment(List<VDS> hosts) {
        for (VDS host : hosts) {
            final FenceVdsActionParameters params = new FenceVdsActionParameters(
                    host.getId(),
                    FenceActionType.Restart
            );
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    Backend.getInstance().runInternalAction(
                            VdcActionType.VdsNotRespondingTreatment,
                            params,
                            ExecutionHandler.createInternalJobContext()
                    );
                }
            });
        }
    }

    public static PmHealthCheckManager getInstance() {
        return instance;
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
