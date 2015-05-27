package org.ovirt.engine.core.bll.network.predicate;

import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.utils.linq.Predicate;

public class NetworkNotInSyncPredicate implements Predicate<NetworkAttachment> {
    @Override
    public boolean eval(NetworkAttachment networkAttachment) {
        return !networkAttachment.getReportedConfigurations().isNetworkInSync();
    }
}
