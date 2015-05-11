package org.ovirt.engine.core.bll.validator.network;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;
import org.ovirt.engine.core.common.errors.EngineMessage;

@Singleton
@Named
final class LegacyNetworkExclusivenessValidator implements NetworkExclusivenessValidator {

    /**
     * Make sure that if the given interface has a VM network on it then there is nothing else on the interface, or if
     * the given interface is a VLAN network, then there is no VM network on the base interface.<br>
     * Other combinations are either legal or illegal but are not a concern of this method.
     *
     * @return true if for given nic there's either nothing, sole VM network,
     * or at most one NON-VM network with any number of VLANs.
     */
    @Override
    public boolean isNetworkExclusive(List<NetworkType> networksOnInterface) {
        if (networksOnInterface.size() <= 1) {
            return true;
        }

        Bag networkTypes = new HashBag(networksOnInterface);
        boolean vmNetworkIsNotSoleNetworkAssigned = networkTypes.contains(NetworkType.VM) && networkTypes.size() > 1;
        boolean moreThanOneNonVmNetworkAssigned = networkTypes.getCount(NetworkType.NON_VM) > 1;

        return !(vmNetworkIsNotSoleNetworkAssigned || moreThanOneNonVmNetworkAssigned);
    }

    @Override
    public EngineMessage getViolationMessage() {
        return EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK;
    }
}
