package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.utils.Pair;

import com.google.gwt.resources.client.ImageResource;

public class NetworkClusterStatusColumn extends WebAdminImageResourceColumn<Pair<network_cluster, VDSGroup>>{

    private final NetworkStatusColumn networkStatusColumn = new NetworkStatusColumn();


    @Override
    public ImageResource getValue(Pair<network_cluster, VDSGroup> object) {
       return networkStatusColumn.getValue(object.getFirst());
    }

}
