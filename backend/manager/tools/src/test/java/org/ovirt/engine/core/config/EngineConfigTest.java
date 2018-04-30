package org.ovirt.engine.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineConfigTest {

    private static final Logger log = LoggerFactory.getLogger(EngineConfigTest.class);
    private EngineConfig config = EngineConfig.getInstance();

    @BeforeAll
    public static void setConfigFilePathProperty() throws UnsupportedEncodingException {
        final String path = URLDecoder.decode(ClassLoader.getSystemResource("engine-config.conf").getPath(), "UTF-8");
        System.setProperty(EngineConfig.CONFIG_FILE_PATH_PROPERTY, path);
    }

    @Test
    public void testConfigDirWithFlagSet() throws Exception {
        // get the real path of the config file
        final String path = URLDecoder.decode(ClassLoader.getSystemResource("engine-config.conf").getPath(), "UTF-8");
        assertNotNull(path);
        EngineConfigExecutor.main("-a", "--config=" + path);
    }

    @Test
    public void getValueWithMultipleVersions() throws Exception {
        final String key = "MaxNumOfVmSockets";
        log.info("getValue: Testing fetch multiple version of {}", key);
        List<ConfigKey> keys = config.getEngineConfigLogic().getConfigDao().getKeysForName(key);
        for (ConfigKey configKey : keys) {
            log.info("{} version: {}", configKey.getDisplayValue(), configKey.getVersion());
        }
        assertTrue(keys.size() > 0);
    }

    @Test
    public void setOutOfRangeValue() throws Exception {
        final String outOfRangeForFenceQuietTime = "601";
        final String key = "FenceQuietTimeBetweenOperationsInSec";
        // Should throw IllegalAccessException since the given value is out of range
        assertThrows(IllegalAccessException.class,
                () -> config.getEngineConfigLogic().persist(key, outOfRangeForFenceQuietTime, ""));
    }

    @Test
    public void setStringValueFromFlag() throws Exception {
        final String certificateFileNameKey = "CertificateFileName";
        // Backing up current CertificateFileName
        ConfigKey originalAuthenticationMethod = config.getEngineConfigLogic().fetchConfigKey(certificateFileNameKey, "general");

        final String certificateFileNameNewValue = "/certs/";
        setKeyAndValidate(certificateFileNameKey, certificateFileNameNewValue, "general");

        // Restoring original value and making sure it was restored successfully
        restoreOriginalValue(certificateFileNameKey, originalAuthenticationMethod);
    }

    private void setKeyAndValidate(final String keyName, final String value, final String version)
            throws IllegalAccessException {
        config.getEngineConfigLogic().persist(keyName, value, version);
        ConfigKey currentConfigKey = config.getEngineConfigLogic().fetchConfigKey(keyName, "general");
        assertEquals(value, currentConfigKey.getValue());
    }

    private void restoreOriginalValue(final String keyName, ConfigKey originialValue)
            throws IllegalAccessException {
        config.getEngineConfigLogic().persist(keyName, originialValue.getValue(), originialValue.getVersion());
        ConfigKey currentConfigKey = config.getEngineConfigLogic().fetchConfigKey(keyName, "general");
        assertEquals(originialValue.getValue(), currentConfigKey.getValue());
    }
}
