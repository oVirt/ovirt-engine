package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HostDeviceView other = (HostDeviceView) o;

        return ObjectUtils.objectsEqual(configuredVmId, other.configuredVmId) &&
                ObjectUtils.objectsEqual(attachedVmNames, other.attachedVmNames) &&
                ObjectUtils.objectsEqual(runningVmName, other.runningVmName) &&
                ObjectUtils.objectsEqual(iommuPlaceholder, other.iommuPlaceholder);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (configuredVmId != null ? configuredVmId.hashCode() : 0);
        result = 31 * result + (attachedVmNames != null ? attachedVmNames.hashCode() : 0);
        result = 31 * result + (runningVmName != null ? runningVmName.hashCode() : 0);
        result = 31 * result + (iommuPlaceholder ? 1 : 0);
        return result;
    }
}
