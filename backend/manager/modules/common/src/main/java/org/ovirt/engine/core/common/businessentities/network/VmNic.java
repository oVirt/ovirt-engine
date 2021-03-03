package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.utils.MacAddressValidationPatterns;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.ValidNameWithDot;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateVmNic;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VmNic</code> defines a type of {@link NetworkInterface} for instances of {@link VM}.
 */
public class VmNic extends NetworkInterface<VmNetworkStatistics> {

    private static final long serialVersionUID = 7428150502868988886L;

    static final String VALIDATION_MESSAGE_MAC_ADDRESS_NOT_NULL =
            "VALIDATION_VM_NETWORK_MAC_ADDRESS_NOT_NULL";
    static final String VALIDATION_MESSAGE_NAME_NOT_NULL = "VALIDATION_VM_NETWORK_NAME_NOT_NULL";
    public static final String VALIDATION_MESSAGE_MAC_ADDRESS_INVALID = "VALIDATION_VM_NETWORK_MAC_ADDRESS_INVALID";
    public static final String VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST =
            "VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST";

    private Guid vmId;
    private Guid vnicProfileId;

    /**
     * Link State of the Nic. <BR>
     * <code>true</code> if UP and <code>false</code> if DOWN.
     */
    private boolean linked;

    /**
     * <code>true</code> if the vnic configuration on engine
     * is identical to the vnic configuration on the VM.
     */
    private boolean synced;

    public VmNic() {
        super(new VmNetworkStatistics(), VmInterfaceType.pv.getValue());
        linked = true;
        synced = true;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
        this.statistics.setVmId(vmId);
    }

    public Guid getVmId() {
        return vmId;
    }

    @Override
    public Object getQueryableId() {
        return getVmId();
    }

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }

    @NotNull(message = VALIDATION_MESSAGE_NAME_NOT_NULL,
             groups = { CreateEntity.class,
            UpdateEntity.class })
    @ValidNameWithDot(groups = { CreateEntity.class, UpdateEntity.class })
    @Override
    public String getName() {
        return super.getName();
    }

    @NotNull(message = VALIDATION_MESSAGE_MAC_ADDRESS_NOT_NULL,
             groups = UpdateVmNic.class)
    @Pattern.List({
                   @Pattern(regexp = "(^$)|(" + MacAddressValidationPatterns.VALID_MAC_ADDRESS_FORMAT + ")",
                            message = VALIDATION_MESSAGE_MAC_ADDRESS_INVALID,
                            groups = CreateEntity.class),
                   @Pattern(regexp = "(^$)|(" + MacAddressValidationPatterns.NON_MULTICAST_MAC_ADDRESS_FORMAT + ")",
                            message = VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST,
                            groups = CreateEntity.class),
                   @Pattern(regexp = MacAddressValidationPatterns.VALID_MAC_ADDRESS_FORMAT,
                            message = VALIDATION_MESSAGE_MAC_ADDRESS_INVALID,
                            groups = UpdateEntity.class),
                   @Pattern(regexp = MacAddressValidationPatterns.NON_MULTICAST_MAC_ADDRESS_FORMAT,
                            message = VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST,
                            groups = UpdateEntity.class),
                   @Pattern(regexp = MacAddressValidationPatterns.NON_NULLABLE_MAC_ADDRESS_FORMAT,
                            message = VALIDATION_MESSAGE_MAC_ADDRESS_INVALID,
                            groups = { CreateEntity.class, UpdateEntity.class })
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

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("vnicProfileId", getVnicProfileId())
                .append("speed", getSpeed())
                .append("type", getType())
                .append("macAddress", getMacAddress())
                .append("linked", isLinked())
                .append("vmId", getVmId())
                .append("synced", isSynced())
                .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                linked,
                vmId,
                vnicProfileId,
                synced
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmNic)) {
            return false;
        }
        VmNic other = (VmNic) obj;
        return super.equals(obj)
                && Objects.equals(vmId, other.vmId)
                && Objects.equals(vnicProfileId, other.vnicProfileId)
                && linked == other.linked
                && synced == other.synced;
    }

    public boolean isPassthrough() {
        return VmInterfaceType.pciPassthrough.equals(VmInterfaceType.forValue(getType()));
    }
}
