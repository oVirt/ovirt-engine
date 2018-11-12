package org.ovirt.engine.core.common.action;

import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;

public class AddManagedBlockStorageDomainParameters extends StorageDomainManagementParameter {
    public static final String VOLUME_BACKEND_NAME = "volume_backend_name";

    private Map<String, Object> driverOptions;
    private Map<String, Object> driverSensitiveOptions;

    public AddManagedBlockStorageDomainParameters() {
    }

    public AddManagedBlockStorageDomainParameters(StorageDomainStatic storageDomain,
            String driverName, Map<String, Object> driverOptions, Map<String, Object> driverSensitiveOptions) {
        super(storageDomain);
        this.driverOptions = driverOptions;
        this.driverSensitiveOptions = driverSensitiveOptions;
    }

    public Map<String, Object> getDriverOptions() {
        return driverOptions;
    }

    public void setDriverOptions(Map<String, Object> driverOptions) {
        this.driverOptions = driverOptions;
    }

    public void setSriverSensitiveOptions(Map<String, Object> driverSensitiveOptions) {
        this.driverSensitiveOptions = driverSensitiveOptions;
    }

    public Map<String, Object> getDriverSensitiveOptions() {
        return this.driverSensitiveOptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AddManagedBlockStorageDomainParameters)) {
            return false;
        }

        AddManagedBlockStorageDomainParameters that = (AddManagedBlockStorageDomainParameters) o;
        return Objects.equals(driverOptions, that.driverOptions) &&
                Objects.equals(driverSensitiveOptions, that.driverSensitiveOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverOptions, driverSensitiveOptions);
    }
}
