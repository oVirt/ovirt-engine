package org.ovirt.engine.core.common.businessentities.network;

import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateVmNic;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VmNetworkInterface</code> defines a type of {@link BaseNetworkInterface} for instances of {@link VM}.
 *
 */
public class VmNetworkInterface extends NetworkInterface<VmNetworkStatistics> {
    public static final String VALID_MAC_ADDRESS_FORMAT = "(\\p{XDigit}{2}:){5}\\p{XDigit}{2}";

    private static final long serialVersionUID = 7428150502868988886L;

    protected static final String VALIDATION_MESSAGE_MAC_ADDRESS_NOT_NULL =
            "VALIDATION.VM.NETWORK.MAC.ADDRESS.NOT_NULL";
    protected static final String VALIDATION_MESSAGE_NAME_NOT_NULL = "VALIDATION.VM.NETWORK.NAME.NOT_NULL";
    protected static final String VALIDATION_MESSAGE_MAC_ADDRESS_INVALID = "VALIDATION.VM.NETWORK.MAC.ADDRESS.INVALID";

    private Guid vmId;
    private Guid vnicProfileId;
    private String vmName;
    private Guid vmTemplateId;
    private boolean plugged = true;

    /**
     * Device custom properties
     */
    private Map<String, String> customProperties;

    /**
     * Link State of the Nic. <BR>
     * <code>true</code> if UP and <code>false</code> if DOWN.
     */
    private boolean linked = true;
    private boolean portMirroring;

    public VmNetworkInterface() {
        super(new VmNetworkStatistics(), VmInterfaceType.pv.getValue());
    }

    /**
     * Sets the VM instance id.
     *
     * @param vmId
     *            the id
     */
    public void setVmId(Guid vmId) {
        this.vmId = vmId;
        this.statistics.setVmId(vmId);
    }

    /**
     * Returns the VM instance id.
     *
     * @return the id
     */
    public Guid getVmId() {
        return vmId;
    }

    /**
     * Sets the VM name.
     *
     * @param vmName
     *            the name
     */
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    /**
     * Returns the VM name.
     *
     * @return the name
     */
    public String getVmName() {
        return vmName;
    }

    /**
     * Sets the VM template instance id.
     *
     * @param vmTemplateId
     *            the id
     */
    public void setVmTemplateId(Guid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
    }

    /**
     * Returns the VM template instance id.
     *
     * @return the id
     */
    public Guid getVmTemplateId() {
        return vmTemplateId;
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

    public boolean isPlugged() {
        return plugged;
    }

    public void setPlugged(boolean plugged) {
        this.plugged = plugged;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }

    @NotNull(message = VmNetworkInterface.VALIDATION_MESSAGE_NAME_NOT_NULL, groups = { CreateEntity.class,
            UpdateEntity.class })
    @Override
    public String getName() {
        return super.getName();
    }

    @NotNull(message = VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_NOT_NULL, groups = { UpdateVmNic.class })
    @Pattern.List({
            @Pattern(regexp = "(^$)|(" + VALID_MAC_ADDRESS_FORMAT + ")",
                    message = VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID,
                    groups = { CreateEntity.class }),
            @Pattern(regexp = VALID_MAC_ADDRESS_FORMAT,
                    message = VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID,
                    groups = { UpdateEntity.class })
    })
    @Override
    public String getMacAddress() {
        return super.getMacAddress();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VmNetworkInterface {id=")
                .append(getId())
                .append(", networkName=")
                .append(getNetworkName())
                .append(", speed=")
                .append(getSpeed())
                .append(", type=")
                .append(getType())
                .append(", name=")
                .append(getName())
                .append(", macAddress=")
                .append(getMacAddress())
                .append(", active=")
                .append(isPlugged())
                .append(", customProperties=")
                .append(getCustomProperties())
                .append(", linked=")
                .append(isLinked())
                .append(", portMirroring=")
                .append(isPortMirroring())
                .append(", vmId=")
                .append(getVmId())
                .append(", vnicProfileId=")
                .append(getVnicProfileId())
                .append(", vmName=")
                .append(getVmName())
                .append(", vmTemplateId=")
                .append(getVmTemplateId())
                .append("}");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (plugged ? 1231 : 1237);
        result = prime * result + (customProperties == null ? 0 : customProperties.hashCode());
        result = prime * result + (linked ? 1231 : 1237);
        result = prime * result + ((vmId == null) ? 0 : vmId.hashCode());
        result = prime * result + ((vnicProfileId == null) ? 0 : vnicProfileId.hashCode());
        result = prime * result + ((vmName == null) ? 0 : vmName.hashCode());
        result = prime * result + ((vmTemplateId == null) ? 0 : vmTemplateId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof VmNetworkInterface)) {
            return false;
        }
        VmNetworkInterface other = (VmNetworkInterface) obj;
        if (plugged != other.plugged) {
            return false;
        }

        if (!ObjectUtils.objectsEqual(customProperties, other.customProperties)) {
            return false;
        }

        if (linked != other.linked) {
            return false;
        }
        if (vmId == null) {
            if (other.vmId != null) {
                return false;
            }
        } else if (!vmId.equals(other.vmId)) {
            return false;
        }
        if (vnicProfileId == null) {
            if (other.vnicProfileId != null) {
                return false;
            }
        } else if (!vnicProfileId.equals(other.vnicProfileId)) {
            return false;
        }
        if (vmName == null) {
            if (other.vmName != null) {
                return false;
            }
        } else if (!vmName.equals(other.vmName)) {
            return false;
        }
        if (vmTemplateId == null) {
            if (other.vmTemplateId != null) {
                return false;
            }
        } else if (!vmTemplateId.equals(other.vmTemplateId)) {
            return false;
        }
        return true;
    }

    public boolean isPortMirroring() {
        return portMirroring;
    }

    public void setPortMirroring(boolean portMirroring) {
        this.portMirroring = portMirroring;
    }

    public Guid getVnicProfileId() {
        return vnicProfileId;
    }

    public void setVnicProfileId(Guid vnicProfileId) {
        this.vnicProfileId = vnicProfileId;
    }
}
