package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class ManagedBlockStorage implements BusinessEntity<Guid>, Serializable {

    private static final long serialVersionUID = -649485547437787568L;
    private Guid id;
    private Map<String, Object> driverOptions;

    public ManagedBlockStorage() {
    }

    public ManagedBlockStorage(Guid id, Map<String, Object> driver_options) {
        this.id = id;
        this.driverOptions = driver_options;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Map<String, Object> getDriverOptions() {
        return driverOptions;
    }

    public void setDriverOptions(Map<String, Object> driverOptions) {
        this.driverOptions = driverOptions;
    }
}
