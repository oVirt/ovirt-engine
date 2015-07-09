package org.ovirt.engine.core.bll.host;

import java.security.KeyStoreException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.hostdeploy.VdsMgmtPackages;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HostUpgradeManager implements UpdateAvailable, Updateable {

    private static Logger log = LoggerFactory.getLogger(HostUpgradeManager.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Override
    public boolean isUpdateAvailable(final VDS host) {
        final Collection<String> packages = Config.getPackagesForCheckUpdate();

        try (final VdsMgmtPackages hostPackagesManager = createPackagesManager(host, true)) {
            hostPackagesManager.setPackages(packages);
            hostPackagesManager.execute();

            Collection<String> availablePackages = hostPackagesManager.getUpdates();
            boolean updatesAvailable = !availablePackages.isEmpty();
            if (updatesAvailable) {
                log.info("There are available packages ({}) for host '{}'",
                        StringUtils.join(availablePackages, ", "),
                        host.getName());

                AuditLogableBase auditLog = new AuditLogableBase();
                auditLog.setVds(host);
                auditLog.addCustomValue("Packages", StringUtils.join(filterPackages(packages, availablePackages), ","));
                auditLogDirector.log(auditLog, AuditLogType.HOST_UPDATES_ARE_AVAILABLE);
            }
            return updatesAvailable;
        } catch (final Exception e) {
            log.error("Failed to refresh host '{}' packages '{}' availability.",
                    host.getName(),
                    StringUtils.join(packages, ", "));
            log.error("Exception", e);

            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Set<String> filterPackages(Collection<String> queriedPackages, Collection<String> reportedPackages) {
        Set<String> filtered = new HashSet<>();
        for (String reportedPackage : reportedPackages) {
            for (String queriedPackage : queriedPackages) {
                if (StringUtils.startsWith(reportedPackage, queriedPackage)) {
                    filtered.add(queriedPackage);
                }
            }
        }

        return filtered;
    }

    @Override
    public VDSType getHostType() {
        return VDSType.VDS;
    }

    @Override
    public void update(final VDS host) {
        final Collection<String> packages = Config.getPackagesForCheckUpdate();

        try (final VdsMgmtPackages hostPackagesManager = createPackagesManager(host, false)) {
            hostPackagesManager.setPackages(packages);
            hostPackagesManager.execute();
        } catch (final Exception e) {
            log.error("Failed to update host '{}' packages '{}'.", host.getName(), StringUtils.join(packages, ", "));
            log.error("Exception", e);

            throw new RuntimeException(e);
        }
    }

    private VdsMgmtPackages createPackagesManager(final VDS host, final boolean checkOnly) throws KeyStoreException {
        final VdsMgmtPackages hostPackagesManager = new VdsMgmtPackages(host, checkOnly);

        String correlationId = CorrelationIdTracker.getCorrelationId();
        hostPackagesManager.setCorrelationId(correlationId);
        if (StringUtils.isEmpty(correlationId)) {
            correlationId = LoggedUtils.getObjectId(host);
        }

        hostPackagesManager.setCorrelationId(correlationId);
        hostPackagesManager.useDefaultKeyPair();
        return hostPackagesManager;
    }
}
