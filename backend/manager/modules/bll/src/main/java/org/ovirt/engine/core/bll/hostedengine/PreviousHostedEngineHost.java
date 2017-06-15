package org.ovirt.engine.core.bll.hostedengine;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It stores information about the host the hosted engine VM was running on prior to engine restart.
 * It is initialized after Backend but before other services (especially monitoring) so load previous host id from db.
 *
 * TODO: Reexamine this class during 4.0 fencing refactoring
 */
@Singleton
public class PreviousHostedEngineHost implements BackendService {
    private static final Logger log = LoggerFactory.getLogger(PreviousHostedEngineHost.class);

    private Guid previousHostId;

    @Inject
    private VmDao vmDao;

    @PostConstruct
    public void create() {
        List<VM> vms = vmDao.getVmsByOrigins(
                Arrays.asList(
                        OriginType.HOSTED_ENGINE,
                        OriginType.MANAGED_HOSTED_ENGINE));

        if (vms != null && !vms.isEmpty()) {
            previousHostId = vms.iterator().next().getRunOnVds();
        }
        log.debug("Hosted engine VM was running prior to restart on host '{}'", previousHostId);
    }

    public boolean isPreviousHostId(Guid hostId) {
        return previousHostId != null &&
                previousHostId.equals(hostId);
    }
}
