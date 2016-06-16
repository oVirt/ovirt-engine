package org.ovirt.engine.arquillian.database;

import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

public class MockTransactionManagerRule implements ArquillianRule {

    private MockEJBStrategyRule mockEJBStrategyRule;

    @Override
    public void before(Before test) {
        mockEJBStrategyRule = new MockEJBStrategyRule();
        mockEJBStrategyRule.starting(null);
    }

    @Override
    public void after(After test) {
        mockEJBStrategyRule.finished(null);
    }
}
