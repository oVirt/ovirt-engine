package org.ovirt.engine.core.searchbackend;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;

public class SyntaxCheckerFactoryTest {

    @ClassRule
    public static MockConfigRule mcr =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.SearchResultsLimit, 100));

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
}
