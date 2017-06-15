package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;

import com.google.gwt.resources.client.ImageResource;

public class NetworkClusterStatusColumn extends AbstractImageResourceColumn<PairQueryable<Cluster, NetworkCluster>>{

    private final NetworkStatusColumn networkStatusColumn = new NetworkStatusColumn();

    @Override
    public ImageResource getValue(PairQueryable<Cluster, NetworkCluster> object) {
        if (object.getSecond() != null){
            return networkStatusColumn.getValue(object.getSecond());
        }
     return null;
    }

}
