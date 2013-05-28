package org.ovirt.engine.core.bll;


import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.ovirt.engine.core.bll.gluster.GlusterJobsManager;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.provider.ExternalTrustStoreInitializer;
import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;
import org.ovirt.engine.core.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.utils.exceptions.InitializationException;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

/**
 * The following bean is created in order to initialize and start all related vdsms schedulers
 * in the system after all beans finished initialization
 */
@Singleton
@Startup
@DependsOn({ "Backend"})
public class InitBackendServicesOnStartupBean implements InitBackendServicesOnStartup{

    private static Log log = LogFactory.getLog(InitBackendServicesOnStartupBean.class);

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean life cycle.
     */
    @Override
    @PostConstruct
    public void create() {

        ResourceManager.getInstance().init();
        AsyncTaskManager.getInstance().InitAsyncTaskManager();
        OvfDataUpdater.getInstance().initOvfDataUpdater();

        if (Config.<Boolean> GetValue(ConfigValues.EnableVdsLoadBalancing)) {
            VdsLoadBalancer.EnableLoadBalancer();
        }

        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                MacPoolManager.getInstance().initialize();
            }
        });
        StoragePoolStatusHandler.Init();

        GlusterJobsManager.init();

        ExternalTrustStoreInitializer.init();

        try {
            log.info("Init VM custom properties utilities");
            VmPropertiesUtils.getInstance().init();
        } catch (InitializationException e) {
            log.error("Initialization of vm custom properties failed.",e);
        }

        try {
            log.info("Init device custom properties utilities");
            DevicePropertiesUtils.getInstance().init();
        } catch (InitializationException e) {
            log.error("Initialization of device custom properties failed.",e);
        }
    }

}
