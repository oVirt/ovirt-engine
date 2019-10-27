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
import org.ovirt.engine.core.bll.hostdeploy.HostUpdatesCheckerService;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.kubevirt.KubevirtService;
import org.ovirt.engine.core.bll.network.cluster.ExternalNetworkSyncService;
import org.ovirt.engine.core.bll.pm.PmHealthCheckManager;
import org.ovirt.engine.core.bll.scheduling.AffinityRulesEnforcementManager;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfDataUpdater;
import org.ovirt.engine.core.bll.storage.pool.StoragePoolStatusHandler;
import org.ovirt.engine.core.bll.tasks.AsyncTaskManager;
import org.ovirt.engine.core.bll.tasks.CommandCallbacksPoller;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.CommandsRepository;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.utils.exceptions.InitializationException;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProxyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The following bean is created in order to initialize and start all related vdsms schedulers
 * in the system after all beans finished initialization
 */
@Singleton
@Startup
@DependsOn("Backend")
public class InitBackendServicesOnStartupBean implements InitBackendServicesOnStartup {

    private static final Logger log = LoggerFactory.getLogger(InitBackendServicesOnStartupBean.class);

    @Inject
    private Instance<SchedulingManager> schedulingManagerProvider;

    @Inject
    private SessionDataContainer sessionDataContainer;

    @Inject
    private ServiceLoader serviceLoader;

    @Inject
    private Instance<ResourceManager> resourceManager;

    @Inject
    private Instance<CommandsRepository> commandsRepository;

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean life cycle.
     */
    @Override
    @PostConstruct
    public void create() {

        try {
            serviceLoader.load(ClusterCpuFlagsManager.class);
            // This must be done before starting to sample the hosts status from VDSM since the sampling will turn such host from Reboot to NonResponsive
            serviceLoader.load(PmHealthCheckManager.class);
            serviceLoader.load(EngineBackupAwarenessManager.class);
            serviceLoader.load(DataCenterCompatibilityChecker.class);
            serviceLoader.load(ResourceManager.class);
            serviceLoader.load(HostDeviceManager.class);

            try {
                log.info("Init VM custom properties utilities");
                VmPropertiesUtils.getInstance().init();
            } catch (InitializationException e) {
                log.error("Initialization of vm custom properties failed.", e);
            }

            serviceLoader.load(KubevirtService.class);

            serviceLoader.load(IrsProxyManager.class);
            serviceLoader.load(OvfDataUpdater.class);
            serviceLoader.load(StoragePoolStatusHandler.class);
            serviceLoader.load(GlusterJobsManager.class);

            resourceManager.get().scheduleJobsForHosts();

            try {
                log.info("Init device custom properties utilities");
                DevicePropertiesUtils.getInstance().init();
            } catch (InitializationException e) {
                log.error("Initialization of device custom properties failed.", e);
            }

            serviceLoader.load(SchedulingManager.class);

            sessionDataContainer.cleanupEngineSessionsOnStartup();

            serviceLoader.load(DwhHeartBeat.class);

            commandsRepository.get().handleUnmanagedCommands();
            serviceLoader.load(AsyncTaskManager.class);
            serviceLoader.load(CommandCoordinatorUtil.class);
            serviceLoader.load(CommandCallbacksPoller.class);
            serviceLoader.load(CommandEntityCleanupManager.class);

            if(Config.<Boolean> getValue(ConfigValues.AffinityRulesEnforcementManagerEnabled)) {
                serviceLoader.load(AffinityRulesEnforcementManager.class);
            }

            serviceLoader.load(CertificationValidityChecker.class);
            serviceLoader.load(HostUpdatesCheckerService.class);
            serviceLoader.load(IPTablesDeprecationNotifier.class);
            serviceLoader.load(ExternalNetworkSyncService.class);
        } catch (Exception ex) {
            log.error("Failed to initialize backend", ex);
            throw ex;
        }
    }

}
