package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

/**
 * Interface for business entity. Each entity in the system has an ID which uniquely identifies it (at least) in it's
 * type.
 * @param <T> The type of the id.
 */
public interface BusinessEntity<T extends Serializable> extends Serializable, Managed {

    /**
     * Returns the unique ID of the business entity.
     * @return The unique ID of the business entity.
     */
    T getId();

    /**
     * Sets the unique ID of the business entity
     * @param id The unique ID of the business entity.
     */
    void setId(T id);
}
