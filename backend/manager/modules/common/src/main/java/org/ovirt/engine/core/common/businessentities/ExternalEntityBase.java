package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public interface ExternalEntityBase extends Serializable {
    String getDescription();

    String getName();

    default String getViewableName() {
        return getName();
    }
}
