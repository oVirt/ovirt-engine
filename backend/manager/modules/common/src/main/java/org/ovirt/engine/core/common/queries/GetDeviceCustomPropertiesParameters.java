package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.compat.Version;

/**
 * Parameters to get available device custom properties for specified device type
 */
public class GetDeviceCustomPropertiesParameters extends QueryParametersBase {
    private static final long serialVersionUID = 6760061352097116149L;

    /**
     * Version of a cluster
     */
    private Version version;

    /**
     * Device type to get custom properties for
     */
    private VmDeviceGeneralType deviceType;

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public VmDeviceGeneralType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(VmDeviceGeneralType deviceType) {
        this.deviceType = deviceType;
    }

}
