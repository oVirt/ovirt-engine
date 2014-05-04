package org.ovirt.engine.core.bll.pm;

import org.ovirt.engine.core.bll.FenceExecutor;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgentOrder;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for checking PM enabled hosts by sending a status command to each host configured PM agent cards and
 * raise alerts for failed operations. .
 */
public class PmHealthCheckManager {

    private static final Log log = LogFactory.getLog(PmHealthCheckManager.class);
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
            log.info("Start initializing " + getClass().getSimpleName());
            Integer pmHealthCheckInterval = Config.<Integer> getValue(ConfigValues.PMHealthCheckIntervalInSec);
            SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this,
                    "pmHealthCheck",
                    new Class[]{},
                    new Object[]{},
                    pmHealthCheckInterval,
                    pmHealthCheckInterval,
                    TimeUnit.SECONDS);
            log.info("Finished initializing " + getClass().getSimpleName());
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

    public static PmHealthCheckManager getInstance() {
        return instance;
    }
}
