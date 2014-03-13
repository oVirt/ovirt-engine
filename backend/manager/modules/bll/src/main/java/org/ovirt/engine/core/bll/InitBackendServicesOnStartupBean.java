package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
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
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
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
        // TODO: remove this later, and rely only on the custom and built in extensions directories configuration

        createInternalAAAConfigurations();
        createKerberosLdapAAAConfigurations();
        ExtensionsManager.getInstance().dump();
        AuthenticationProfileRepository.getInstance();
        DbUserCacheManager.getInstance().init();
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

    private void createInternalAAAConfigurations() {
        Properties authConfig = new Properties();
        authConfig.put(ExtensionsManager.CLASS,
                "org.ovirt.engine.extensions.aaa.builtin.internal.InternalAuthenticator");
        authConfig.put(ExtensionsManager.PROVIDES, "org.ovirt.engine.authentication");
        authConfig.put(ExtensionsManager.ENABLED, "true");
        authConfig.put(ExtensionsManager.MODULE, "org.ovirt.engine.extensions.builtin");
        authConfig.put(ExtensionsManager.NAME, "builtin-authn-internal");
        authConfig.put("ovirt.engine.aaa.authn.profile.name", "internal");
        authConfig.put("ovirt.engine.aaa.authn.authz.plugin", "internal");
        authConfig.put("config.authn.user.name", Config.<String> getValue(ConfigValues.AdminUser));
        authConfig.put("config.authn.user.password", Config.<String> getValue(ConfigValues.AdminPassword));
        authConfig.put(ExtensionsManager.SENSITIVE_KEYS, "config.authn.user.password)");
        ExtensionsManager.getInstance().load(authConfig);

        Properties dirConfig = new Properties();
        dirConfig.put(ExtensionsManager.CLASS, "org.ovirt.engine.extensions.aaa.builtin.internal.InternalDirectory");
        dirConfig.put(ExtensionsManager.PROVIDES, "org.ovirt.engine.authorization");
        dirConfig.put(ExtensionsManager.ENABLED, "true");
        dirConfig.put(ExtensionsManager.MODULE, "org.ovirt.engine.extensions.builtin");
        dirConfig.put(ExtensionsManager.NAME, "internal");
        dirConfig.put("config.authz.user.name", Config.<String> getValue(ConfigValues.AdminUser));
        ExtensionsManager.getInstance().load(dirConfig);
    }

    private void createKerberosLdapAAAConfigurations() {

        List<Properties> results = new ArrayList<>();
        for (String domain : Config.<String> getValue(ConfigValues.DomainName).split("[,]", -1)) {
            Properties authConfig = new Properties();
            authConfig.put(ExtensionsManager.CLASS,
                    "org.ovirt.engine.extensions.aaa.builtin.kerberosldap.KerberosLdapAuthenticator");
            authConfig.put(ExtensionsManager.PROVIDES, "org.ovirt.engine.authentication");
            authConfig.put(ExtensionsManager.ENABLED, "true");
            authConfig.put(ExtensionsManager.MODULE, "org.ovirt.engine.extensions.builtin");
            authConfig.put(ExtensionsManager.NAME, String.format("builtin-authn-%1$s", domain));
            authConfig.put("ovirt.engine.aaa.authn.profile.name", domain);
            authConfig.put("ovirt.engine.aaa.authn.authz.plugin", domain);
            ExtensionsManager.getInstance().load(authConfig);

            Properties dirConfig = new Properties();
            dirConfig.put(ExtensionsManager.CLASS,
                    "org.ovirt.engine.extensions.aaa.builtin.kerberosldap.KerberosLdapDirectory");
            dirConfig.put(ExtensionsManager.PROVIDES, "org.ovirt.engine.authorization");
            dirConfig.put(ExtensionsManager.ENABLED, "true");
            dirConfig.put(ExtensionsManager.MODULE, "org.ovirt.engine.extensions.builtin");
            dirConfig.put(ExtensionsManager.NAME, domain);
            ExtensionsManager.getInstance().load(dirConfig);
        }
    }

}
