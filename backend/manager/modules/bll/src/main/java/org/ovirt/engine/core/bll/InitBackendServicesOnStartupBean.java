package org.ovirt.engine.core.bll;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.ovirt.engine.core.bll.gluster.GlusterManager;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.exceptions.InitializationException;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
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
        log.infoFormat("InitResourceManager: {0}", new Date());
        ResourceManager.getInstance().init();
        AsyncTaskManager.getInstance().InitAsyncTaskManager();
        log.infoFormat("AsyncTaskManager: {0}", new Date());
        OvfDataUpdater.getInstance().initOvfDataUpdater();
        log.infoFormat("OvfDataUpdater: {0}", new Date());

        if (Config.<Boolean> GetValue(ConfigValues.EnableVdsLoadBalancing)) {
            VdsLoadBalancer.EnableLoadBalancer();
        }

        log.infoFormat("VdsLoadBalancer: {0}", new Date());
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                log.infoFormat("MacPoolManager started: {0}", new Date());
                MacPoolManager.getInstance().initialize();
                log.infoFormat("MacPoolManager finished: {0}", new Date());
            }
        });
        StoragePoolStatusHandler.Init();

        GlusterManager.getInstance().init();
        try {
            log.infoFormat("Init VM Custom Properties utilities: {0}", new Date());
            VmPropertiesUtils.getInstance().init();
        } catch (InitializationException e) {
            log.errorFormat("Initialization failed. Exception message is {0} ",e.getMessage());
            log.debug("Initialization failed ",e);
            throw new RuntimeException(e);
        }
    }

}
