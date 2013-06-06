package org.ovirt.engine.core.common.validation;

import static org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol.STATIC_IP;
import static org.ovirt.engine.core.compat.StringHelper.isNullOrEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.validation.annotation.ValidNetworkConfiguration;

public class NetworkInterfaceValidator implements ConstraintValidator<ValidNetworkConfiguration, VdsNetworkInterface> {

    @Override
    public void initialize(ValidNetworkConfiguration constraintAnnotation) {
    }

    /**
     * validate the following:
     * <ul>
     * <li>an interface must have an address when the boot protocol is static
     * </ul>
     */
    @Override
    public boolean isValid(VdsNetworkInterface iface, ConstraintValidatorContext context) {
        NetworkBootProtocol bootProtocol = iface.getBootProtocol();
        String address = iface.getAddress();

        if (bootProtocol != null && bootProtocol == STATIC_IP) {
            if (isNullOrEmpty(address)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("NETWORK_ADDR_MANDATORY_IN_STATIC_IP")
                        .addNode("address").addConstraintViolation();
                return false;
            }
        }

        if (!isEmpty(iface.getBondName()) && !validateSlave(iface)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("SLAVE_INTERFACE_IS_MISCONFIGURED").addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * Validate the slave is configured properly by the following traits:
     * <ul>
     * <li>No network name</li>
     * <li>No boot protocol: no address, subnet, gateway or boot protocol</li>
     * <li>No vlan configured, either as part of the name or explicitly by {@link VdsNetworkInterface#getVlanId()}</li>
     * </ul>
     *
     * @param slave
     *            The network interface represents a slave
     * @return {@code true} if the slave is configured properly.
     */
    private boolean validateSlave(VdsNetworkInterface slave) {
        return (slave.getBootProtocol() == null || slave.getBootProtocol() == NetworkBootProtocol.NONE)
                && isEmpty(slave.getNetworkName())
                && isEmpty(slave.getAddress())
                && isEmpty(slave.getSubnet())
                && isEmpty(slave.getGateway())
                && (slave.getName() == null || !slave.getName().contains("."))
                && slave.getVlanId() == null;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
