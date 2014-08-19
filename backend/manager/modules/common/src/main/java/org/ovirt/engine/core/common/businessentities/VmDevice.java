package org.ovirt.engine.core.common.businessentities;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

/**
 * The VmDevice represents a managed or unmanaged VM device.<br>
 * The VmDevice data consists of this entity which holds the immutable fields of the device.<br>
 * Each device have a link to its VM id , an address and optional boot order and additional special device parameters. <br>
 * This BE holds both managed (disk, network interface etc.) and unmanaged (sound, video etc.) devices.
 */

public class VmDevice extends IVdcQueryable implements BusinessEntity<VmDeviceId>, Comparable<VmDevice> {

    /**
     * Needed for java serialization/deserialization mechanism.
     */
    private static final long serialVersionUID = 826297167224396854L;

    /**
     * Device id and Vm id pair as a compound key
     */
    private VmDeviceId id;

    /**
     * The device name.
     */
    private String device;

    /**
     * The device type.
     */
    private VmDeviceGeneralType type;

    /**
     * The device address.
     */
    private String address;

    /**
     * The device boot order (if applicable).
     */
    private int bootOrder;

    /**
     * The device special parameters.
     */
    private Map<String, Object> specParams;

    /**
     * The device managed/unmanaged flag
     */
    private boolean isManaged;

    /**
     * The device plugged flag
     */
    private Boolean isPlugged;

    /**
     * The device read-only flag
     */
    private Boolean isReadOnly;

    /**
     * The device flag indicating whether the device
     * is a device from a taken snapshot
     */
    private Guid snapshotId;

    /**
     * The device alias.
     */
    private String alias;

    /**
     * The device logical name.
     */
    private String logicalName;

    /**
     * Map of custom properties
     */
    private Map<String, String> customProperties;

    public VmDevice() {
        isPlugged = Boolean.FALSE;
        alias = "";
    }

    public VmDevice(VmDeviceId id, VmDeviceGeneralType type, String device, String address,
                    int bootOrder,
                    Map<String, Object> specParams,
                    boolean isManaged,
                    Boolean isPlugged,
                    Boolean isReadOnly,
                    String alias,
                    Map<String, String> customProperties,
                    Guid snapshotId,
                    String logicalName) {
        this.id = id;
        this.type = type;
        this.device = device;
        this.address = address;
        this.bootOrder = bootOrder;
        this.specParams = specParams;
        this.isManaged = isManaged;
        this.isPlugged = isPlugged;
        this.isReadOnly = isReadOnly;
        this.alias = alias;
        this.customProperties = customProperties;
        this.snapshotId = snapshotId;
        this.logicalName = logicalName;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public VmDeviceId getId() {
        return id;
    }

    @Override
    public void setId(VmDeviceId id) {
        this.id = id;
    }

    public Guid getDeviceId() {
        return id.getDeviceId();
    }

    public void setDeviceId(Guid deviceId) {
        id.setDeviceId(deviceId);
    }

    public Guid getVmId() {
        return id.getVmId();
    }

    public void setVmId(Guid vmId) {
        this.id.setVmId(vmId);
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public VmDeviceGeneralType getType() {
        return type;
    }

    public void setType(VmDeviceGeneralType type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getBootOrder() {
        return bootOrder;
    }

    public void setBootOrder(int bootOrder) {
        this.bootOrder = bootOrder;
    }

    public Map<String, Object> getSpecParams() {
        return specParams;
    }

    public void setSpecParams(Map<String, Object> specParams) {
        this.specParams = specParams;
    }

    public boolean getIsManaged() {
        return isManaged;
    }

    public void setIsManaged(boolean isManaged) {
        this.isManaged = isManaged;
    }

    public Boolean getIsPlugged() {
        return isPlugged == null ? Boolean.FALSE : isPlugged;
    }

    public void setIsPlugged(Boolean isPlugged) {
        this.isPlugged = isPlugged;
    }

    public Boolean getIsReadOnly() {
        return isReadOnly == null ? Boolean.FALSE : isReadOnly;
    }

    public void setIsReadOnly(Boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + device.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + address.hashCode();
        result = prime * result + bootOrder;
        result = prime * result + ((specParams == null) ? 0 : specParams.hashCode());
        result = prime * result + (isManaged ? 1231 : 1237);
        result = prime * result + (isPlugged ? 1231 : 1237);
        result = prime * result + (getIsReadOnly() ? 1231 : 1237);
        result = prime * result + alias.hashCode();
        result = prime * result + (customProperties == null ? 0 : customProperties.hashCode());
        result = prime * result + (snapshotId == null ? 0 : snapshotId.hashCode());
        result = prime * result + (logicalName == null ? 0 : logicalName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VmDevice)) {
            return false;
        }
        VmDevice other = (VmDevice) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && device.equals(other.device)
                && type.equals(other.type)
                && address.equals(other.address)
                && bootOrder == other.bootOrder
                && ObjectUtils.objectsEqual(specParams, other.specParams)
                && isManaged == other.isManaged
                && getIsPlugged().equals(other.getIsPlugged())
                && getIsReadOnly().equals(other.getIsReadOnly())
                && alias.equals(other.alias)
                && ObjectUtils.objectsEqual(customProperties, other.customProperties)
                && ObjectUtils.objectsEqual(snapshotId, other.snapshotId)
                && ObjectUtils.objectsEqual(logicalName, other.logicalName));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("VmDevice {");
        sb.append("vmId=");
        sb.append(id.getVmId());
        sb.append(", deviceId=");
        sb.append(id.getDeviceId());
        sb.append(", device=");
        sb.append(getDevice());
        sb.append(", type=");
        sb.append(getType());
        sb.append(", bootOrder=");
        sb.append(getBootOrder());
        sb.append(", specParams=");
        sb.append(getSpecParams());
        sb.append(", address=");
        sb.append(getAddress());
        sb.append(", managed=");
        sb.append(getIsManaged());
        sb.append(", plugged=");
        sb.append(getIsPlugged());
        sb.append(", readOnly=");
        sb.append(getIsReadOnly());
        sb.append(", deviceAlias=");
        sb.append(getAlias());
        sb.append(", customProperties=");
        sb.append(getCustomProperties());
        sb.append(", snapshotId=");
        sb.append(getSnapshotId());
        sb.append(", logicalName=");
        sb.append(getLogicalName());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int compareTo(VmDevice other) {
        return BusinessEntityComparator.<VmDevice, VmDeviceId>newInstance().compare(this, other);
    }
}
