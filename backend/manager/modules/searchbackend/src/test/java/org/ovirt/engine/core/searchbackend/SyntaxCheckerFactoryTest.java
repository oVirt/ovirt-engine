package org.ovirt.engine.core.searchbackend;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;

public class SyntaxCheckerFactoryTest {

    @Test
    public void createUISyntaxChecker() {
        ISyntaxChecker checker = SyntaxCheckerFactory.createUISyntaxChecker("foo");
        Assert.assertNotNull(checker);
        Assert.assertEquals(checker, SyntaxCheckerFactory.createUISyntaxChecker("foo"));
        Assert.assertEquals("foo", SyntaxCheckerFactory.getConfigAuthenticationMethod());
    }

    @Test
    public void createBackendSyntaxChecker() {
        ISyntaxChecker checker = SyntaxCheckerFactory.createBackendSyntaxChecker("foo");
        Assert.assertNotNull(checker);
        Assert.assertEquals(checker, SyntaxCheckerFactory.createBackendSyntaxChecker("foo"));
        Assert.assertEquals("foo", SyntaxCheckerFactory.getConfigAuthenticationMethod());
    }

    @Test
    public void createADSyntaxChecker() {
        ISyntaxChecker checker = SyntaxCheckerFactory.createADSyntaxChecker("foo");
        Assert.assertNotNull(checker);
        Assert.assertEquals(checker, SyntaxCheckerFactory.createADSyntaxChecker("foo"));
        Assert.assertEquals("foo", SyntaxCheckerFactory.getConfigAuthenticationMethod());
    }

    @Before
    public void setUp() {
        final IConfigUtilsInterface configUtils = Mockito.mock(IConfigUtilsInterface.class);
        Mockito.when(configUtils.getValue(ConfigValues.SearchResultsLimit, ConfigCommon.defaultConfigurationVersion))
                .thenReturn(100);
        Config.setConfigUtils(configUtils);
    }
}
