package org.ovirt.engine.core.bll.hostdeploy;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.host.AvailableUpdatesFinder;
import org.ovirt.engine.core.bll.host.HostUpgradeManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.HostUpgradeManagerResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HostUpdatesChecker {

    private static Logger log = LoggerFactory.getLogger(HostUpdatesChecker.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private AvailableUpdatesFinder availableUpdatesFinder;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    @Inject
    private ResourceManager resourceManager;

    public HostUpgradeManagerResult checkForUpdates(VDS host) {
        AuditLogable auditLog = new AuditLogableImpl();
        auditLog.setVdsName(host.getName());
        auditLog.setVdsId(host.getId());
        if (!vdsDynamicDao.get(host.getId()).getStatus().isEligibleForOnDemandCheckUpdates()) {
            log.warn("Check for available updates is skipped for host '{}' due to unsupported host status '{}' ",
                    host.getName(),
                    host.getStatus());
            auditLogDirector.log(auditLog, AuditLogType.HOST_AVAILABLE_UPDATES_SKIPPED_UNSUPPORTED_STATUS);
            return null;
        }

        HostUpgradeManagerResult updatesResult = null;
        try {
            updatesResult = availableUpdatesFinder.checkForUpdates(host);
            if (updatesResult.isUpdatesAvailable()) {
                List<String> availablePackages = updatesResult.getAvailablePackages();
                String message;
                if (availablePackages.size() > HostUpgradeManager.MAX_NUM_OF_DISPLAYED_UPDATES) {
                    message = String.format(
                        "%1$s and %2$s others. To see all packages check engine.log.",
                        StringUtils.join(
                            availablePackages.subList(0, HostUpgradeManager.MAX_NUM_OF_DISPLAYED_UPDATES),
                            ", "
                        ),
                        availablePackages.size() - HostUpgradeManager.MAX_NUM_OF_DISPLAYED_UPDATES
                    );
                } else {
                    message = String.format(
                        "found updates for packages %s",
                        StringUtils.join(updatesResult.getAvailablePackages(), ", ")
                    );
                }
                auditLog.addCustomValue("Message", message);
            } else {
                auditLog.addCustomValue("Message", "no updates found.");
            }
            auditLogDirector.log(auditLog, AuditLogType.HOST_AVAILABLE_UPDATES_FINISHED);
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
            auditLogDirector.log(auditLog, AuditLogType.HOST_AVAILABLE_UPDATES_PROCESS_IS_ALREADY_RUNNING);
        } catch (Exception e) {
            log.error("Failed to check if updates are available for host '{}' with error message '{}'",
                    host.getName(),
                    e.getMessage());
            log.debug("Exception", e);
            auditLog.addCustomValue("Message",
                    StringUtils.defaultString(e.getMessage(), e.getCause() == null ? null : e.getCause().toString()));
            auditLogDirector.log(auditLog, AuditLogType.HOST_AVAILABLE_UPDATES_FAILED);
        }

        if (updatesResult != null && updatesResult.isUpdatesAvailable() != host.isUpdateAvailable()) {
            VdsManager hostManager = resourceManager.getVdsManager(host.getId());
            synchronized (hostManager) {
                hostManager.updateUpdateAvailable(updatesResult.isUpdatesAvailable());
            }
        }

        return updatesResult;
    }
}
