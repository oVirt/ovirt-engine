package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.backendinterfaces.IResourceManager;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * The following bean is created in order to initialize and start all related vdsms schedulers
 * in the system after all beans finished initialization
 */
@Singleton
@Startup
@DependsOn({ "Backend", "VdsBroker" })
public class InitBackendServicesOnStartupBean implements InitBackendServicesOnStartup{

    private static LogCompat log = LogFactoryCompat.getLog(InitBackendServicesOnStartupBean.class);

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean lifecycle.
     */
    @PostConstruct
    public void create() {
        log.infoFormat("InitResourceManager: {0}", new java.util.Date());
        IResourceManager manager = EjbUtils.findBean(BeanType.VDS_BROKER, BeanProxyType.LOCAL);
        manager.setup();
        AsyncTaskManager.getInstance().InitAsyncTaskManager();
        log.infoFormat("AsyncTaskManager: {0}", new java.util.Date());

        if (Config.<Boolean> GetValue(ConfigValues.EnableVdsLoadBalancing)) {
            VdsLoadBalancer.EnableLoadBalancer();
        }

        log.infoFormat("VdsLoadBalancer: {0}", new java.util.Date());

        TimeLeasedVmPoolManager.getInstance();
        log.infoFormat("TimeLeasedVmPoolManager: {0}", new java.util.Date());
        MacPoolManager.getInstance().initialize();
        log.infoFormat("MacPoolManager: {0}", new java.util.Date());
        StoragePoolStatusHandler.Init();

    }

}
