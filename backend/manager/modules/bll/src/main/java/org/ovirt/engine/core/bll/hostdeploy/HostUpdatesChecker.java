package org.ovirt.engine.core.bll.hostdeploy;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.host.AvailableUpdatesFinder;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.HostUpgradeManagerResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.di.Injector;
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
        if (!vdsDynamicDao.get(host.getId()).getStatus().isEligibleForOnDemandCheckUpdates()) {
            log.warn("Check for available updates is skipped for host '{}' due to unsupported host status '{}' ",
                    host.getName(),
                    host.getStatus());
            return null;
        }

        HostUpgradeManagerResult updatesResult = null;
        AuditLogableBase auditLog = Injector.injectMembers(new AuditLogableBase());
        auditLog.setVds(host);
        try {
            updatesResult = availableUpdatesFinder.checkForUpdates(host);
            if (updatesResult.isUpdatesAvailable()) {
                String message = updatesResult.getAvailablePackages() == null ? "found updates."
                        : String.format("found updates for packages %s",
                                StringUtils.join(updatesResult.getAvailablePackages(), ", "));
                auditLog.addCustomValue("Message", message);
            } else {
                auditLog.addCustomValue("Message", "no updates found.");
            }
            auditLogDirector.log(auditLog, AuditLogType.HOST_AVAILABLE_UPDATES_FINISHED);
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
            auditLog.addCustomValue("Message", "Another refresh process is already running");
            auditLogDirector.log(auditLog, AuditLogType.HOST_AVAILABLE_UPDATES_FAILED);
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
