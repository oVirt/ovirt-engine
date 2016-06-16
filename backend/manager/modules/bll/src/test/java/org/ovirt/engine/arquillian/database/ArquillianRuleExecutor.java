package org.ovirt.engine.arquillian.database;

import java.util.Collection;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

public class ArquillianRuleExecutor {

    @Inject
    Instance<ServiceLoader> serviceLoader;

    Collection<TestEnricher> testEnrichers;
    private Collection<ArquillianRule> arquillianRules;

    public void beforeSuite(@Observes BeforeSuite test) throws IllegalAccessException {
        testEnrichers = serviceLoader.get().all(TestEnricher.class);
        arquillianRules = serviceLoader.get().all(ArquillianRule.class);
    }

    public void before(@Observes Before test) throws IllegalAccessException {
        for (ArquillianRule rule : arquillianRules) {
            for (TestEnricher enricher : testEnrichers) {
                enricher.enrich(rule);
            }
            rule.before(test);
        }
    }

    public void after(@Observes After test) throws IllegalAccessException {
        for (ArquillianRule rule : arquillianRules) {
            rule.after(test);
        }
    }
}
