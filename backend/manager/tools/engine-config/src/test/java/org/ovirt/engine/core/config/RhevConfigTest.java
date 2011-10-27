package org.ovirt.engine.core.config;

import static junit.framework.Assert.assertNotNull;
import java.util.List;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.core.config.entity.ConfigKey;

public class RhevConfigTest {

    public static final Logger log = Logger.getLogger(RhevConfigTest.class);

    @BeforeClass
    public static void setConfigFilePathProperty() {
        String path = ClassLoader.getSystemResource("engine-config.conf").getPath();
        System.setProperty(EngineConfig.CONFIG_FILE_PATH_PROPERTY, path);
    }

    @Ignore
    @Test
    public void setStringValueFromFlag() throws Exception {
        String domains = "1.example.com, 2.example.com";
        String domainsKey = "DomainName";
        String version = "--cver=general";
        EngineConfig.main("-s", domainsKey, domains, version);
        ConfigKey domainsConfigKey = EngineConfig.getInstance().getEngineConfigLogic().fetchConfigKey(domainsKey, "");
        Assert.assertEquals(domains, domainsConfigKey.getValue());
    }

    @Test
    public void testConfigDirWithFlagSet() throws Exception {
        // get the real path of the config file
        String path = ClassLoader.getSystemResource("engine-config.conf").getPath();
        assertNotNull(path);
        EngineConfig.main("-a", "--config=" + path);
    }

    @Ignore
    @Test
    public void getValueWithMultipleVersions() throws Exception {
        String key = "MaxNumOfVmSockets";
        log.info("getValue: Testing fetch multiple version of " + key);
        List<ConfigKey> keys = EngineConfig.getInstance().getEngineConfigLogic().getConfigDAO().getKeysForName(key);
        for (ConfigKey configKey : keys) {
            log.info(configKey.getDisplayValue() + " version: " + configKey.getVersion());
        }
        Assert.assertTrue(keys.size() > 0);
    }

    @Test
    public void setOutOfRangeValue() throws Exception {
        EngineConfig config = EngineConfig.getInstance();
        String key = "VdsRefreshRate";
        boolean setKeyValue = config.getEngineConfigLogic().persist(key, "33444", "");
        Assert.assertFalse(setKeyValue); // out of valid range
    }

    @Test
    public void setInvalidStringValue() throws Exception {
        EngineConfig config = EngineConfig.getInstance();
        String key = "LDAP_Security_mode";
        Assert.assertFalse(config.getEngineConfigLogic().persist(key, "GSSAPI-invalid-value")); // not valid
    }

    @Ignore
    @Test
    public void setValueWithMultipleVersions() throws Exception {
        String key = "MaxNumOfVmSockets";
        log.info("getValue: Testing set dialog, with multiple version of " + key);
        EngineConfig.main("-s", key, "40");
    }
}
