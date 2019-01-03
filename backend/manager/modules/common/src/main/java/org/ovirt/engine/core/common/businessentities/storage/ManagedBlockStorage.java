package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class ManagedBlockStorage implements BusinessEntity<Guid>, Serializable {

    private static final long serialVersionUID = -649485547437787568L;
    private Guid id;
    private Map<String, Object> driverOptions;
    private Map<String, Object> driverSensitiveOptions;

    public ManagedBlockStorage() {
    }

    public ManagedBlockStorage(Guid id, Map<String, Object> driver_options, Map<String, Object> driverSensitiveOptions) {
        this.id = id;
        this.driverOptions = driver_options;
        this.driverSensitiveOptions = driverSensitiveOptions;
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

    public void setDriverSensitiveOptions(Map<String, Object> driverSensitiveOptions) {
        this.driverSensitiveOptions = driverSensitiveOptions;
    }

    public Map<String, Object> getDriverSensitiveOptions() {
        return this.driverSensitiveOptions;
    }

    public Map<String, Object> getAllDriverOptions() {
        Map<String, Object> driverOptions = new HashMap<>(getDriverOptions());

        if (getDriverSensitiveOptions() != null) {
            driverOptions.putAll(getDriverSensitiveOptions());
        }

        return driverOptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ManagedBlockStorage)) {
            return false;
        }
        ManagedBlockStorage that = (ManagedBlockStorage) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(driverOptions, that.driverOptions) &&
                Objects.equals(driverSensitiveOptions, that.driverSensitiveOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, driverOptions, driverSensitiveOptions);
    }
}
