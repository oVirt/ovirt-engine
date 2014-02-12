package org.ovirt.engine.core.aaa;

import org.ovirt.engine.core.extensions.mgr.Factory;

/**
 * This is just a concrete realization of the generic interface intended to simplify things for developers of
 * authenticator factories.
 */
public interface AuthenticatorFactory extends Factory<Authenticator> {
}
