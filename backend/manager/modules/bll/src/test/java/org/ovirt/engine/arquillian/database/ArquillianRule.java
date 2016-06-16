package org.ovirt.engine.arquillian.database;

import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;

public interface ArquillianRule {

    void before(Before test);

    void after(After test);
}
