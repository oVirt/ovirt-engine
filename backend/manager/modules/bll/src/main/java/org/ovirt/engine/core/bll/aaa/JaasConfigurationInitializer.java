package org.ovirt.engine.core.bll.aaa;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes JAAS Configuration for Kerberos authentication before
 * AAA extensions are loaded.
 *
 * This provides all oVirt JAAS entries:
 * - oVirtKerb: Basic Kerberos login
 * - oVirtKerbDebug: Basic Kerberos with debug enabled
 * - oVirtKerbUseTicketCache: Kerberos using ticket cache
 * - oVirtKerbUseTicketCacheDebug: Kerberos using ticket cache with debug
 * - oVirtKerbAAA: Dynamic entry for AAA-LDAP extension (keytab/ticket cache based)
 *
 * The oVirtKerbAAA entry is built dynamically from engine local config
 * properties (AAA_JAAS_*) and is used by AAA-LDAP extension for GSSAPI
 * binds to LDAP servers.
 */
@Startup
@Singleton(name = "JaasConfigurationInitializer")
public class JaasConfigurationInitializer {

    private static final Logger log = LoggerFactory.getLogger(JaasConfigurationInitializer.class);

    private static final String KRB5_LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule";

    @PostConstruct
    public void initialize() {
        log.info("Initializing JAAS configuration for Kerberos authentication");

        Configuration.setConfiguration(new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                switch (name) {
                    case "oVirtKerb":
                        return createEntry(false, false, false);
                    case "oVirtKerbDebug":
                        return createEntry(false, false, true);
                    case "oVirtKerbUseTicketCache":
                        return createEntry(true, false, false);
                    case "oVirtKerbUseTicketCacheDebug":
                        return createEntry(true, false, true);
                    case "oVirtKerbAAA":
                        return createKerbAAAEntry();
                    default:
                        return null;
                }
            }
        });

        log.info("JAAS configuration initialized successfully");
    }

    /**
     * Create a basic JAAS entry for Kerberos authentication.
     */
    private AppConfigurationEntry[] createEntry(boolean useTicketCache, boolean useKeyTab, boolean debug) {
        Map<String, String> options = new HashMap<>();

        if (useTicketCache) {
            options.put("useTicketCache", "true");
        }

        if (useKeyTab) {
            options.put("useKeyTab", "true");
            options.put("doNotPrompt", "true");
            options.put("storeKey", "true");
        }

        if (debug) {
            options.put("debug", "true");
        }

        return new AppConfigurationEntry[] {
            new AppConfigurationEntry(
                KRB5_LOGIN_MODULE,
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                options
            )
        };
    }

    /**
     * Create the dynamic oVirtKerbAAA entry from engine configuration.
     * This is used by AAA-LDAP extension for GSSAPI binds with keytab/ticket cache.
     */
    private AppConfigurationEntry[] createKerbAAAEntry() {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        Map<String, String> options = new HashMap<>();

        // useTicketCache
        if (config.getBoolean("AAA_JAAS_USE_TICKET_CACHE", false)) {
            options.put("useTicketCache", "true");
            String ticketCache = config.getProperty("AAA_JAAS_TICKET_CACHE_FILE", true);
            if (ticketCache != null && !ticketCache.isEmpty()) {
                options.put("ticketCache", ticketCache);
            }
        }

        // useKeyTab
        if (config.getBoolean("AAA_JAAS_USE_KEYTAB", false)) {
            options.put("useKeyTab", "true");
            options.put("doNotPrompt", "true");
            options.put("storeKey", "true");
            String keytab = config.getProperty("AAA_JAAS_KEYTAB_FILE", true);
            if (keytab != null && !keytab.isEmpty()) {
                options.put("keyTab", keytab);
            }
        }

        // principal
        String principal = config.getProperty("AAA_JAAS_PRINCIPAL_NAME", true);
        if (principal != null && !principal.isEmpty()) {
            options.put("principal", principal);
        }

        // debug
        if (config.getBoolean("AAA_JAAS_ENABLE_DEBUG", false)) {
            options.put("debug", "true");
        }

        log.debug("Created oVirtKerbAAA JAAS entry with options: {}", options.keySet());

        return new AppConfigurationEntry[] {
            new AppConfigurationEntry(
                KRB5_LOGIN_MODULE,
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                options
            )
        };
    }
}
