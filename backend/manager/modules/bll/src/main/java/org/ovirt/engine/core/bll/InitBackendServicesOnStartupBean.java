package org.ovirt.engine.core.bll;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.dwh.DwhHeartBeat;
import org.ovirt.engine.core.bll.gluster.GlusterJobsManager;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.pm.PmHealthCheckManager;
import org.ovirt.engine.core.bll.scheduling.AffinityRulesEnforcementManager;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfDataUpdater;
import org.ovirt.engine.core.bll.storage.pool.StoragePoolStatusHandler;
import org.ovirt.engine.core.bll.tasks.CommandCallbacksPoller;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.utils.exceptions.InitializationException;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The following bean is created in order to initialize and start all related vdsms schedulers
 * in the system after all beans finished initialization
 */
@Singleton
@Startup
@DependsOn({ "Backend"})
public class InitBackendServicesOnStartupBean implements InitBackendServicesOnStartup{

    private static final Logger log = LoggerFactory.getLogger(InitBackendServicesOnStartupBean.class);

    @Inject
    private Instance<BackendService> services;

    @Inject
    private Instance<SchedulingManager> schedulingManagerProvider;

    @Inject
    private SessionDataContainer sessionDataContainer;

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean life cycle.
     */
    @Override
    @PostConstruct
    public void create() {

        try {
            // This must be done before starting to sample the hosts status from VDSM since the sampling will turn such host from Reboot to NonResponsive
            loadService(PmHealthCheckManager.class);
            loadService(EngineBackupAwarenessManager.class);
            CommandCoordinatorUtil.initAsyncTaskManager();
            loadService(CommandCallbacksPoller.class);
            loadService(ResourceManager.class);
            OvfDataUpdater.getInstance().initOvfDataUpdater();
            StoragePoolStatusHandler.init();

            GlusterJobsManager.init();

            try {
                log.info("Init VM custom properties utilities");
                VmPropertiesUtils.getInstance().init();
            } catch (InitializationException e) {
                log.error("Initialization of vm custom properties failed.", e);
            }

            try {
                log.info("Init device custom properties utilities");
                DevicePropertiesUtils.getInstance().init();
            } catch (InitializationException e) {
                log.error("Initialization of device custom properties failed.", e);
            }

            loadService(SchedulingManager.class);

            sessionDataContainer.cleanupEngineSessionsOnStartup();

            loadService(HostDeviceManager.class);
            loadService(DwhHeartBeat.class);

            if(Config.<Boolean> getValue(ConfigValues.AffinityRulesEnforcementManagerEnabled)) {
                loadService(AffinityRulesEnforcementManager.class);
            }

            loadService(CertificationValidityChecker.class);
        } catch (Exception ex) {
            log.error("Failed to initialize backend", ex);
            throw ex;
        }
    }

    /**
     * Load CDI beans of type {@code BackendService} by simply getting their reference from
     * the bean manager. If the instance doesn't exist (which is the assumption) it will be created
     * and post-constructed (using {@code @PostConstruct} annotated method)
     * @param service a provider of {@code BackendService} instances. see {@linkplain Instance}
     */
    private void loadService(Class<? extends BackendService> service) {
        log.info("Start {} ", services.select(service).get());
    }
}
