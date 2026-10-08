package org.ovirt.engine.core.bll.aaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.utils.EngineLocalConfig;

/**
 * Unit tests for {@link JaasConfigurationInitializer}.
 *
 * Tests verify that the JAAS configuration is correctly set up for all
 * oVirt Kerberos authentication entries.
 */
class JaasConfigurationInitializerTest {

    private static final String KRB5_LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule";

    private JaasConfigurationInitializer initializer;

    @BeforeEach
    void setUp() {
        EngineLocalConfig.clearInstance();
        initializer = new JaasConfigurationInitializer();
    }

    @AfterEach
    void tearDown() {
        EngineLocalConfig.clearInstance();
        // Reset configuration to avoid affecting other tests
        Configuration.setConfiguration(null);
    }

    @Test
    void testOVirtKerbEntry() {
        // given
        initializeWithEmptyConfig();

        // when
        initializer.initialize();
        AppConfigurationEntry[] entries = Configuration.getConfiguration()
                .getAppConfigurationEntry("oVirtKerb");

        // then
        assertNotNull(entries, "oVirtKerb entry should exist");
        assertEquals(1, entries.length);
        assertEquals(KRB5_LOGIN_MODULE, entries[0].getLoginModuleName());
        assertEquals(AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, entries[0].getControlFlag());

        Map<String, ?> options = entries[0].getOptions();
        assertNull(options.get("useTicketCache"), "useTicketCache should not be set");
        assertNull(options.get("useKeyTab"), "useKeyTab should not be set");
        assertNull(options.get("debug"), "debug should not be set");
    }

    @Test
    void testOVirtKerbDebugEntry() {
        // given
        initializeWithEmptyConfig();

        // when
        initializer.initialize();
        AppConfigurationEntry[] entries = Configuration.getConfiguration()
                .getAppConfigurationEntry("oVirtKerbDebug");

        // then
        assertNotNull(entries, "oVirtKerbDebug entry should exist");
        assertEquals(1, entries.length);
        assertEquals(KRB5_LOGIN_MODULE, entries[0].getLoginModuleName());

        Map<String, ?> options = entries[0].getOptions();
        assertEquals("true", options.get("debug"), "debug should be enabled");
        assertNull(options.get("useTicketCache"), "useTicketCache should not be set");
    }

    @Test
    void testOVirtKerbUseTicketCacheEntry() {
        // given
        initializeWithEmptyConfig();

        // when
        initializer.initialize();
        AppConfigurationEntry[] entries = Configuration.getConfiguration()
                .getAppConfigurationEntry("oVirtKerbUseTicketCache");

        // then
        assertNotNull(entries, "oVirtKerbUseTicketCache entry should exist");
        assertEquals(1, entries.length);
        assertEquals(KRB5_LOGIN_MODULE, entries[0].getLoginModuleName());

        Map<String, ?> options = entries[0].getOptions();
        assertEquals("true", options.get("useTicketCache"), "useTicketCache should be enabled");
        assertNull(options.get("debug"), "debug should not be set");
    }

    @Test
    void testOVirtKerbUseTicketCacheDebugEntry() {
        // given
        initializeWithEmptyConfig();

        // when
        initializer.initialize();
        AppConfigurationEntry[] entries = Configuration.getConfiguration()
                .getAppConfigurationEntry("oVirtKerbUseTicketCacheDebug");

        // then
        assertNotNull(entries, "oVirtKerbUseTicketCacheDebug entry should exist");
        assertEquals(1, entries.length);

        Map<String, ?> options = entries[0].getOptions();
        assertEquals("true", options.get("useTicketCache"), "useTicketCache should be enabled");
        assertEquals("true", options.get("debug"), "debug should be enabled");
    }

