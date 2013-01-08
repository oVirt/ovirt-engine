package org.ovirt.engine.core.bll.provider.network;

import java.util.List;

import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.common.businessentities.network.Network;

public interface NetworkProviderProxy extends ProviderProxy {

    /**
     * Retrieve a list of all the networks that this provider can provide.
     *
     * @return A list of the networks available from the provider.
     */
    List<Network> getAll();
}
