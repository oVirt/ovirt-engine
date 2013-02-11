package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.IAsyncConverter;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public abstract class NetworkBehavior {

    public void initNetworks(final boolean hotUpdateSupported, final Guid clusterId, AsyncQuery query) {

        query.converterCallback = new IAsyncConverter() {

            @Override
            public Object Convert(Object returnValue, AsyncQuery asyncQuery) {
                ArrayList<Network> networks = new ArrayList<Network>();
                if (returnValue == null) {
                    return networks;
                }

                for (Network a : (ArrayList<Network>) returnValue) {
                    if (a.isVmNetwork()) {
                        networks.add(a);
                    }
                }

                if (hotUpdateSupported) {
                    networks.add(null);
                }

                Collections.sort(networks, new Comparator<Network>() {

                    private LexoNumericComparator lexoNumeric = new LexoNumericComparator();

                    @Override
                    public int compare(Network net1, Network net2) {
                        if (net1 == null) {
                            return net2 == null ? 0 : 1;
                        } else if (net2 == null) {
                            return -1;
                        }
                        return lexoNumeric.compare(net1.getName(), net2.getName());
                    }

                });

                return networks;
            }
        };

        AsyncDataProvider.getClusterNetworkList(query, clusterId);
    }

    public abstract void initSelectedNetwork(ListModel networksList, VmNetworkInterface networkInterface);
}
