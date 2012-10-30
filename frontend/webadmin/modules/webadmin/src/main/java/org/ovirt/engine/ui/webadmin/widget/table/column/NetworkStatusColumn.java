package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.network_cluster;

import com.google.gwt.resources.client.ImageResource;

public class NetworkStatusColumn extends WebAdminImageResourceColumn<Network> {

    @Override
    public ImageResource getValue(Network nwk) {
        return getValue(nwk.getCluster());
    }

    public ImageResource getValue(network_cluster net_cluster) {
        setEnumTitle(net_cluster.getstatus());
        switch (net_cluster.getstatus()) {
        case Operational:
            return getApplicationResources().upImage();
        case NonOperational:
            return getApplicationResources().downImage();
        default:
            return getApplicationResources().downImage();
        }
    }

}
