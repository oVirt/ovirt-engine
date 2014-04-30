package org.ovirt.engine.core.bll;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
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

    private static final Logger log = LoggerFactory.getLogger(InitBackendServicesOnStartupBean.class);

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
        authConfig.put(Base.ConfigKeys.NAME, "builtin-authn-internal");
        authConfig.put(Base.ConfigKeys.PROVIDES, Authn.class.getName());
        authConfig.put(Base.ConfigKeys.BINDINGS_METHOD, Base.ConfigBindingsMethods.JBOSSMODULE);
        authConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE, "org.ovirt.engine.extensions.builtin");
        authConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS, "org.ovirt.engine.extensions.aaa.builtin.internal.InternalAuthn");
        authConfig.put("ovirt.engine.aaa.authn.profile.name", "internal");
        authConfig.put("ovirt.engine.aaa.authn.authz.plugin", "internal");
        authConfig.put("config.authn.user.name", Config.<String> getValue(ConfigValues.AdminUser));
        authConfig.put("config.authn.user.password", Config.<String> getValue(ConfigValues.AdminPassword));
        authConfig.put(Base.ConfigKeys.SENSITIVE_KEYS, "config.authn.user.password)");
        ExtensionsManager.getInstance().load(authConfig);

        Properties dirConfig = new Properties();
        dirConfig.put(Base.ConfigKeys.NAME, "internal");
        dirConfig.put(Base.ConfigKeys.PROVIDES, Authz.class.getName());
        dirConfig.put(Base.ConfigKeys.BINDINGS_METHOD, Base.ConfigBindingsMethods.JBOSSMODULE);
        dirConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE, "org.ovirt.engine.extensions.builtin");
        dirConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS, "org.ovirt.engine.extensions.aaa.builtin.internal.InternalAuthz");
        dirConfig.put("config.authz.user.name", Config.<String> getValue(ConfigValues.AdminUser));
        dirConfig.put("config.authz.user.id", "fdfc627c-d875-11e0-90f0-83df133b58cc");
        dirConfig.put("config.query.filter.size",
                Config.<Integer> getValue(ConfigValues.MaxLDAPQueryPartsNumber));
        ExtensionsManager.getInstance().load(dirConfig);
    }

    private void createKerberosLdapAAAConfigurations() {
        Map<String, String> passwordChangeMsgPerDomain = new HashMap<>();
        Map<String, String> passwordChangeUrlPerDomain = new HashMap<>();
        String[] pairs = Config.<String> getValue(ConfigValues.ChangePasswordMsg).split(",");
        for (String pair : pairs) {
            //Split the pair in such a way that if the URL contains :, it will not be split to strings
            String[] pairParts = pair.split(":", 2);
            if (pairParts.length >= 2) {
                String decodedMsgOrUrl;
                try {
                    decodedMsgOrUrl = URLDecoder.decode(pairParts[1], Charset.forName("UTF-8").toString());
                    if (decodedMsgOrUrl.indexOf("http:") == 0 || decodedMsgOrUrl.indexOf("https:") == 0) {
                        passwordChangeUrlPerDomain.put(pairParts[0], decodedMsgOrUrl);
                    } else {
                        passwordChangeMsgPerDomain.put(pairParts[0], decodedMsgOrUrl);
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (String domain : Config.<String> getValue(ConfigValues.DomainName).split("[,]", 0)) {
            domain = domain.trim();
            if (!domain.isEmpty()) {
                Properties authConfig = new Properties();
                authConfig.put(Base.ConfigKeys.NAME, String.format("builtin-authn-%1$s", domain));
                authConfig.put(Base.ConfigKeys.PROVIDES, Authn.class.getName());
                authConfig.put(Base.ConfigKeys.ENABLED, "true");
                authConfig.put(Base.ConfigKeys.BINDINGS_METHOD, Base.ConfigBindingsMethods.JBOSSMODULE);
                authConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE, "org.ovirt.engine.extensions.builtin");
                authConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS,
                        "org.ovirt.engine.extensions.aaa.builtin.kerberosldap.KerberosLdapAuthn");
                authConfig.put("ovirt.engine.aaa.authn.profile.name", domain);
                authConfig.put("ovirt.engine.aaa.authn.authz.plugin", domain);
                authConfig.put("config.change.password.url", blankIfNull(passwordChangeUrlPerDomain.get(domain)));
                authConfig.put("config.change.password.msg", blankIfNull(passwordChangeMsgPerDomain.get(domain)));
                ExtensionsManager.getInstance().load(authConfig);

                Properties dirConfig = new Properties();
                dirConfig.put(Base.ConfigKeys.NAME, domain);
                dirConfig.put(Base.ConfigKeys.PROVIDES, Authz.class.getName());
                dirConfig.put(Base.ConfigKeys.BINDINGS_METHOD, Base.ConfigBindingsMethods.JBOSSMODULE);
                dirConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE, "org.ovirt.engine.extensions.builtin");
                dirConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS,
                        "org.ovirt.engine.extensions.aaa.builtin.kerberosldap.KerberosLdapAuthz");
                dirConfig.put("config.query.filter.size",
                        Config.<Integer> getValue(ConfigValues.MaxLDAPQueryPartsNumber));
                ExtensionsManager.getInstance().load(dirConfig);
            }
        }
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }

}
