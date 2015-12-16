package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

/**
 * A {@link BusinessEntity} with a status enum.
 */
public interface BusinessEntityWithStatus<ID extends Serializable, Status extends Enum<?>> extends BusinessEntity<ID> {
    Status getStatus();

    void setStatus(Status status);
}
