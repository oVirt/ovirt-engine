package org.ovirt.engine.core.common.action;

import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;

public class AddManagedBlockStorageDomainParameters extends StorageDomainManagementParameter {

    private Map<String, Object> driverOptions;

    public AddManagedBlockStorageDomainParameters() {
    }

    public AddManagedBlockStorageDomainParameters(StorageDomainStatic storageDomain, Map<String, Object> driverOptions) {
        super(storageDomain);
        this.driverOptions = driverOptions;
    }

    public Map<String, Object> getDriverOptions() {
        return driverOptions;
    }

    public void setDriverOptions(Map<String, Object> driverOptions) {
        this.driverOptions = driverOptions;
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
        return Objects.equals(driverOptions, that.driverOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverOptions);
    }
}
