package org.ovirt.engine.core.searchbackend;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;

public class SyntaxCheckerFactoryTest {

    @Test
    public void CreateUISyntaxChecker() {
        ISyntaxChecker checker = SyntaxCheckerFactory.CreateUISyntaxChecker("foo");
        Assert.assertNotNull(checker);
        Assert.assertEquals(checker, SyntaxCheckerFactory.CreateUISyntaxChecker("foo"));
        Assert.assertEquals("foo", SyntaxCheckerFactory.getConfigAuthenticationMethod());
    }

    @Test
    public void CreateBackendSyntaxChecker() {
        ISyntaxChecker checker = SyntaxCheckerFactory.CreateBackendSyntaxChecker("foo");
        Assert.assertNotNull(checker);
        Assert.assertEquals(checker, SyntaxCheckerFactory.CreateBackendSyntaxChecker("foo"));
        Assert.assertEquals("foo", SyntaxCheckerFactory.getConfigAuthenticationMethod());
    }

    @Test
    public void CreateADSyntaxChecker() {
        ISyntaxChecker checker = SyntaxCheckerFactory.CreateADSyntaxChecker("foo");
        Assert.assertNotNull(checker);
        Assert.assertEquals(checker, SyntaxCheckerFactory.CreateADSyntaxChecker("foo"));
        Assert.assertEquals("foo", SyntaxCheckerFactory.getConfigAuthenticationMethod());
    }

    @Before
    public void setup() {
        final IConfigUtilsInterface configUtils = Mockito.mock(IConfigUtilsInterface.class);
        Mockito.when(configUtils.GetValue(ConfigValues.SearchResultsLimit, Config.DefaultConfigurationVersion))
                .thenReturn(100);
        Config.setConfigUtils(configUtils);
    }
}
