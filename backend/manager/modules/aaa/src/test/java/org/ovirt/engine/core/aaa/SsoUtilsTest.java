package org.ovirt.engine.core.aaa;

import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.utils.EngineLocalConfig;

class SsoUtilsTest {

    @BeforeEach
    public void setup() {
        EngineLocalConfig.clearInstance();
    }

    @AfterEach
    public void cleanUp() {
        EngineLocalConfig.clearInstance();
    }

    @Test
    public void shouldMatchAppUrlDomainOnAlternateSSOEngineUrl() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("SSO_ENGINE_URL", "https://engine.example.com:8221/ovirt-engine");
                put("SSO_ALTERNATE_ENGINE_FQDNS", "engine1.example.com alternate-engine.example.com");
            }
        });

        // when
        boolean valid = SsoUtils.isDomainValid("https://alternate-engine.example.com:20001/somerest/api_v9");

        // then
        Assertions.assertTrue(valid);
    }

    @Test
    public void shouldMatchAppUrlDomainOnAlternateSSOEngineUrlRegardlessUpperCase() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("SSO_ENGINE_URL", "https://engine.example.com:8221/ovirt-engine");
                put("SSO_ALTERNATE_ENGINE_FQDNS", "engine1.example.com ALTERNATE-engine.example.com");
            }
        });

        // when
        boolean valid = SsoUtils.isDomainValid("https://alternate-engine.EXAMPLE.com:20001/somerest/api_v9");

        // then
        Assertions.assertTrue(valid);
    }

    @Test
    public void shouldAllowBlankAppUrl() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("SSO_ENGINE_URL", "https://engine.example.com:8221/ovirt-engine");
                put("SSO_ALTERNATE_ENGINE_FQDNS", "engine1.example.com alternate-engine.example.com");
            }
        });

        // when
        boolean valid = SsoUtils.isDomainValid("  ");

        // then
        Assertions.assertTrue(valid);
    }

    @Test
    public void shouldAllowNullAppUrl() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("SSO_ENGINE_URL", "https://engine.example.com:8221/ovirt-engine");
                put("SSO_ALTERNATE_ENGINE_FQDNS", "engine1.example.com alternate-engine.example.com");
            }
        });

        // when
        boolean valid = SsoUtils.isDomainValid(null);

        // then
        Assertions.assertTrue(valid);
    }

    @Test
    public void shouldMatchAppUrlDomainOnSSOEngineUrl() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("SSO_ENGINE_URL", "https://engine.example.com:30003/ovirt-engine");
                put("SSO_ALTERNATE_ENGINE_FQDNS", "alternate-engine.example.com");
            }
        });

        // when
        boolean valid = SsoUtils.isDomainValid("https://engine.example.com:20001/somerest/api_v9");

        // then
        Assertions.assertTrue(valid);
    }

    @Test
    public void shouldMatchAppUrlDomainOnSSOEngineUrlRegardlessUpperCase() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("SSO_ENGINE_URL", "https://engine.EXAMPLE.com:30003/ovirt-engine");
                put("SSO_ALTERNATE_ENGINE_FQDNS", "alternate-engine.example.com");
            }
        });

        // when
        boolean valid = SsoUtils.isDomainValid("https://ENGINE.example.com:20001/somerest/api_v9");

        // then
        Assertions.assertTrue(valid);
    }

    @Test
    public void shouldNotMatchValidAppUrlDomain() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("SSO_ENGINE_URL", "https://engine.example.com:30003/ovirt-engine");
                put("SSO_ALTERNATE_ENGINE_FQDNS", "alternate-engine.example.com");
            }
        });

        // when
        boolean valid = SsoUtils.isDomainValid("https://another-engine.example.com:20001/somerest/api_v9");

        // then
        Assertions.assertFalse(valid);
    }

    @Test
    public void shouldHandleInvalidAlternateSSOEngineUrl() {
        // given
        EngineLocalConfig.getInstance(new HashMap<>() {
            {
                put("SSO_ENGINE_URL", "https://engine.example.com:30003/ovirt-engine");
                put("SSO_ALTERNATE_ENGINE_FQDNS", "  ");
            }
        });

        // when
        boolean valid = SsoUtils.isDomainValid("https://engine.example.com:20001/somerest/api_v9");

        // then
        Assertions.assertTrue(valid);
    }

}
