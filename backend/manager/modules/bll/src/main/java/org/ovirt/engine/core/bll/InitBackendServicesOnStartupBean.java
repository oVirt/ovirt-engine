package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.dwh.DwhHeartBeat;
import org.ovirt.engine.core.bll.gluster.GlusterJobsManager;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolPerDcSingleton;
import org.ovirt.engine.core.bll.pm.PmHealthCheckManager;
import org.ovirt.engine.core.bll.scheduling.MigrationHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.Injector;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.utils.exceptions.InitializationException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
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

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean life cycle.
     */
    @Override
    @PostConstruct
    public void create() {

        try {
            // This must be done before starting to sample the hosts status from VDSM since the sampling will turn such host from Reboot to NonResponsive
            List<VDS> hosts = DbFacade.getInstance().getVdsDao().getAll();
            // Initialize Power Management Health Check
            PmHealthCheckManager.getInstance().initialize();
            // recover from engine failure
            PmHealthCheckManager.getInstance().recover(hosts);

            CommandCoordinatorUtil.initAsyncTaskManager();
            Injector.get(ResourceManager.class);
            OvfDataUpdater.getInstance().initOvfDataUpdater();
            SchedulingManager.getInstance().setMigrationHandler(new MigrationHandler() {

                @Override
                public void migrateVM(List<Guid> initialHosts, Guid vmToMigrate) {
                    MigrateVmParameters parameters = new MigrateVmParameters(false, vmToMigrate);
                    parameters.setInitialHosts(new ArrayList<Guid>(initialHosts));
                    Backend.getInstance().runInternalAction(VdcActionType.MigrateVm,
                            parameters,
                            ExecutionHandler.createInternalJobContext());
                }
            });

            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    MacPoolPerDcSingleton.getInstance().initialize();
                }
            });
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

            SchedulingManager.getInstance().init();

            SessionDataContainer.getInstance().cleanupEngineSessionsOnStartup();

            new DwhHeartBeat().init();

        } catch (Exception ex) {
            log.error("Failed to initialize backend", ex);
            throw ex;
        }
    }


}
