package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.utils.MacAddressValidationPatterns;
import org.ovirt.engine.core.compat.Guid;

public class MacRange implements Serializable {
    private static final long serialVersionUID = 5706298268467442698L;

    private Guid macPoolId;

    @Pattern.List({
                   @Pattern(regexp = MacAddressValidationPatterns.VALID_MAC_ADDRESS_FORMAT,
                            message = VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID),
                   @Pattern(regexp = MacAddressValidationPatterns.NON_MULTICAST_MAC_ADDRESS_FORMAT,
                            message = VmNic.VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST)
    })
    @NotNull(message= "VALIDATION.VM.NETWORK.MAC.ADDRESS.NOT_NULL")
    private String macFrom;

    @Pattern.List({
                   @Pattern(regexp = MacAddressValidationPatterns.VALID_MAC_ADDRESS_FORMAT,
                            message = VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID),
                   @Pattern(regexp = MacAddressValidationPatterns.NON_MULTICAST_MAC_ADDRESS_FORMAT,
                            message = VmNic.VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST)
    })
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MacRange)) {
            return false;
        }
        MacRange other = (MacRange) obj;
        if (macFrom == null) {
            if (other.macFrom != null) {
                return false;
            }
        } else if (!macFrom.equals(other.macFrom)) {
            return false;
        }
        if (macPoolId == null) {
            if (other.macPoolId != null) {
                return false;
            }
        } else if (!macPoolId.equals(other.macPoolId)) {
            return false;
        }
        if (macTo == null) {
            if (other.macTo != null) {
                return false;
            }
        } else if (!macTo.equals(other.macTo)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((macFrom == null) ? 0 : macFrom.hashCode());
        result = prime * result + ((macPoolId == null) ? 0 : macPoolId.hashCode());
        result = prime * result + ((macTo == null) ? 0 : macTo.hashCode());
        return result;
    }
}
