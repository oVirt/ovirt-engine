package org.ovirt.engine.core.bll.pm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.FenceExecutor;
import org.ovirt.engine.core.bll.RestartVdsCommand;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgentOrder;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for checking PM enabled hosts by sending a status command to each host configured PM agent cards and
 * raise alerts for failed operations. .
 */
public class PmHealthCheckManager {

    private static final Logger log = LoggerFactory.getLogger(PmHealthCheckManager.class);
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
                            boolean hasSecondary = (host.getPmSecondaryIp() != null && !host.getPmSecondaryIp().isEmpty());
                            boolean isConcurrent = host.isPmSecondaryConcurrent();
                            FenceExecutor executor = new FenceExecutor(host, FenceActionType.Status);
                            if (executor.findProxyHost() && executor.fence(FenceAgentOrder.Primary).getSucceeded()) {
                                removeAlarm(host.getId(), isConcurrent, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_FAILED_FOR_CON_PRIMARY_AGENT, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_FAILED_FOR_SEQ_PRIMARY_AGENT);
                                if (hasSecondary) {
                                    if (executor.findProxyHost() && executor.fence(FenceAgentOrder.Secondary).getSucceeded()) {
                                        removeAlarm(host.getId(), isConcurrent, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_FAILED_FOR_CON_SECONDARY_AGENT, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_FAILED_FOR_SEQ_SECONDARY_AGENT);
                                    } else {
                                        addAlarm(host.getId(), isConcurrent, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_FAILED_FOR_CON_SECONDARY_AGENT, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_FAILED_FOR_SEQ_SECONDARY_AGENT);
                                    }
                                }
                            } else {
                                addAlarm(host.getId(), isConcurrent, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_FAILED_FOR_CON_PRIMARY_AGENT, AuditLogType.VDS_ALERT_PM_HEALTH_CHECK_FAILED_FOR_SEQ_PRIMARY_AGENT);
                            }
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

    private void addAlarm(Guid hostId, boolean isConcurrent, AuditLogType conAlert, AuditLogType seqAlert) {
        if (isConcurrent) {
            AlertDirector.AddVdsAlert(hostId, conAlert);
        }
        else {
            AlertDirector.AddVdsAlert(hostId, seqAlert);
        }
    }

    private void removeAlarm(Guid hostId, boolean isConcurrent, AuditLogType conAlert, AuditLogType seqAlert) {
        if (isConcurrent) {
            AlertDirector.RemoveVdsAlert(hostId, conAlert);
        }
        else {
            AlertDirector.RemoveVdsAlert(hostId, seqAlert);
        }
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
        VDSReturnValue returnValue = null;
        for (VDS host : hostWithPMInStatusReboot) {
            RestartVdsCommand restartVdsCommand = new RestartVdsCommand(new
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
}
