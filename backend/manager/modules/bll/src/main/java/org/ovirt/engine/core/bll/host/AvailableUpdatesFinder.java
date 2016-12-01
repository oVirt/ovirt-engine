package org.ovirt.engine.core.bll.host;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.HostUpgradeManagerResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AvailableUpdatesFinder {

    private static Logger log = LoggerFactory.getLogger(AvailableUpdatesFinder.class);

    @Inject
    private Instance<UpdateAvailable> hostUpdaters;

    private ConcurrentMap<Guid, Boolean> upgradeCheckInProgressMap = new ConcurrentHashMap<>();

    private AvailableUpdatesFinder() {
    }

    public HostUpgradeManagerResult checkForUpdates(VDS host) {
        if (upgradeCheckInProgressMap.getOrDefault(host.getId(), false)) {
            String error = String.format(
                    "Failed to refresh host '%s' packages availability, another refresh process is already running.",
                    host.getName());
            throw new IllegalStateException(error);
        }
        try {
            upgradeCheckInProgressMap.put(host.getId(), true);
            return create(host.getVdsType()).checkForUpdates(host);
        } finally {
            upgradeCheckInProgressMap.remove(host.getId());
        }
    }

    private UpdateAvailable create(VDSType hostType) {
        for (UpdateAvailable hostUpdater : hostUpdaters) {
            EnumSet<VDSType> hostTypes = hostUpdater.getHostTypes();
            if (hostTypes.contains(hostType)) {
                return hostUpdater;
            }
        }

        log.error("Cannot instantiate host available strategy for unknown host type '{}'", hostType);
        throw new RuntimeException("Cannot instantiate host available strategy for unknown host type");
    }
}
