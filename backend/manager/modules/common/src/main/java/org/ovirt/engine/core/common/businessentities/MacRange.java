package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.utils.MacAddressValidationPatterns;
import org.ovirt.engine.core.compat.Guid;

public class MacRange implements Serializable {
    private static final long serialVersionUID = 5706298268467442698L;

    private Guid macPoolId;

    @Pattern(regexp = MacAddressValidationPatterns.VALID_MAC_ADDRESS_FORMAT,
            message = VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID)
    @NotNull(message= "VALIDATION.VM.NETWORK.MAC.ADDRESS.NOT_NULL")
    private String macFrom;

    @Pattern(regexp = MacAddressValidationPatterns.VALID_MAC_ADDRESS_FORMAT,
            message = VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID)
    @NotNull(message = "VALIDATION.VM.NETWORK.MAC.ADDRESS.NOT_NULL")
    private String macTo;

    public String getMacFrom() {
        return macFrom;
    }

    public void setMacFrom(String macFrom) {
        this.macFrom = macFrom;
    }

    public String getMacTo() {
        return macTo;
    }

    public void setMacTo(String macTo) {
        this.macTo = macTo;
    }

    public Guid getMacPoolId() {
        return macPoolId;
    }

    public void setMacPoolId(Guid macPoolId) {
        this.macPoolId = macPoolId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MacRange)) {
            return false;
        }
        MacRange other = (MacRange) obj;
        return Objects.equals(macFrom, other.macFrom)
                && Objects.equals(macPoolId, other.macPoolId)
                && Objects.equals(macTo, other.macTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                macFrom,
                macPoolId,
                macTo
        );
    }
}
