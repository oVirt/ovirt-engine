package org.ovirt.engine.core.utils.ejb;

/**
 * Enum that defines container managed resources (beans, transaction manager, data source, etc...) Please use this enum
 * with EJBUtils in order to perform lookups
 *
 *
 */
public enum ContainerManagedResourceType {

    TRANSACTION_MANAGER,
    DATA_SOURCE,
}
