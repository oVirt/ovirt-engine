package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.NotImplementedException;

public class IVdcQueryable implements Serializable {
    private static final long serialVersionUID = 4622458656586042539L;

    public Object getQueryableId() {
        throw new NotImplementedException("QueryableId not overridden in type");
    }

    public IVdcQueryable() {
    }
}
