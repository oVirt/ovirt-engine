package org.ovirt.engine.core.common.validation;

import static org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol.STATIC_IP;
import static org.ovirt.engine.core.compat.StringHelper.isNullOrEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
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
        Ipv4BootProtocol bootProtocol = iface.getIpv4BootProtocol();
        String address = iface.getIpv4Address();

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

        if (!validateLabel(iface)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("IMPROPER_INTERFACE_IS_LABELED").addConstraintViolation();
        }

        return true;
    }

    /**
     * Validate the slave is configured properly by the following traits:
     * <ul>
     * <li>No network name</li>
     * <li>No vlan configured: {@link VdsNetworkInterface#getBaseInterface()} and
     * {@link VdsNetworkInterface#getVlanId()} are <code>null</code></li>
     * </ul>
     *
     * @param slave
     *            The network interface represents a slave
     * @return {@code true} if the slave is configured properly.
     */
    private boolean validateSlave(VdsNetworkInterface slave) {
        return isEmpty(slave.getNetworkName())
                && isEmpty(slave.getBaseInterface())
                && slave.getVlanId() == null;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Checks if a given nic is labeled properly: either an interface or a bond (not a slave nor vlan).
     *
     * @param iface
     *            the nic to check
     * @return <code>true</code> iff the nic is properly labeled or if no labels provided for it, else
     *         <code>false</code>
     */
    private boolean validateLabel(VdsNetworkInterface iface) {
        return iface.getLabels() == null || iface.getLabels().isEmpty() ? true : isEmpty(iface.getBondName())
                && iface.getVlanId() == null;
    }
}
