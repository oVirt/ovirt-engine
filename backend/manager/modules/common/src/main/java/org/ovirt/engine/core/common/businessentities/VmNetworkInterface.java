package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Arrays;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateVmNic;
import org.ovirt.engine.core.compat.NGuid;

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

    private NGuid vmId;
    private String vmName;
    private NGuid vmTemplateId;
    /* status of the nic. Active nic is one that is plugged to its VM */
    private boolean active = true;

    /**
     * Link State of the Nic. <BR>
     * <code>true</code> if UP and <code>false</code> if DOWN.
     */
    private boolean linked = true;
    private boolean portMirroring;

    private static final ArrayList<String> _changeablePropertiesList =
            new ArrayList<String>(Arrays.asList(new String[] {
                    "Id", "Name", "MacAddress", "NetworkName", "Type", "Speed", "Statistics", "VmId", "VmName",
                    "VmTemplateId", "PortMirroring", "Linked"
            }));

    public VmNetworkInterface() {
        super(new VmNetworkStatistics(), VmInterfaceType.pv.getValue());
    }

    /**
     * Sets the VM instance id.
     *
     * @param vmId
     *            the id
     */
    public void setVmId(NGuid vmId) {
        this.vmId = vmId;
        this.statistics.setVmId(vmId);
    }

    /**
     * Returns the VM instance id.
     *
     * @return the id
     */
    public NGuid getVmId() {
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
    public void setVmTemplateId(NGuid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
    }

    /**
     * Returns the VM template instance id.
     *
     * @return the id
     */
    public NGuid getVmTemplateId() {
        return vmTemplateId;
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        return _changeablePropertiesList;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
                .append(isActive())
                .append(", linked=")
                .append(isLinked())
                .append(", portMirroring=")
                .append(isPortMirroring())
                .append(", vmId=")
                .append(getVmId())
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
        result = prime * result + (active ? 1231 : 1237);
        result = prime * result + (linked ? 1231 : 1237);
        result = prime * result + ((vmId == null) ? 0 : vmId.hashCode());
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
        if (active != other.active) {
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
}
