package org.ovirt.engine.core.aaa.internal;

import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.DirectoryFactory;
import org.ovirt.engine.core.extensions.mgr.Configuration;
import org.ovirt.engine.core.extensions.mgr.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalDirectoryFactory implements DirectoryFactory {
    private static final Logger log = LoggerFactory.getLogger(InternalDirectoryFactory.class);

    /**
     * The type supported by this factory.
     */
    private static final String TYPE = "internal";

    // Names of the configuration parameters:
    private static final String NAME_PARAMETER = "name";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Directory create(Configuration config) throws ConfigurationException {
        // Get the name of the directory:
        String name = config.getInheritedString(NAME_PARAMETER);
        if (name == null) {
            throw new ConfigurationException(
                "The configuration file \"" + config.getFile().getAbsolutePath() + "\" doesn't contain the name of " +
                "the directory."
            );
        }

        // We are good, create the directory:
        return new InternalDirectory(name);
    }
}
