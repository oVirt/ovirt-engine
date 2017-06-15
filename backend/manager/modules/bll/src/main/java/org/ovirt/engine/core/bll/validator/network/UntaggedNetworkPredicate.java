package org.ovirt.engine.core.bll.validator.network;

import java.util.function.Predicate;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public final class UntaggedNetworkPredicate implements Predicate<NetworkType> {
    @Override
    public boolean test(NetworkType networkType) {
        return !NetworkType.VLAN.equals(networkType);
    }
}
