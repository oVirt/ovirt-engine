package org.ovirt.engine.core.bll.common.predicates;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.utils.linq.Predicate;

public final class VmNetworkCanBeUpdatedPredicate implements Predicate<VmNetworkInterface> {

    private static final Predicate<VmNetworkInterface> instance = new VmNetworkCanBeUpdatedPredicate();

    public static Predicate<VmNetworkInterface> getInstance() {
        return instance;
    }

    @Override
    public boolean eval(VmNetworkInterface vNic) {
        return vNic.isPlugged();
    }
}
