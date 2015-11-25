package org.ovirt.engine.core.bll.network.predicate;

import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;

public class NetworkNotInSyncPredicate implements Predicate<NetworkAttachment> {
    @Override
    public boolean test(NetworkAttachment networkAttachment) {
        return !networkAttachment.getReportedConfigurations().isNetworkInSync();
    }
}
