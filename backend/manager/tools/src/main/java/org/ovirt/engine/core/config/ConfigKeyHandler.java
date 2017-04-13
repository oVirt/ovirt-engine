package org.ovirt.engine.core.config;

import org.ovirt.engine.core.config.entity.ConfigKey;

@FunctionalInterface
public interface ConfigKeyHandler {
    public boolean handle(ConfigKey key);
}
