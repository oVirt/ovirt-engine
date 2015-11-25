package org.ovirt.engine.core.bll.network.predicate;

import java.util.function.Predicate;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.Network;

@Singleton
@Named
final class ExternalNetworkPredicate implements Predicate<Network> {
    @Override
    public boolean test(Network network) {
        return network.isExternal();
    }
}