    @Test
    void testOVirtKerbAAAEntryWithKeytab() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("AAA_JAAS_USE_KEYTAB", "true");
                put("AAA_JAAS_KEYTAB_FILE", "/etc/ovirt-engine/ovirt-engine.keytab");
                put("AAA_JAAS_PRINCIPAL_NAME", "HTTP/engine.example.com@EXAMPLE.COM");
                put("AAA_JAAS_ENABLE_DEBUG", "true");
            }
        });

        // when
        initializer.initialize();
        AppConfigurationEntry[] entries = Configuration.getConfiguration()
                .getAppConfigurationEntry("oVirtKerbAAA");

        // then
        assertNotNull(entries, "oVirtKerbAAA entry should exist");
        assertEquals(1, entries.length);
        assertEquals(KRB5_LOGIN_MODULE, entries[0].getLoginModuleName());

        Map<String, ?> options = entries[0].getOptions();
        assertEquals("true", options.get("useKeyTab"), "useKeyTab should be enabled");
        assertEquals("true", options.get("doNotPrompt"), "doNotPrompt should be enabled");
        assertEquals("true", options.get("storeKey"), "storeKey should be enabled");
        assertEquals("/etc/ovirt-engine/ovirt-engine.keytab", options.get("keyTab"));
        assertEquals("HTTP/engine.example.com@EXAMPLE.COM", options.get("principal"));
        assertEquals("true", options.get("debug"), "debug should be enabled");
    }

    @Test
    void testOVirtKerbAAAEntryWithTicketCache() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("AAA_JAAS_USE_TICKET_CACHE", "true");
                put("AAA_JAAS_TICKET_CACHE_FILE", "/tmp/krb5cc_engine");
                put("AAA_JAAS_PRINCIPAL_NAME", "engine@EXAMPLE.COM");
            }
        });

        // when
        initializer.initialize();
        AppConfigurationEntry[] entries = Configuration.getConfiguration()
                .getAppConfigurationEntry("oVirtKerbAAA");

        // then
        assertNotNull(entries, "oVirtKerbAAA entry should exist");
        assertEquals(1, entries.length);

        Map<String, ?> options = entries[0].getOptions();
        assertEquals("true", options.get("useTicketCache"), "useTicketCache should be enabled");
        assertEquals("/tmp/krb5cc_engine", options.get("ticketCache"));
        assertEquals("engine@EXAMPLE.COM", options.get("principal"));
        assertNull(options.get("useKeyTab"), "useKeyTab should not be set");
        assertNull(options.get("debug"), "debug should not be set when not configured");
    }

    @Test
    void testOVirtKerbAAAEntryWithEmptyConfig() {
        // given
        initializeWithEmptyConfig();

        // when
        initializer.initialize();
        AppConfigurationEntry[] entries = Configuration.getConfiguration()
                .getAppConfigurationEntry("oVirtKerbAAA");

        // then
        assertNotNull(entries, "oVirtKerbAAA entry should exist even with empty config");
        assertEquals(1, entries.length);

        Map<String, ?> options = entries[0].getOptions();
        assertTrue(options.isEmpty() || !options.containsKey("useKeyTab"),
                "No keytab options should be set when not configured");
    }

    @Test
    void testUnknownEntryReturnsNull() {
        // given
        initializeWithEmptyConfig();

        // when
        initializer.initialize();
        AppConfigurationEntry[] entries = Configuration.getConfiguration()
                .getAppConfigurationEntry("unknownEntry");

        // then
        assertNull(entries, "Unknown entry should return null");
    }

    @Test
    void testAllEntriesUseRequiredControlFlag() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("AAA_JAAS_USE_KEYTAB", "true");
                put("AAA_JAAS_KEYTAB_FILE", "/test.keytab");
            }
        });

        // when
        initializer.initialize();
        Configuration config = Configuration.getConfiguration();

        // then
        String[] entryNames = {
            "oVirtKerb", "oVirtKerbDebug", "oVirtKerbUseTicketCache",
            "oVirtKerbUseTicketCacheDebug", "oVirtKerbAAA"
        };

        for (String entryName : entryNames) {
            AppConfigurationEntry[] entries = config.getAppConfigurationEntry(entryName);
            assertNotNull(entries, entryName + " entry should exist");
            assertEquals(AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    entries[0].getControlFlag(),
                    entryName + " should use REQUIRED control flag");
        }
    }

    private void initializeWithEmptyConfig() {
        EngineLocalConfig.getInstance(new HashMap<>());
    }
}
