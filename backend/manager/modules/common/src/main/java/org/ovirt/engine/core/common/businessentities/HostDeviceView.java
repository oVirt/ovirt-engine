package org.ovirt.engine.core.common.businessentities;

import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class HostDeviceView extends HostDevice {

    /** VM view only field */
    private Guid configuredVmId;
    /** VM view only field */
    private boolean iommuPlaceholder;

    private List<String> attachedVmNames;
    private String runningVmName;

    public void setAttachedVmNames(List<String> attachedVmNames) {
        this.attachedVmNames = attachedVmNames;
    }

    public List<String> getAttachedVmNames() {
        return attachedVmNames;
    }

    public void setRunningVmName(String runningVmName) {
        this.runningVmName = runningVmName;
    }

    public String getRunningVmName() {
        return runningVmName;
    }

    public Guid getRunningVmId() {
        return getVmId();
    }

    public Guid getConfiguredVmId() {
        return configuredVmId;
    }

    public void setConfiguredVmId(Guid configuredVmId) {
        this.configuredVmId = configuredVmId;
    }

    public boolean isIommuPlaceholder() {
        return iommuPlaceholder;
    }

    public void setIommuPlaceholder(boolean iommuPlaceholder) {
        this.iommuPlaceholder = iommuPlaceholder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HostDeviceView)) {
            return false;
        }

        HostDeviceView other = (HostDeviceView) o;
        return super.equals(other)
                && Objects.equals(configuredVmId, other.configuredVmId)
                && Objects.equals(attachedVmNames, other.attachedVmNames)
                && Objects.equals(runningVmName, other.runningVmName)
                && Objects.equals(iommuPlaceholder, other.iommuPlaceholder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                configuredVmId,
                attachedVmNames,
                runningVmName,
                iommuPlaceholder
        );
    }
}
