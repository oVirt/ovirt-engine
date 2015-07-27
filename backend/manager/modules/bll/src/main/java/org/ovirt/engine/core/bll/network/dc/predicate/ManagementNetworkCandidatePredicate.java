package org.ovirt.engine.core.bll.network.dc.predicate;

import static org.ovirt.engine.core.utils.linq.LinqUtils.not;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.utils.linq.Predicate;

@Singleton
@Named
public class ManagementNetworkCandidatePredicate implements Predicate<Network> {

    private final Predicate<Network> externalNetworkPredicate;

    @Inject
    public ManagementNetworkCandidatePredicate(
            @Named("externalNetworkPredicate") Predicate<Network> externalNetworkPredicate) {
        Objects.requireNonNull(externalNetworkPredicate, "externalNetworkPredicate cannot be null");

        this.externalNetworkPredicate = not(externalNetworkPredicate);
    }

    @Override
    public boolean eval(Network network) {
        return externalNetworkPredicate.eval(network);
    }
}
