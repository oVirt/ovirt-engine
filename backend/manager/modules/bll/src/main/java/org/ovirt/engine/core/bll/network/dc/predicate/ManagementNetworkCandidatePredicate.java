package org.ovirt.engine.core.bll.network.dc.predicate;

import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.Network;

@Singleton
@Named
public class ManagementNetworkCandidatePredicate implements Predicate<Network> {

    private final Predicate<Network> externalNetworkPredicate;

    @Inject
    public ManagementNetworkCandidatePredicate(
            @Named("externalNetworkPredicate") Predicate<Network> externalNetworkPredicate) {
        Objects.requireNonNull(externalNetworkPredicate, "externalNetworkPredicate cannot be null");

        this.externalNetworkPredicate = externalNetworkPredicate.negate();
    }

    @Override
    public boolean test(Network network) {
        return externalNetworkPredicate.test(network);
    }
}
