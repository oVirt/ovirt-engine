package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class MacAddressValidator {
    private final MacPool macPool;
    private final String macAddress;

    public MacAddressValidator(MacPool macPool, String macAddress) {
        this.macPool = macPool;
        this.macAddress = macAddress;
    }

    public ValidationResult isMacAssignableValidator() {
        boolean allowDupMacs = macPool.isDuplicateMacAddressesAllowed();
        boolean illegalDuplicateMacUsage = !allowDupMacs && macPool.isMacInUse(macAddress);

        EngineMessage failMessage = EngineMessage.NETWORK_MAC_ADDRESS_IN_USE;
        return ValidationResult
                .failWith(failMessage, ReplacementUtils.getVariableAssignmentString(failMessage, macAddress))
                .when(illegalDuplicateMacUsage);
    }
}
