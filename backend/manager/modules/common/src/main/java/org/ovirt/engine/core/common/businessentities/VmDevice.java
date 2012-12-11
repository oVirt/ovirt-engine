package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Map;

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
     * The device name.
     */
    private String type;

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
    private Map<String, Object> specParams = null;

    /**
     * The device managed/unmanaged flag
     */
    private boolean isManaged = false;

    /**
     * The device plugged flag
     */
    private Boolean isPlugged = false;

    /**
     * The device read-only flag
     */

    private boolean isReadOnly = false;

    /**
     * The device alias.
     */
    private String alias = "";

    public VmDevice() {
    }

    public VmDevice(VmDeviceId id, String type, String device, String address,
            int bootOrder,
            Map<String, Object> specParams,
            boolean isManaged,
            Boolean isPlugged,
            boolean isReadOnly,
            String alias) {
        super();
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
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        return null;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

    public boolean getIsReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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
        result = prime * result
                + ((specParams == null) ? 0 : specParams.hashCode());
        result = prime * result + (isManaged ? 1 : 0);
        result = prime * result + (isPlugged ? 1 : 0);
        result = prime * result + (isReadOnly ? 1 : 0);
        result = prime * result + alias.hashCode();
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
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (!device.equals(other.device)) {
            return false;
        }
        if (!type.equals(other.type)) {
            return false;
        }
        if (!address.equals(other.address)) {
            return false;
        }
        if (bootOrder != other.bootOrder) {
            return false;
        }
        if (specParams == null) {
            if (other.specParams != null) {
                return false;
            }
        } else if (!specParams.equals(other.specParams)) {
            return false;
        }
        if (isManaged != other.isManaged) {
            return false;
        }
        if (isPlugged != other.isPlugged) {
            return false;
        }
        if (isReadOnly != other.isReadOnly) {
            return false;
        }
        if (!alias.equals(other.alias)) {
            return false;
        }

        return true;
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
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int compareTo(VmDevice other) {
        return BusinessEntityComparator.<VmDevice,VmDeviceId>newInstance().compare(this, other);
    }
}
