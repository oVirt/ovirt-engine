package org.ovirt.engine.core.bll.validator;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MacAddressValidationPatterns;

@Singleton
public class VmNicMacsUtils {

    private static final Pattern VALIDATE_MAC_ADDRESS =
            Pattern.compile(MacAddressValidationPatterns.UNICAST_MAC_ADDRESS_FORMAT);

    public ValidationResult validateMacAddress(List<? extends VmNic> vmNics) {
        for (VmNic iface : vmNics) {
            if (iface.getMacAddress() != null) {
                if (!VALIDATE_MAC_ADDRESS.matcher(iface.getMacAddress()).matches()) {
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_INTERFACE_MAC_INVALID,
                            String.format("$IfaceName %1$s", iface.getName()),
                            String.format("$MacAddress %1$s", iface.getMacAddress()));
                }
            }
        }

        return ValidationResult.VALID;
    }

    public <T extends VmNic> ValidationResult validateThereIsEnoughOfFreeMacs(List<T> vmNics,
            MacPool macPool,
            Predicate<T> vnicRequiresNewMacPredicate) {
        Stream<T> vnicsRequireNewMac = vmNics.stream().filter(vnicRequiresNewMacPredicate);
        long requiredMacs = vnicsRequireNewMac.count();

        boolean notEnoughOfMacs = requiredMacs > 0 && macPool.getAvailableMacsCount() < requiredMacs;

        return ValidationResult.failWith(EngineMessage.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES).when(notEnoughOfMacs);
    }

    public void replaceInvalidEmptyStringMacAddressesWithNull(List<VmNetworkInterface> vmNetworkInterfaces) {
        vmNetworkInterfaces.stream()
                .filter(vmNetworkInterface -> StringUtils.isEmpty(vmNetworkInterface.getMacAddress()))
                .forEach(e->e.setMacAddress(null));
    }

}
