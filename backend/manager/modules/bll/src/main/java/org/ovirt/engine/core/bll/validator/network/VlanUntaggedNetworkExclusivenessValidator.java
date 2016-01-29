package org.ovirt.engine.core.bll.validator.network;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.errors.EngineMessage;

@Singleton
@Named
final class VlanUntaggedNetworkExclusivenessValidator implements NetworkExclusivenessValidator {

    private final Predicate<NetworkType> untaggedNetworkPredicate;

    @Inject
    VlanUntaggedNetworkExclusivenessValidator(@Named("untaggedNetworkPredicate") Predicate<NetworkType> untaggedNetworkPredicate) {
        Objects.requireNonNull(untaggedNetworkPredicate, "untaggedNetworkPredicate cannot be null");

        this.untaggedNetworkPredicate = untaggedNetworkPredicate;
    }

    /**
     * Make sure that at most one vlan-untagged network is attached to a given interface.
     *
     * @return true for a valid configuration, otherwise false.
     */
    @Override
    public boolean isNetworkExclusive(List<NetworkType> networksOnIface) {
        return  networksOnIface.stream().filter(untaggedNetworkPredicate).count() <= 1;
    }

    @Override
    public EngineMessage getViolationMessage() {
        return EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_UNTAGGED_NETWORK;
    }
}
