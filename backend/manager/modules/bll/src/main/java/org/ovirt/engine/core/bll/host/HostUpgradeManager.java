package org.ovirt.engine.core.bll.host;

import java.security.KeyStoreException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.hostdeploy.VdsMgmtPackages;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostUpgradeManager implements UpdateAvailable {

    private static Logger log = LoggerFactory.getLogger(HostUpgradeManager.class);

    @Override
    public boolean isUpdateAvailable(final VDS host) {
        final Collection<String> packages = Config.getPackagesForCheckUpdate();

        try (final VdsMgmtPackages hostPackagesManager = createPackagesManager(host, true)) {
            hostPackagesManager.setPackages(packages);
            hostPackagesManager.execute();

            boolean updatesAvailable = !hostPackagesManager.getUpdates().isEmpty();
            if (updatesAvailable) {
                log.info("There are available packages ({}) for host '{}'",
                        StringUtils.join(hostPackagesManager.getUpdates(), ", "),
                        host.getName());
            }

            return updatesAvailable;
        } catch (final Exception e) {
            log.error("Failed to refresh host '{}' packages '{}'.", host.getName(), StringUtils.join(packages, ", "));
            log.error("Exception", e);

            throw new RuntimeException(e.getMessage(), e);
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
