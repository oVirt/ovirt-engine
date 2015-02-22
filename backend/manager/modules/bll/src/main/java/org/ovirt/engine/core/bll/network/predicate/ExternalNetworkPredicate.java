package org.ovirt.engine.core.bll.network.predicate;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.utils.linq.Predicate;

@Singleton
@Named
final class ExternalNetworkPredicate implements Predicate<Network> {
    @Override
    public boolean eval(Network network) {
        return network.isExternal();
    }
}
