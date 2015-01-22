package org.ovirt.engine.core.utils.extensionsmgr;

import static java.util.Arrays.sort;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineExtensionsManager extends ExtensionsManager {

    private static final String ENGINE_EXTENSION_ENABLED = "ENGINE_EXTENSION_ENABLED_";

    private static volatile EngineExtensionsManager instance = null;
    private static Logger log = LoggerFactory.getLogger(EngineExtensionsManager.class);
    private final Set<String> multipleValuesKeys = new HashSet<>(Arrays.asList(
            "AdUserName",
            "AdUserId",
            "AdUserPassword",
            "LDAPProviderTypes",
            "LDAPSecurityAuthentication",
            "LdapServers"
            ));

    public static EngineExtensionsManager getInstance() {
        if (instance == null) {
            synchronized (EngineExtensionsManager.class) {
                if (instance == null) {
                    instance = new EngineExtensionsManager();
                }
            }
        }
        return instance;
    }

    public EngineExtensionsManager() {
        super();
        getGlobalContext().put(
                Base.GlobalContextKeys.APPLICATION_NAME,
                Base.ApplicationNames.OVIRT_ENGINE);
    }

    public void engineInitialize() {
        createInternalAAAConfigurations();
        createKerberosLdapAAAConfigurations();

        for (File directory : EngineLocalConfig.getInstance().getExtensionsDirectories()) {
            if (!directory.exists()) {
                log.warn("The directory '{}' cotaning configuration files does not exist.",
                        directory.getAbsolutePath());
            } else {

                // The order of the files inside the directory is relevant, as the objects are created in
                // the same order
                // that
                // the files are processed, so it is better to sort them so that objects will always be
                // created in the
                // same
                // order regardless of how the filesystem decides to store the entries of the directory:
                File[] files = directory.listFiles();
                if (files != null) {
                    sort(files);
                    for (File file : files) {
                        if (file.getName().endsWith(".properties")) {
                            try {
                                load(file);
                            } catch (Exception ex) {
                                log.error("Could not load extension based on configuration file '{}'. Please check the configuration file is valid. Exception message is: {}",
                                        file.getAbsolutePath(),
                                        ex.getMessage());
                                log.debug("", ex);
                            }
                        }
                    }
                }
            }
        }

        for (ExtensionProxy extension : getLoadedExtensions()) {
            if (
                EngineLocalConfig.getInstance().getBoolean(
                    ENGINE_EXTENSION_ENABLED + normalizeName(
                        extension.getContext().<String> get(
                            Base.ContextKeys.INSTANCE_NAME
                        )
                    ),
                    Boolean.parseBoolean(
                            extension.getContext().<Properties> get(
                                    Base.ContextKeys.CONFIGURATION
                            ).getProperty(Base.ConfigKeys.ENABLED, "true")
                    )
                )
            ) {
                initialize(extension.getContext().<String> get(Base.ContextKeys.INSTANCE_NAME));
            }
        }

        dump();
    }

    private String normalizeName(String s) {
        StringBuilder ret = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '_' || Character.isLetterOrDigit(c)) {
                ret.append(c);
            } else {
                ret.append('_');
            }
        }
        return ret.toString();
    }

    private void createInternalAAAConfigurations() {
        Properties authConfig = new Properties();
        authConfig.put(Base.ConfigKeys.NAME, "builtin-authn-internal");
        authConfig.put(Base.ConfigKeys.PROVIDES, Authn.class.getName());
        authConfig.put(Base.ConfigKeys.BINDINGS_METHOD, Base.ConfigBindingsMethods.JBOSSMODULE);
        authConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE, "org.ovirt.engine.extensions.builtin");
        authConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS,
                "org.ovirt.engine.extensions.aaa.builtin.internal.InternalAuthn");
        authConfig.put("ovirt.engine.aaa.authn.profile.name", "internal");
        authConfig.put("ovirt.engine.aaa.authn.authz.plugin", "internal");
        authConfig.put("config.authn.user.name", Config.<String> getValue(ConfigValues.AdminUser));
        authConfig.put("config.authn.user.password", Config.<String> getValue(ConfigValues.AdminPassword));
        authConfig.put(Base.ConfigKeys.SENSITIVE_KEYS, "config.authn.user.password)");
        load(authConfig);

        Properties dirConfig = new Properties();
        dirConfig.put(Base.ConfigKeys.NAME, "internal");
        dirConfig.put(Base.ConfigKeys.PROVIDES, Authz.class.getName());
        dirConfig.put(Base.ConfigKeys.BINDINGS_METHOD, Base.ConfigBindingsMethods.JBOSSMODULE);
        dirConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE, "org.ovirt.engine.extensions.builtin");
        dirConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS,
                "org.ovirt.engine.extensions.aaa.builtin.internal.InternalAuthz");
        dirConfig.put("config.authz.user.name", Config.<String> getValue(ConfigValues.AdminUser));
        dirConfig.put("config.authz.user.id", "fdfc627c-d875-11e0-90f0-83df133b58cc");
        dirConfig.put("config.query.filter.size",
                Config.getValue(ConfigValues.MaxLDAPQueryPartsNumber).toString());
        load(dirConfig);
    }

    private void createKerberosLdapAAAConfigurations() {
        Map<String, String> passwordChangeMsgPerDomain = new HashMap<>();
        Map<String, String> passwordChangeUrlPerDomain = new HashMap<>();
        String[] pairs = Config.<String> getValue(ConfigValues.ChangePasswordMsg).split(",");
        for (String pair : pairs) {
            // Split the pair in such a way that if the URL contains :, it will not be split to strings
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
                attachConfigValuesFromDb(authConfig, domain);
                load(authConfig);

                Properties dirConfig = new Properties();
                dirConfig.put(Base.ConfigKeys.NAME, domain);
                dirConfig.put(Base.ConfigKeys.PROVIDES, Authz.class.getName());
                dirConfig.put(Base.ConfigKeys.BINDINGS_METHOD, Base.ConfigBindingsMethods.JBOSSMODULE);
                dirConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE, "org.ovirt.engine.extensions.builtin");
                dirConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS,
                        "org.ovirt.engine.extensions.aaa.builtin.kerberosldap.KerberosLdapAuthz");
                dirConfig.put("config.query.filter.size",
                        Config.getValue(ConfigValues.MaxLDAPQueryPartsNumber).toString());
                attachConfigValuesFromDb(dirConfig, domain);
                load(dirConfig);
            }
        }
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }

    private void attachConfigValuesFromDb(Properties props, String domain) {
        attachConfigValueFromDb(domain, props,
                ConfigValues.LdapServers,
                ConfigValues.LDAPServerPort,
                ConfigValues.AdUserName,
                ConfigValues.AdUserPassword,
                ConfigValues.LDAPSecurityAuthentication,
                ConfigValues.AuthenticationMethod,
                ConfigValues.LDAPProviderTypes,
                ConfigValues.LDAPQueryTimeout,
                ConfigValues.DomainName,
                ConfigValues.SASL_QOP,
                ConfigValues.LDAPConnectTimeout,
                ConfigValues.MaxLDAPQueryPartsNumber,
                ConfigValues.LDAPOperationTimeout,
                ConfigValues.LdapQueryPageSize);
    }

    private void attachConfigValueFromDb(String domain, Properties props, ConfigValues... keys) {
        try {
            for (ConfigValues key : keys) {
                String value = multipleValuesKeys.contains(key.name()) ? getValue(domain, Config.getValue(key).toString()): Config.getValue(key).toString();
                if (ConfigValues.AdUserPassword == key) {
                    value = EngineEncryptionUtils.decrypt(value);
                }
                props.put("config." + key.name(), value);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getValue(String domain, String val) {
        String result = "";
        if (val != null && val.length() > 0) {
            String[] domainPairs = val.split(",");
            for (String pair : domainPairs) {
                String[] nameValue = pair.split(":");
                if (nameValue.length != 2) {
                    throw new RuntimeException(
                            String.format(
                                    "Domain information should be in format of DomainName:Value. The string %1$s does not match this format",
                                    pair
                            )
                    );
                }
                if (nameValue[0].equalsIgnoreCase(domain)) {
                    result = nameValue[1];
                    break;
                }
            }
        }
        return result;
    }

}
