package org.ovirt.engine.core.bll;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.dao.ClusterDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ClusterUpgradeRunningCleanupManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(ClusterUpgradeRunningCleanupManager.class);

    @Inject
    private ClusterDao clusterDao;

    @PostConstruct
    private void init() {
        cleanup();
    }

    public void cleanup() {
        try {
            log.debug("Start cleanup of upgrade running flag");
            clusterDao.clearAllUpgradeRunning();
            log.debug("Finished cleanup of upgrade running flag");
        } catch (Throwable t) {
            log.error("Exception in performing cluster upgrade running cleanup: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

}
