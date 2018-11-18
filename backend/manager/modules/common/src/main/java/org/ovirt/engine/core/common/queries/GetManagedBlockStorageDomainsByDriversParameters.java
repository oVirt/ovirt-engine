package org.ovirt.engine.core.common.queries;

import java.util.Map;

public class GetManagedBlockStorageDomainsByDriversParameters extends QueryParametersBase {

    private static final long serialVersionUID = 7971855787265007865L;

    private Map<String, Object> driverOption;

    private Map<String, Object> driverSensitiveOption;

    public GetManagedBlockStorageDomainsByDriversParameters() {
    }

    /**
     * Instantiate the parameter class by storage pool id and connection
     * @param driverOption
     *            drivers for creating the offload storage
     * @param driverSensitiveOption
     *            sensitive encrypted drivers for creating the offload storage
     */
    public GetManagedBlockStorageDomainsByDriversParameters(Map<String, Object> driverOption, Map<String, Object> driverSensitiveOption) {
        setDriverOption(driverOption);
        setDriverSensitiveOption(driverSensitiveOption);
    }

    public Map<String, Object> getDriverOption() {
        return driverOption;
    }

    public void setDriverOption(Map<String, Object> driverOption) {
        this.driverOption = driverOption;
    }

    public Map<String, Object> getDriverSensitiveOption() {
        return driverSensitiveOption;
    }

    public void setDriverSensitiveOption(Map<String, Object> driverSensitiveOption) {
        this.driverSensitiveOption = driverSensitiveOption;
    }
}
