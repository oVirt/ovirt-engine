package org.ovirt.engine.core.bll.validator;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.ejb.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MacAddressValidationPatterns;

@Singleton
public class VmNicMacsUtils {

    private static final Pattern VALIDATE_MAC_ADDRESS =
            Pattern.compile(MacAddressValidationPatterns.UNICAST_MAC_ADDRESS_FORMAT);

    private Stream<? extends VmNic> streamOfFilteredNics(List<? extends VmNic> vmNics, boolean havingMacAddress) {
        return vmNics.stream().filter(vmNic->StringUtils.isEmpty(vmNic.getMacAddress()) != havingMacAddress);
    }

    public ValidationResult validateMacAddress(List<? extends VmNic> vmNics) {
        for (VmNic iface : vmNics) {
            if (!StringUtils.isEmpty(iface.getMacAddress())) {
                if(!VALIDATE_MAC_ADDRESS.matcher(iface.getMacAddress()).matches()) {
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_INTERFACE_MAC_INVALID,
                            String.format("$IfaceName %1$s", iface.getName()),
                            String.format("$MacAddress %1$s", iface.getMacAddress()));
                }
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult validateThereIsEnoughOfFreeMacs(List<? extends VmNic> vmNics, MacPool macPool) {
        long requiredMacs = streamOfFilteredNics(vmNics, true).count();
        boolean notEnoughOfMacs = requiredMacs > 0 && macPool.getAvailableMacsCount() < requiredMacs;

        return ValidationResult.failWith(EngineMessage.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES) .when(notEnoughOfMacs);
    }
}
