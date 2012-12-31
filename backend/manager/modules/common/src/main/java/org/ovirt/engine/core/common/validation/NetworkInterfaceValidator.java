package org.ovirt.engine.core.common.validation;

import static org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol.StaticIp;
import static org.ovirt.engine.core.compat.StringHelper.isNullOrEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ValidNetworkConfiguration;

public class NetworkInterfaceValidator implements ConstraintValidator<ValidNetworkConfiguration, VdsNetworkInterface> {

    @Override
    public void initialize(ValidNetworkConfiguration constraintAnnotation) {
    }

    /**
     * validate the following:
     * <ul>
     * <li>an interface must have an address when the boot protocol is static
     * <li>its legal to state the network gateway only to the management network
     * </ul>
     */
    @Override
    public boolean isValid(VdsNetworkInterface iface, ConstraintValidatorContext context) {
        NetworkBootProtocol bootProtocol = iface.getBootProtocol();
        String address = iface.getAddress();

        if (bootProtocol != null && bootProtocol == StaticIp) {
            if (isNullOrEmpty(address)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("NETWORK_ADDR_MANDATORY_IN_STATIC_IP")
                        .addNode("address").addConstraintViolation();
                return false;
            }
        }

        if (!Config.<String> GetValue(ConfigValues.ManagementNetwork).equals(iface.getNetworkName())
                && !isNullOrEmpty(iface.getGateway())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("NETWORK_ATTACH_ILLEGAL_GATEWAY")
                    .addNode("gateway").addConstraintViolation();
            return false;
        }

        return true;
    }

}
