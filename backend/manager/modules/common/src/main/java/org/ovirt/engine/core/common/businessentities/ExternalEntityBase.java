package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public interface ExternalEntityBase extends Serializable {
    public abstract String getDescription();

    public abstract String getName();
}
