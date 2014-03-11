package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.Authenticator;
import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.internal.InternalAuthenticator;
import org.ovirt.engine.core.aaa.internal.InternalDirectory;
import org.ovirt.engine.core.aaa.provisional.ProvisionalAuthenticator;
import org.ovirt.engine.core.aaa.provisional.ProvisionalDirectory;
import org.ovirt.engine.core.bll.adbroker.KerberosManager;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.bll.adbroker.UsersDomainsCacheManagerService;
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
import org.ovirt.engine.api.extensions.Extension.ExtensionProperties;
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
        InternalAuthenticator internalAuthenticator = new InternalAuthenticator();
        Map<ExtensionProperties, Object> internalAuthContext = new EnumMap<>(ExtensionProperties.class);
        Properties internalAuthProps = new Properties();
        String internalProfileName = Config.<String> getValue(ConfigValues.AdminDomain).trim();
        internalAuthProps.put("ovirt.engine.aaa.authn.profile.name", internalProfileName);
        internalAuthContext.put(ExtensionProperties.CONFIGURATION, internalAuthProps);
        internalAuthContext.put(ExtensionProperties.NAME, internalProfileName);
        internalAuthenticator.setContext(internalAuthContext);
        internalAuthenticator.init();

        InternalDirectory internalDirectory = new InternalDirectory();
        Map<ExtensionProperties, Object> internalDirContext = new EnumMap<>(ExtensionProperties.class);
        Properties internalDirProps = new Properties();
        internalDirProps.put("ovirt.engine.aaa.authz.profile.name", internalProfileName);
        internalDirContext.put(ExtensionProperties.CONFIGURATION, internalDirProps);
        internalDirContext.put(ExtensionProperties.NAME, internalProfileName);
        internalDirectory.setContext(internalDirContext);
        internalDirectory.init();

        AuthenticationProfileRepository.getInstance().registerProfile(
                new AuthenticationProfile(internalAuthenticator,
                        internalDirectory)
                );
        for (String domain : LdapBrokerUtils.getDomainsList(true)) {
            Map<ExtensionProperties, Object> dirContext = new EnumMap<>(ExtensionProperties.class);
            Properties dirProps = new Properties();
            dirProps.put("ovirt.engine.aaa.authz.profile.name", domain);
            dirContext.put(ExtensionProperties.CONFIGURATION, dirProps);
            dirContext.put(ExtensionProperties.NAME, domain);
            Directory directory = new ProvisionalDirectory();
            directory.setContext(dirContext);
            directory.init();

            Map<ExtensionProperties, Object> authContext = new EnumMap<>(ExtensionProperties.class);
            Properties authProps = new Properties();
            authProps.put("ovirt.engine.aaa.authn.profile.name", domain);
            authContext.put(ExtensionProperties.CONFIGURATION, authProps);
            authContext.put(ExtensionProperties.NAME, domain);
            Authenticator authenticator = new ProvisionalAuthenticator();
            authenticator.setContext(authContext);
            authenticator.init();

            AuthenticationProfile profile = new AuthenticationProfile(authenticator, directory);

            AuthenticationProfileRepository.getInstance().registerProfile(profile);
        }

        AuthenticationProfileRepository.getInstance();

        KerberosManager.getInstance();
        UsersDomainsCacheManagerService.getInstance().init();
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

}
