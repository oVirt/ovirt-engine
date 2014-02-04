package org.ovirt.engine.core.authentication.nop;

import org.ovirt.engine.core.authentication.Directory;
import org.ovirt.engine.core.authentication.DirectoryFactory;
import org.ovirt.engine.core.extensions.mgr.Configuration;
import org.ovirt.engine.core.extensions.mgr.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a directory without any backend, it just creates the users when requested. This is useful when
 * there is no need for any of the attribures or groups provided by an external directory.
 */
public class NopDirectoryFactory implements DirectoryFactory {
    private static final Logger log = LoggerFactory.getLogger(NopDirectoryFactory.class);

    /**
     * The type supported by this factory.
     */
    private static final String TYPE = "nop";

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
        return new NopDirectory(name);
    }
}
