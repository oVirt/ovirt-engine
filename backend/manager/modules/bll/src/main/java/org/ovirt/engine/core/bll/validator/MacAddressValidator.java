package org.ovirt.engine.core.bll.validator;

import java.util.Optional;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacsUsedAcrossWholeSystem;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class MacAddressValidator {

    public static final String VAR_MAC_ADDRESS = "MacAddress";
    public static final String VAR_VM_NAME = "VmName";

    private final MacPool macPool;
    private final String macAddress;

    private MacsUsedAcrossWholeSystem macsUsedAcrossWholeSystem;

    public MacAddressValidator(MacPool macPool,
            String macAddress,
            MacsUsedAcrossWholeSystem macsUsedAcrossWholeSystem) {
        this.macPool = macPool;
        this.macAddress = macAddress;
        this.macsUsedAcrossWholeSystem = macsUsedAcrossWholeSystem;
    }

    public ValidationResult isMacAssignableValidator() {
        boolean allowDupMacs = macPool.isDuplicateMacAddressesAllowed();
        boolean illegalDuplicateMacUsage = !allowDupMacs && macPool.isMacInUse(macAddress);

        if (illegalDuplicateMacUsage) {
            Optional<VM> optionalVm = macsUsedAcrossWholeSystem.getVmUsingMac(macPool.getId(), macAddress);
            if (!optionalVm.isPresent()) {
                optionalVm = macsUsedAcrossWholeSystem.getSnapshotUsingMac(macPool.getId(), macAddress);
            }

            return new ValidationResult(EngineMessage.NETWORK_MAC_ADDRESS_IN_USE,
                    ReplacementUtils.createSetVariableString(VAR_MAC_ADDRESS, macAddress),
                    ReplacementUtils.createSetVariableString(VAR_VM_NAME,
                            optionalVm.isPresent() ? optionalVm.get().getName() : ""));
        }

        return ValidationResult.VALID;
    }
}
