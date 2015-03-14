package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

public class NetworkStatusColumn extends AbstractImageResourceColumn<Network> {

    private final static ApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(Network nwk) {
        return getValue(nwk.getCluster());
    }

    public ImageResource getValue(NetworkCluster net_cluster) {
        setEnumTitle(net_cluster.getStatus());
        switch (net_cluster.getStatus()) {
        case OPERATIONAL:
            return resources.upImage();
        case NON_OPERATIONAL:
            return resources.downImage();
        default:
            return resources.downImage();
        }
    }

}
