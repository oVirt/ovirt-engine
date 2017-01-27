package org.ovirt.engine.core.bll.host;

import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.hostdeploy.VdsDeploy;
import org.ovirt.engine.core.bll.hostdeploy.VdsDeployPackagesUnit;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.HostUpgradeManagerResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HostUpgradeManager implements UpdateAvailable, Updateable {

    private static Logger log = LoggerFactory.getLogger(HostUpgradeManager.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Override
    public HostUpgradeManagerResult checkForUpdates(final VDS host) {
        Collection<String> packages = getPackagesForCheckUpdate(host.getVdsType(),
                host.getClusterCompatibilityVersion());
        try (final VdsDeploy hostPackagesManager = createPackagesManager(host, false)) {
            VdsDeployPackagesUnit unit = new VdsDeployPackagesUnit(packages, true);
            hostPackagesManager.addUnit(unit);
            hostPackagesManager.execute();
            Collection<String> availablePackages = unit.getUpdates();
            boolean updatesAvailable = !availablePackages.isEmpty();
            HostUpgradeManagerResult hostUpgradeManagerResult = new HostUpgradeManagerResult();
            hostUpgradeManagerResult.setUpdatesAvailable(updatesAvailable);
            if (updatesAvailable) {
                hostUpgradeManagerResult.setAvailablePackages(availablePackages);
                log.info("There are available packages ({}) for host '{}'",
                        StringUtils.join(availablePackages, ", "),
                        host.getName());

                AuditLogableBase auditLog = Injector.injectMembers(new AuditLogableBase());
                auditLog.setVds(host);
                Set<String> relevantPackages = filterPackages(packages, availablePackages);
                if (relevantPackages.isEmpty()) {
                    auditLogDirector.log(auditLog, AuditLogType.HOST_UPDATES_ARE_AVAILABLE);
                } else {
                    auditLog.addCustomValue("Packages", StringUtils.join(relevantPackages, ","));
                    auditLogDirector.log(auditLog, AuditLogType.HOST_UPDATES_ARE_AVAILABLE_WITH_PACKAGES);
                }
            }
            return hostUpgradeManagerResult;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Set<String> filterPackages(Collection<String> queriedPackages, Collection<String> reportedPackages) {
        Set<String> filtered = new TreeSet<>();
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
    public EnumSet<VDSType> getHostTypes() {
        return EnumSet.of(VDSType.VDS, VDSType.oVirtNode);
    }

    @Override
    public void update(final VDS host) {
        Collection<String> packages = getPackagesForCheckUpdate(host.getVdsType(),
                host.getClusterCompatibilityVersion());
        try (final VdsDeploy hostPackagesManager = createPackagesManager(host, true)) {
            hostPackagesManager.addUnit(new VdsDeployPackagesUnit(packages, false));
            hostPackagesManager.execute();
        } catch (final Exception e) {
            log.error("Failed to update host '{}' packages '{}': {}",
                 host.getName(),
                 StringUtils.join(packages, ", "),
                 e.getMessage());
            log.debug("Exception", e);

            throw new RuntimeException(e);
        }
    }

    private VdsDeploy createPackagesManager(final VDS host, boolean alertLog) throws KeyStoreException {
        final VdsDeploy hostPackagesManager = new VdsDeploy("ovirt-host-mgmt", host, alertLog);
        hostPackagesManager.useDefaultKeyPair();
        hostPackagesManager.setCorrelationId(CorrelationIdTracker.getCorrelationId());
        return hostPackagesManager;
    }

    protected static Collection<String> getPackagesForCheckUpdate(VDSType hostType, Version version) {
        List<String> systemPackages = new ArrayList<>();
        List<String> userPackages = new ArrayList<>();
        if (hostType == VDSType.VDS) {
            systemPackages = Config.getValue(ConfigValues.PackageNamesForCheckUpdate, version.toString());
            userPackages = Config.getValue(ConfigValues.UserPackageNamesForCheckUpdate);
        } else if (hostType == VDSType.oVirtNode) {
            systemPackages = Config.getValue(ConfigValues.OvirtNodePackageNamesForCheckUpdate);
        }

        return Stream.concat(systemPackages.stream(),
                userPackages.stream().filter(StringUtils::isNotEmpty)).collect(Collectors.toSet());
    }
}
