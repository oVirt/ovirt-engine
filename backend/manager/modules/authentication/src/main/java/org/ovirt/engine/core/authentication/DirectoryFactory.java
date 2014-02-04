package org.ovirt.engine.core.authentication;

import org.ovirt.engine.core.extensions.mgr.Factory;

/**
 * This is just a concrete realization of the generic interface intended to simplify things for developers of directory
 * factories.
 */
public interface DirectoryFactory extends Factory<Directory> {
}
