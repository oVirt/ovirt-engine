package org.ovirt.engine.core.extensions.mgr;

/**
 * This interface is to be implemented by the classes that create objects using a configuration file.
 *
 * @param <O> the type of the objects created by this factory
 */
public interface Factory<O> {
    /**
     * Returns the string that represents the type of this factory. This type is used to lookup factories without
     * specifying a class name. For example, if a the type of a factory is {@code ldap} then a configuration file can
     * reference it like this:
     *
     * <pre>
     * authenticator.name=example
     * authenticator.type=ldap
     * </pre>
     */
    String getType();

    /**
     * Creates the objects corresponding to the given configuration. The manager will make sure that this method is
     * called only once for each object name and will synchronize the operation, so the factories don't need to worry
     * about concurrent access or caching crated objects.
     *
     * @param config the configuration for the object, already loaded by the manager
     * @throws ConfigurationException if something fails while creating the directory
     */
    O create(Configuration config) throws ConfigurationException;
}
