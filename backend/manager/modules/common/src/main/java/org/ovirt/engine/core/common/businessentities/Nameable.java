package org.ovirt.engine.core.common.businessentities;

/**
 * 2 reason to implement this interface: 1. make our entities consistent 2. help create map of entityName -> entity for
 * cleaner and quicker code See {@link Entities}
 */
public interface Nameable {

    String getName();

}
