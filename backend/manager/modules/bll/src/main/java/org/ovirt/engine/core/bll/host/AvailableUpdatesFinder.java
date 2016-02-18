package org.ovirt.engine.core.bll.host;

import java.util.EnumSet;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AvailableUpdatesFinder {

    private static Logger log = LoggerFactory.getLogger(AvailableUpdatesFinder.class);

    @Inject
    private Instance<UpdateAvailable> hostUpdaters;

    private AvailableUpdatesFinder() {
    }

    public boolean isUpdateAvailable(VDS host) {
        return create(host.getVdsType()).isUpdateAvailable(host);
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
