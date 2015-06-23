package org.ovirt.engine.core.bll.validator;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;

//TODO: refactor LabelNicCommand to use this validator
public class HostInterfaceValidator {

    private final VdsNetworkInterface iface;

    public HostInterfaceValidator(VdsNetworkInterface iface) {
        this.iface = iface;
    }

    public ValidationResult interfaceExists() {
        return ValidationResult.failWith(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST).when(iface == null);
    }

    public ValidationResult interfaceInHost(Guid hostId) {
        return ValidationResult.failWith(VdcBllMessages.NIC_NOT_EXISTS_ON_HOST).when(!iface.getVdsId().equals(hostId));
    }

    public ValidationResult validBond(List<VdsNetworkInterface> nics) {
        if (!Boolean.TRUE.equals(iface.getBonded())) {
            return ValidationResult.VALID;
        }

        int slavesCount = 0;
        for (VdsNetworkInterface nic : nics) {
            if (StringUtils.equals(nic.getName(), iface.getBondName())) {
                slavesCount++;
                if (slavesCount == 2) {
                    break;
                }
            }
        }

        return ValidationResult.failWith(VdcBllMessages.NETWORK_BONDS_INVALID_SLAVE_COUNT,
                "$NETWORK_BONDS_INVALID_SLAVE_COUNT_LIST " + slavesCount).when(slavesCount < 2);
    }

    public ValidationResult networkCanBeAttached() {
        return ValidationResult.failWith(VdcBllMessages.CANNOT_ADD_NETWORK_ATTACHMENT_ON_SLAVE_OR_VLAN)
                .when(NetworkUtils.isVlan(iface) || iface.isPartOfBond());
    }

}
