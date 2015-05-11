package org.ovirt.engine.core.bll.validator.network;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.Predicate;

@Named
@Singleton
public final class UntaggedNetworkPredicate implements Predicate {
    @Override
    public boolean evaluate(Object networkType) {
        return !NetworkType.VLAN.equals(networkType);
    }
}
