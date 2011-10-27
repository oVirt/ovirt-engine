package org.ovirt.engine.core.bll;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;
import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.backendinterfaces.IResourceManager;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

/**
 * The following bean is created in order to initialize and start all related vdsms schedulers
 * in the system after all beans finished initialization
 */
@Service
@Management(InitBackendServicesOnStartup.class)
@Depends({"jboss.j2ee:ear=engine.ear,jar=engine-bll.jar,name=Backend,service=EJB3", "jboss.j2ee:ear=engine.ear,jar=engine-vdsbroker.jar,name=VdsBroker,service=EJB3"})
public class InitBackendServicesOnStartupBean implements InitBackendServicesOnStartup{

    private static LogCompat log = LogFactoryCompat.getLog(InitBackendServicesOnStartupBean.class);

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean lifecycle.
     */
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
