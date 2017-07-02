package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public interface Queryable extends Serializable {
    Object getQueryableId();
}
