package org.ovirt.engine.arquillian.database;

import org.jboss.arquillian.core.spi.LoadableExtension;

public class ArquillianRuleExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(ArquillianRuleExecutor.class);
        builder.service(ArquillianRule.class, RollbackRule.class);
    }
}
