package org.ovirt.engine.core.common.businessentities.network;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidNameWithDot;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateVmNic;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VmNic</code> defines a type of {@link NetworkInterface} for instances of {@link VM}.
 */
public class VmNic extends NetworkInterface<VmNetworkStatistics> {
    public static final String VALID_MAC_ADDRESS_FORMAT = "(\\p{XDigit}{2}:){5}\\p{XDigit}{2}";

    private static final long serialVersionUID = 7428150502868988886L;

    protected static final String VALIDATION_MESSAGE_MAC_ADDRESS_NOT_NULL =
            "VALIDATION.VM.NETWORK.MAC.ADDRESS.NOT_NULL";
    protected static final String VALIDATION_MESSAGE_NAME_NOT_NULL = "VALIDATION.VM.NETWORK.NAME.NOT_NULL";
    protected static final String VALIDATION_MESSAGE_MAC_ADDRESS_INVALID = "VALIDATION.VM.NETWORK.MAC.ADDRESS.INVALID";

    private Guid vmId;
    private Guid vnicProfileId;
    private Guid vmTemplateId;

    /**
     * Link State of the Nic. <BR>
     * <code>true</code> if UP and <code>false</code> if DOWN.
     */
    private boolean linked;

    public VmNic() {
        super(new VmNetworkStatistics(), VmInterfaceType.pv.getValue());
        linked = true;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
        this.statistics.setVmId(vmId);
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmTemplateId(Guid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
    }

    public Guid getVmTemplateId() {
        return vmTemplateId;
    }

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }

    @NotNull(message = VmNic.VALIDATION_MESSAGE_NAME_NOT_NULL, groups = { CreateEntity.class,
            UpdateEntity.class })
    @ValidNameWithDot(groups = { CreateEntity.class, UpdateEntity.class })
    @Override
    public String getName() {
        return super.getName();
    }

    @NotNull(message = VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_NOT_NULL, groups = { UpdateVmNic.class })
    @Pattern.List({
            @Pattern(regexp = "(^$)|(" + VALID_MAC_ADDRESS_FORMAT + ")",
                    message = VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID,
                    groups = { CreateEntity.class }),
            @Pattern(regexp = VALID_MAC_ADDRESS_FORMAT,
                    message = VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID,
                    groups = { UpdateEntity.class })
    })
    @Override
    public String getMacAddress() {
        return super.getMacAddress();
    }

    public Guid getVnicProfileId() {
        return vnicProfileId;
    }

    public void setVnicProfileId(Guid vnicProfileId) {
        this.vnicProfileId = vnicProfileId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName())
                .append(" {id=")
                .append(getId())
                .append(", vnicProfileId=")
                .append(getVnicProfileId())
                .append(", speed=")
                .append(getSpeed())
                .append(", type=")
                .append(getType())
                .append(", macAddress=")
                .append(getMacAddress())
                .append(", linked=")
                .append(isLinked())
                .append(", vmId=")
                .append(getVmId())
                .append(", vmTemplateId=")
                .append(getVmTemplateId())
                .append("}");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (isLinked() ? 1231 : 1237);
        result = prime * result + ((getVmId() == null) ? 0 : getVmId().hashCode());
        result = prime * result + ((getVnicProfileId() == null) ? 0 : getVnicProfileId().hashCode());
        result = prime * result + ((getVmTemplateId() == null) ? 0 : getVmTemplateId().hashCode());
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
        if (!(obj instanceof VmNic)) {
            return false;
        }
        VmNic other = (VmNic) obj;
        if (!ObjectUtils.objectsEqual(getVmId(), other.getVmId())) {
            return false;
        }
        if (!ObjectUtils.objectsEqual(getVnicProfileId(), other.getVnicProfileId())) {
            return false;
        }
        if (!ObjectUtils.objectsEqual(getVmTemplateId(), other.getVmTemplateId())) {
            return false;
        }
        return true;
    }
}
