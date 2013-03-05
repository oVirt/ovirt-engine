package org.ovirt.engine.core.bll;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigRule;

public class VdsDeployTest {
    @ClassRule
    public static MockConfigRule configRule = new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.keystoreUrl,
            "src/test/resources/engine.p12"),
            MockConfigRule.mockConfig(ConfigValues.CertAlias, "1"),
            MockConfigRule.mockConfig(ConfigValues.keystorePass, "mypass"));

    @Test
    public void getEngineSSHPublicKey() {
        Assert.assertNotNull(VdsDeploy.getEngineSSHPublicKey());
    }
}
