package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.ovirt.engine.core.authentication.AuthenticationProfile;
import org.ovirt.engine.core.authentication.AuthenticationProfileManager;
import org.ovirt.engine.core.authentication.Authenticator;
import org.ovirt.engine.core.authentication.AuthenticatorManager;
import org.ovirt.engine.core.authentication.Directory;
import org.ovirt.engine.core.authentication.DirectoryManager;
import org.ovirt.engine.core.authentication.provisional.ProvisionalAuthenticator;
import org.ovirt.engine.core.authentication.provisional.ProvisionalDirectory;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.dwh.DwhHeartBeat;
import org.ovirt.engine.core.bll.gluster.GlusterJobsManager;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.provider.ExternalTrustStoreInitializer;
import org.ovirt.engine.core.bll.scheduling.MigrationHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;
import org.ovirt.engine.core.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.utils.exceptions.InitializationException;
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

    private static Logger log = LoggerFactory.getLogger(InitBackendServicesOnStartupBean.class);

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean life cycle.
     */
    @Override
    @PostConstruct
    public void create() {

        // Create authentication profiles for all the domains that exist in the database:
        for (String domain : LdapBrokerUtils.getDomainsList()) {
            LdapBroker broker = LdapFactory.getInstance(domain);
            Authenticator authenticator = new ProvisionalAuthenticator(domain, broker);
            Directory directory = new ProvisionalDirectory(domain, broker);
            AuthenticationProfile profile = new AuthenticationProfile(domain, authenticator, directory);
            AuthenticatorManager.getInstance().registerAuthenticator(domain, authenticator);
            DirectoryManager.getInstance().registerDirectory(domain, directory);
            AuthenticationProfileManager.getInstance().registerProfile(domain, profile);
        }

        // Load authentication profiles:
        File authDir = EngineLocalConfig.getInstance().getAuthDir();
        if (authDir.exists() && authDir.isDirectory()) {
            AuthenticationProfileManager.getInstance().loadFiles(authDir);
        }

        AsyncTaskManager.getInstance().initAsyncTaskManager();
        ResourceManager.getInstance().init();
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
                MacPoolManager.getInstance().initialize();
            }
        });
        StoragePoolStatusHandler.init();

        GlusterJobsManager.init();

        ExternalTrustStoreInitializer.init();

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

        new DwhHeartBeat().init();
    }

}
